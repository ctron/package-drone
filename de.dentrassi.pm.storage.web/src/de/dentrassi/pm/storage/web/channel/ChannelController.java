/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.channel;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;
import javax.xml.ws.Holder;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.net.UrlEscapers;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.ProfilerControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.group.GroupInformation;
import de.dentrassi.pm.aspect.recipe.RecipeInformation;
import de.dentrassi.pm.aspect.recipe.RecipeNotFoundException;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.AspectableChannel;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ChannelInformation;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.DeployKeysChannelAdapter;
import de.dentrassi.pm.storage.channel.DescriptorAdapter;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.channel.deploy.DeployAuthService;
import de.dentrassi.pm.storage.channel.deploy.DeployGroup;
import de.dentrassi.pm.storage.channel.util.DownloadHelper;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Tags;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;
import de.dentrassi.pm.storage.web.internal.Activator;
import de.dentrassi.pm.storage.web.utils.Channels;
import de.dentrassi.pm.system.SitePrefixService;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ControllerInterceptor ( ProfilerControllerInterceptor.class )
public class ChannelController implements InterfaceExtender
{

    private static final String DEFAULT_EXAMPLE_KEY = "xxxxx";

    private final static Logger logger = LoggerFactory.getLogger ( ChannelController.class );

    private static final MessageFormat EXPORT_PATTERN = new MessageFormat ( "export-channel-{0}-{1,date,yyyyMMdd-HHmm}.zip" );

    private static final MessageFormat EXPORT_ALL_PATTERN = new MessageFormat ( "export-all-{0,date,yyyyMMdd-HHmm}.zip" );

    private StorageService service;

    private DeployAuthService deployAuthService;

    private SitePrefixService sitePrefix;

    private ChannelService channelService;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( ChannelController.class ).getBundleContext () );

    public void setChannelService ( final ChannelService channelService )
    {
        this.channelService = channelService;
    }

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void setDeployAuthService ( final DeployAuthService deployAuthService )
    {
        this.deployAuthService = deployAuthService;
    }

    public void setSitePrefixService ( final SitePrefixService sitePrefix )
    {
        this.sitePrefix = sitePrefix;
    }

    public void start ()
    {
        this.generators.open ();
    }

    public void stop ()
    {
        this.generators.close ();
    }

    private static final List<MenuEntry> menuEntries = Collections.singletonList ( new MenuEntry ( "Channels", 100, new LinkTarget ( "/channel" ), Modifier.DEFAULT, null ) );

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        return menuEntries;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView list ()
    {
        final ModelAndView result = new ModelAndView ( "channel/list" );

        final List<ChannelInformation> channels = new ArrayList<> ( this.channelService.list () );
        channels.sort ( ChannelId.NAME_COMPARATOR );
        result.put ( "channels", channels );

        return result;
    }

    @RequestMapping ( value = "/channel/create", method = RequestMethod.GET )
    public ModelAndView create ()
    {
        // FIXME: with provider id
        this.channelService.create ( null, null );

        return new ModelAndView ( "redirect:/channel" );
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.GET )
    public ModelAndView createDetailed ()
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", new CreateChannel () );
        return new ModelAndView ( "channel/create", model );
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.POST )
    public ModelAndView createDetailedPost ( @Valid @FormData ( "command" ) final CreateChannel data, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            final ChannelDetails desc = new ChannelDetails ();
            desc.setDescription ( data.getDescription () );
            // FIXME: with provider id
            final ChannelId channel = this.channelService.create ( null, desc );
            setChannelName ( channel, data.getName () );

            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", urlPathSegmentEscaper ().escape ( channel.getId () ) ) );
        }

        return new ModelAndView ( "channel/create" );
    }

    @RequestMapping ( value = "/channel/createWithRecipe", method = RequestMethod.GET )
    public ModelAndView createWithRecipe ()
    {
        final Map<String, Object> model = new HashMap<> ( 2 );

        model.put ( "command", new CreateChannel () );
        model.put ( "recipes", Activator.getRecipes ().getSortedRecipes ( RecipeInformation::getLabel ) );

        return new ModelAndView ( "channel/createWithRecipe", model );
    }

    @RequestMapping ( value = "/channel/createWithRecipe", method = RequestMethod.POST )
    public ModelAndView createWithRecipePost ( @Valid @FormData ( "command" ) final CreateChannel data, @RequestParameter (
            required = false,
            value = "recipe" ) final String recipeId, final BindingResult result) throws UnsupportedEncodingException, RecipeNotFoundException
    {
        if ( !result.hasErrors () )
        {

            final Holder<ChannelId> holder = new Holder<> ();
            final Holder<String> targetHolder = new Holder<> ();

            if ( recipeId == null || recipeId.isEmpty () )
            {
                // without recipe
                final ChannelDetails desc = new ChannelDetails ();
                desc.setDescription ( data.getDescription () );
                //FIXME: add provider id
                holder.value = this.channelService.create ( null, desc );
                setChannelName ( holder.value, data.getName () );
            }
            else
            {
                // with recipe
                Activator.getRecipes ().process ( recipeId, recipe -> {
                    final ChannelDetails desc = new ChannelDetails ();
                    desc.setDescription ( data.getDescription () );

                    //FIXME: add provider id
                    final ChannelId channel = this.channelService.create ( null, desc );

                    setChannelName ( channel, data.getName () );

                    this.channelService.access ( By.id ( channel.getId () ), AspectableChannel.class, aspChannel -> {

                        final LinkTarget target = recipe.setup ( channel.getId (), aspChannel );

                        if ( target != null )
                        {
                            final Map<String, String> model = new HashMap<> ( 1 );
                            model.put ( "channelId", channel.getId () );
                            targetHolder.value = target.expand ( model ).getUrl ();
                        }
                    } );

                    holder.value = channel;
                } );

                if ( targetHolder.value != null )
                {
                    return new ModelAndView ( "redirect:" + targetHolder.value );
                }
            }

            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", URLEncoder.encode ( holder.value.getId (), "UTF-8" ) ) );
        }

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "recipes", Activator.getRecipes ().getSortedRecipes ( RecipeInformation::getLabel ) );

        return new ModelAndView ( "channel/createWithRecipe", model );
    }

    protected void setChannelName ( final ChannelId id, final String name )
    {
        this.channelService.access ( By.id ( id.getId () ), DescriptorAdapter.class, channel -> {
            channel.setName ( name );
        } );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView view ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        final Optional<ChannelInformation> channel = this.channelService.getState ( By.name ( channelId ) );
        if ( channel.isPresent () )
        {
            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", channel.get ().getId () ) );
        }
        else
        {
            request.getRequestDispatcher ( "tree" ).forward ( request, response );
            return null;
        }
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/viewPlain", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewPlain ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/view" );

        try
        {
            this.channelService.access ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {

                final List<ArtifactInformation> sortedArtifacts = new ArrayList<> ( channel.getContext ().getArtifacts ().values () );
                sortedArtifacts.sort ( Comparator.comparing ( ArtifactInformation::getName ) );

                result.put ( "channel", channel.getInformation () );
                result.put ( "sortedArtifacts", sortedArtifacts );

            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/tree", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView tree ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/tree" );

        try
        {
            this.channelService.access ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {

                final Map<String, List<ArtifactInformation>> tree = new HashMap<> ();

                for ( final ArtifactInformation entry : channel.getContext ().getArtifacts ().values () )
                {
                    List<ArtifactInformation> list = tree.get ( entry.getParentId () );
                    if ( list == null )
                    {
                        list = new LinkedList<> ();
                        tree.put ( entry.getParentId (), list );
                    }
                    list.add ( entry );
                }

                result.put ( "channel", channel.getInformation () );
                result.put ( "treeArtifacts", tree );
                result.put ( "treeSeverityTester", new TreeTesterImpl ( tree ) );

            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/validation", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewValidation ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.channelService.access ( By.id ( channelId ), ReadableChannel.class, channel -> {
                final ModelAndView result = new ModelAndView ( "channel/validation" );

                result.put ( "channel", channel.getInformation () );
                result.put ( "messages", channel.getInformation ().getState ().getValidationMessages () );

                return result;
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/details", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView details ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/details" );

        try
        {
            this.channelService.access ( By.id ( channelId ), ReadableChannel.class, ( channel ) -> {
                result.put ( "channel", channel.getInformation () );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        if ( this.channelService.delete ( By.id ( channelId ) ) )
        {
            result.put ( "success", String.format ( "Deleted channel %s", channelId ) );
        }
        else
        {
            result.put ( "warning", String.format ( "Unable to delete channel %s. Was not found.", channelId ) );
        }

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/delete", method = RequestMethod.GET )
    public ModelAndView deleteArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {
            channel.getContext ().deleteArtifact ( artifactId );
            return redirectDefaultView ( channelId, true );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/get", method = RequestMethod.GET )
    public void getArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, final HttpServletResponse response) throws IOException
    {
        DownloadHelper.streamArtifact ( response, this.channelService, channelId, artifactId, null, true );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/dump", method = RequestMethod.GET )
    public void dumpArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, final HttpServletResponse response) throws IOException
    {
        DownloadHelper.streamArtifact ( response, this.channelService, channelId, artifactId, null, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifacts/{artifactId}/view", method = RequestMethod.GET )
    public ModelAndView viewArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "aspect", artifactId );
            }

            final Map<String, Object> model = new HashMap<String, Object> ( 1 );
            model.put ( "artifact", artifact.get () );

            return new ModelAndView ( "artifact/view", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.GET )
    public ModelAndView add ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView mav = new ModelAndView ( "/channel/add" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );

        return mav;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.POST )
    public ModelAndView addPost ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter (
            required = false, value = "name" ) String name, final @RequestParameter ( "file" ) Part file)
    {
        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            final String finalName = name;

            this.channelService.access ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.getContext ().createArtifact ( file.getInputStream (), finalName, null );
            } );

            return redirectDefaultView ( channelId, true );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Upload", "Upload failed", e );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/drop", method = RequestMethod.POST )
    public void drop ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( required = false,
            value = "name" ) String name, final @RequestParameter ( "file" ) Part file, final HttpServletResponse response) throws IOException
    {
        response.setContentType ( "text/plain" );

        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            final String finalName = name;

            this.channelService.access ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.getContext ().createArtifact ( file.getInputStream (), finalName, null );
            } );
        }
        catch ( final Throwable e )
        {
            logger.debug ( "Failed to drop file", e );
            response.setStatus ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            response.getWriter ().write ( "Internal error: " + ExceptionHelper.getMessage ( e ) );
            return;
        }

        response.setStatus ( HttpServletResponse.SC_OK );
        response.getWriter ().write ( "OK" );
    }

    @RequestMapping ( value = "/channel/{channelId}/clear", method = RequestMethod.GET )
    public ModelAndView clear ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {
            channel.getContext ().clear ();
            return redirectDefaultView ( channelId, true );
        } );
    }

    protected ModelAndView redirectDefaultView ( final String channelId, final boolean force )
    {
        return new ModelAndView ( ( force ? "redirect" : "referer" ) + ":/channel/" + channelId + "/view" );
    }

    @RequestMapping ( value = "/channel/{channelId}/deployKeys" )
    public ModelAndView deployKeys ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, DeployKeysChannelAdapter.class, deployChannel -> {
            return withChannel ( channelId, ReadableChannel.class, channel -> {
                final Map<String, Object> model = new HashMap<> ();

                final List<DeployGroup> channelDeployGroups = new ArrayList<> ( deployChannel.getDeployGroups () );
                Collections.sort ( channelDeployGroups, DeployGroup.NAME_COMPARATOR );

                model.put ( "channel", channel.getInformation () );
                model.put ( "channelDeployGroups", channelDeployGroups );
                model.put ( "deployGroups", getGroupsForChannel ( channelDeployGroups ) );

                model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

                return new ModelAndView ( "channel/deployKeys", model );
            } );
        } );
    }

    protected List<DeployGroup> getGroupsForChannel ( final Collection<DeployGroup> channelDeployGroups )
    {
        final List<DeployGroup> groups = new ArrayList<> ( this.deployAuthService.listGroups ( 0, -1 ) );
        groups.removeAll ( channelDeployGroups );
        Collections.sort ( groups, DeployGroup.NAME_COMPARATOR );
        return groups;
    }

    protected <T> ModelAndView withChannel ( final String channelId, final Class<T> clazz, final ChannelOperation<ModelAndView, T> operation )
    {
        return Channels.withChannel ( this.channelService, channelId, clazz, operation );
    }

    @RequestMapping ( "/channel/{channelId}/help/p2" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView helpP2 ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

            model.put ( "p2Active", channel.hasAspect ( "p2.repo" ) );

            return new ModelAndView ( "channel/help/p2", model );
        } );
    }

    @RequestMapping ( "/channel/{channelId}/help/api" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView helpApi ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefix.getSitePrefix () );

            final String exampleKey;
            if ( request.isUserInRole ( "MANAGER" ) )
            {
                //FIXME:  exampleKey = channel.getDeployGroups ().stream ().flatMap ( dg -> dg.getKeys ().stream () ).map ( DeployKey::getKey ).findFirst ().orElse ( DEFAULT_EXAMPLE_KEY );
                exampleKey = DEFAULT_EXAMPLE_KEY;
            }
            else
            {
                exampleKey = DEFAULT_EXAMPLE_KEY;
            }

            model.put ( "exampleKey", exampleKey );
            model.put ( "exampleSitePrefix", makeCredentialsPrefix ( this.sitePrefix.getSitePrefix (), "deploy", exampleKey ) );

            return new ModelAndView ( "channel/help/api", model );
        } );
    }

    private String makeCredentialsPrefix ( final String sitePrefix, final String name, final String password )
    {
        try
        {
            final URIBuilder builder = new URIBuilder ( sitePrefix );

            builder.setUserInfo ( name, password );

            return builder.build ().toString ();
        }
        catch ( final URISyntaxException e )
        {
            return sitePrefix;
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/addDeployGroup", method = RequestMethod.POST )
    public ModelAndView addDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, DeployKeysChannelAdapter::assignDeployGroup );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeDeployGroup", method = RequestMethod.POST )
    public ModelAndView removeDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, DeployKeysChannelAdapter::unassignDeployGroup );
    }

    protected ModelAndView modifyDeployGroup ( final String channelId, final String groupId, final BiConsumer<DeployKeysChannelAdapter, String> cons )
    {
        return withChannel ( channelId, DeployKeysChannelAdapter.class, channel -> {
            cons.accept ( channel, groupId );

            return new ModelAndView ( "redirect:/channel/" + channelId + "/deployKeys" );
        } );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/aspects", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView aspects ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            final ModelAndView model = new ModelAndView ( "channel/aspects" );

            final ChannelAspectProcessor aspects = Activator.getAspects ();
            final Collection<GroupInformation> groups = aspects.getGroups ();

            model.put ( "channel", channel.getInformation () );

            final Set<String> assigned = channel.getInformation ().getAspectStates ().keySet ();

            final List<AspectInformation> allAspects = AspectInformation.resolve ( groups, aspects.getAspectInformations ().values () );

            final List<AspectInformation> assignedAspects = AspectInformation.filterIds ( allAspects, ( id ) -> assigned.contains ( id ) );
            model.put ( "assignedAspects", assignedAspects );

            model.put ( "groupedAssignedAspects", AspectInformation.group ( assignedAspects ) );

            model.put ( "addAspects", AspectInformation.group ( AspectInformation.filterIds ( allAspects, ( id ) -> !assigned.contains ( id ) ) ) );

            final Map<String, String> nameMap = new HashMap<> ();
            for ( final AspectInformation ai : allAspects )
            {
                nameMap.put ( ai.getFactoryId (), ai.getName () );
            }

            model.put ( "nameMapJson", new GsonBuilder ().create ().toJson ( nameMap ) );

            model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Aspects" ) ) );

            return model;
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/viewAspectVersions", method = RequestMethod.GET )
    public ModelAndView viewAspectVersions ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, String> states = channel.getInformation ().getAspectStates ();

            final List<ChannelAspectInformation> aspects = Activator.getAspects ().resolve ( states.keySet () );
            Collections.sort ( aspects, ChannelAspectInformation.NAME_COMPARATOR );

            final Map<String, Object> model = new HashMap<> ( 3 );

            model.put ( "channel", channel.getInformation () );
            model.put ( "states", states );
            model.put ( "aspects", aspects );

            return new ModelAndView ( "channel/viewAspectVersions", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/lock", method = RequestMethod.GET )
    public ModelAndView lock ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            this.channelService.access ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.lock ();
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/unlock", method = RequestMethod.GET )
    public ModelAndView unlock ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            this.channelService.access ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                channel.unlock ();
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.addAspects ( false, aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspectWithDependencies", method = RequestMethod.POST )
    public ModelAndView addAspectWithDependencies ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.addAspects ( true, aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.removeAspects ( aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAspect", method = RequestMethod.POST )
    public ModelAndView refreshAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.refreshAspects ( aspectFactoryId );
            return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAllAspects", method = RequestMethod.GET )
    public ModelAndView refreshAllAspects ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request)
    {
        return withChannel ( channelId, AspectableChannel.class, channel -> {
            channel.refreshAspects ();
            return redirectDefaultView ( channelId, false );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Map<String, Object> model = new HashMap<> ();

        final Optional<ChannelInformation> info = this.channelService.getState ( By.id ( channelId ) );
        if ( !info.isPresent () )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final EditChannel edit = new EditChannel ();

        final ChannelInformation channel = info.get ();

        edit.setId ( channel.getId () );
        edit.setName ( channel.getName () );
        edit.setDescription ( channel.getState ().getDescription () );

        model.put ( "command", edit );
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );

        return new ModelAndView ( "channel/edit", model );

    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final EditChannel data, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            this.channelService.access ( By.id ( channelId ), ModifiableChannel.class, channel -> {
                final ChannelDetails newDesc = new ChannelDetails ();
                newDesc.setDescription ( data.getDescription () );
                channel.setDescription ( newDesc );
            } );

            this.channelService.access ( By.id ( channelId ), DescriptorAdapter.class, channel -> {
                channel.setName ( data.getName () );
            } );

            return redirectDefaultView ( channelId, true );
        }
        else
        {
            final Map<String, Object> model = new HashMap<> ();
            model.put ( "command", data );
            model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );
            return new ModelAndView ( "channel/edit", model );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCache", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCache ( @PathVariable ( "channelId" ) final String channelId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ();

            model.put ( "channel", channel.getInformation () );
            model.put ( "cacheEntries", channel.getCacheEntries ().values () );

            return new ModelAndView ( "channel/viewCache", model );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCacheEntry", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCacheEntry ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "namespace" ) final String namespace, @RequestParameter ( "key" ) final String key, final HttpServletResponse response)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {

            if ( !channel.streamCacheEntry ( new MetaKey ( namespace, key ), entry -> {
                logger.trace ( "Length: {}, Mime: {}", entry.getSize (), entry.getMimeType () );

                response.setContentLengthLong ( entry.getSize () );
                response.setContentType ( entry.getMimeType () );
                response.setHeader ( "Content-Disposition", String.format ( "inline; filename=%s", URLEncoder.encode ( entry.getName (), "UTF-8" ) ) );
                // response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", entry.getName () ) );
                ByteStreams.copy ( entry.getStream (), response.getOutputStream () );
            } ) )
            {
                return CommonController.createNotFound ( "channel cache entry", String.format ( "%s:%s", namespace, key ) );
            }

            return null;
        } );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( !channel.getState ().isLocked () )
                {
                    result.add ( new MenuEntry ( "Add Artifact", 100, LinkTarget.createFromController ( ChannelController.class, "add" ).expand ( model ), Modifier.PRIMARY, null ) );
                    result.add ( new MenuEntry ( "Delete Channel", 400, LinkTarget.createFromController ( ChannelController.class, "delete" ).expand ( model ), Modifier.DANGER, "trash" ).makeModalMessage ( "Delete channel", "Are you sure you want to delete the whole channel?" ) );
                    result.add ( new MenuEntry ( "Clear Channel", 500, LinkTarget.createFromController ( ChannelController.class, "clear" ).expand ( model ), Modifier.WARNING, null ).makeModalMessage ( "Clear channel", "Are you sure you want to delete all artifacts from this channel?" ) );

                    result.add ( new MenuEntry ( "Lock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "lock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }
                else
                {
                    result.add ( new MenuEntry ( "Unlock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "unlock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }

                result.add ( new MenuEntry ( "Edit", 150, "Edit Channel", 200, LinkTarget.createFromController ( ChannelController.class, "edit" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Refresh aspects", 100, LinkTarget.createFromController ( ChannelController.class, "refreshAllAspects" ).expand ( model ), Modifier.SUCCESS, "refresh" ) );
            }

            if ( request.getRemoteUser () != null )
            {
                result.add ( new MenuEntry ( "Edit", 150, "Configure Aspects", 300, LinkTarget.createFromController ( ChannelController.class, "aspects" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Export channel", 200, LinkTarget.createFromController ( ChannelController.class, "exportChannel" ).expand ( model ), Modifier.DEFAULT, "export" ) );
            }

            return result;
        }
        else if ( Tags.ACTION_TAG_CHANNELS.equals ( object ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                // result.add ( new MenuEntry ( "Create Channel", 100, LinkTarget.createFromController ( ChannelController.class, "createDetailed" ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Create Channel", 120, LinkTarget.createFromController ( ChannelController.class, "createWithRecipe" ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Import channel", 200, LinkTarget.createFromController ( ChannelController.class, "importChannel" ), Modifier.DEFAULT, "import" ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Export all channels", 300, LinkTarget.createFromController ( ChannelController.class, "exportAll" ), Modifier.DEFAULT, "export" ) );
            }

            return result;
        }
        else if ( object instanceof de.dentrassi.pm.storage.channel.ChannelArtifactInformation )
        {
            final ChannelArtifactInformation ai = (ChannelArtifactInformation)object;

            final List<MenuEntry> result = new LinkedList<> ();

            final Map<String, Object> model = new HashMap<> ( 2 );
            model.put ( "channelId", ai.getChannelId ().getId () );
            model.put ( "artifactId", ai.getId () );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( ai.is ( "stored" ) )
                {
                    result.add ( new MenuEntry ( "Attach Artifact", 200, LinkTarget.createFromController ( ChannelController.class, "attachArtifact" ).expand ( model ), Modifier.PRIMARY, null ) );
                    result.add ( new MenuEntry ( "Delete", 1000, LinkTarget.createFromController ( ChannelController.class, "deleteArtifact" ).expand ( model ), Modifier.DANGER, "trash" ) );
                }
            }

            return result;
        }
        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "Content", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "List", 120, LinkTarget.createFromController ( ChannelController.class, "viewPlain" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "Details", 200, LinkTarget.createFromController ( ChannelController.class, "details" ).expand ( model ), Modifier.DEFAULT, null ) );

            result.add ( new MenuEntry ( null, -1, "Validation", 210, LinkTarget.createFromController ( ChannelController.class, "viewValidation" ).expand ( model ), Modifier.DEFAULT, null ).setBadge ( channel.getState ().getValidationErrorCount () ) );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                result.add ( new MenuEntry ( "Deploy Keys", 1000, LinkTarget.createFromController ( ChannelController.class, "deployKeys" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            if ( request.isUserInRole ( "MANAGER" ) || request.isUserInRole ( "ADMIN" ) )
            {
                result.add ( new MenuEntry ( "Internal", 400, "View Cache", 100, LinkTarget.createFromController ( ChannelController.class, "viewCache" ).expand ( model ), Modifier.DEFAULT, null ) );
                result.add ( new MenuEntry ( "Internal", 400, "Aspect Versions", 100, LinkTarget.createFromController ( ChannelController.class, "viewAspectVersions" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            if ( channel.hasAspect ( "p2.repo" ) )
            {
                result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "P2 Repository", 2_000, LinkTarget.createFromController ( ChannelController.class, "helpP2" ).expand ( model ), Modifier.DEFAULT, "info-sign" ) );
            }

            result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "API Upload", 1_100, LinkTarget.createFromController ( ChannelController.class, "helpApi" ).expand ( model ), Modifier.DEFAULT, "upload" ) );

            return result;
        }
        return null;
    }

    @ControllerValidator ( formDataClass = CreateChannel.class )
    public void validateCreate ( final CreateChannel data, final ValidationContext ctx )
    {
        validateChannelNameUnique ( null, data.getName (), ctx );
    }

    @ControllerValidator ( formDataClass = EditChannel.class )
    public void validateEdit ( final EditChannel data, final ValidationContext ctx )
    {
        validateChannelNameUnique ( data.getId (), data.getName (), ctx );
    }

    private void validateChannelNameUnique ( final String id, final String name, final ValidationContext ctx )
    {
        if ( name == null || name.isEmpty () )
        {
            return;
        }

        final ChannelInformation other = this.channelService.getState ( By.name ( name ) ).orElse ( null );
        if ( id != null && other != null && !other.getId ().equals ( id ) )
        {
            ctx.error ( "name", String.format ( "The channel name '%s' is already in use by channel '%s'", name, other.getId () ) );
        }
    }

    protected ModelAndView performExport ( final HttpServletResponse response, final String filename, final IOConsumer<OutputStream> exporter )
    {
        try
        {
            final Path tmp = Files.createTempFile ( "export-", null );

            try
            {
                try ( OutputStream tmpStream = new BufferedOutputStream ( new FileOutputStream ( tmp.toFile () ) ) )
                {
                    // first we spool this out to  temp file, so that we don't block the channel for too long
                    exporter.accept ( tmpStream );
                }

                response.setContentLengthLong ( tmp.toFile ().length () );
                response.setContentType ( "application/zip" );
                response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", filename ) );

                try ( InputStream inStream = new BufferedInputStream ( new FileInputStream ( tmp.toFile () ) ) )
                {
                    ByteStreams.copy ( inStream, response.getOutputStream () );
                }

                return null;
            }
            finally
            {
                Files.deleteIfExists ( tmp );
            }
        }
        catch ( final IOException e )
        {
            return CommonController.createError ( "Failed to export", null, e );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/export", method = RequestMethod.GET )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView exportChannel ( @PathVariable ( "channelId" ) final String channelId, final HttpServletResponse response)
    {
        return performExport ( response, makeExportFileName ( channelId ), ( stream ) -> this.service.exportChannel ( channelId, stream ) );
    }

    @RequestMapping ( value = "/channel/export", method = RequestMethod.GET )
    @HttpConstraint ( value = EmptyRoleSemantic.PERMIT )
    public ModelAndView exportAll ( final HttpServletResponse response )
    {
        return performExport ( response, makeExportFileName ( null ), this.service::exportAll );
    }

    @RequestMapping ( value = "/channel/import", method = RequestMethod.GET )
    public ModelAndView importChannel ()
    {
        return new ModelAndView ( "channel/importChannel" );
    }

    @RequestMapping ( value = "/channel/import", method = RequestMethod.POST )
    public ModelAndView importChannelPost ( @RequestParameter ( "file" ) final Part part, @RequestParameter (
            value = "useName", required = false ) final boolean useName)
    {
        try
        {
            final Channel channel = this.service.importChannel ( part.getInputStream (), useName );
            return new ModelAndView ( "redirect:/channel/" + channel.getId () + "/view" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import", e );
            return CommonController.createError ( "Import", "Channel", "Failed to import channel", e, null );
        }
    }

    @RequestMapping ( value = "/channel/importAll", method = RequestMethod.GET )
    public ModelAndView importAll ( final HttpServletResponse response )
    {
        return new ModelAndView ( "channel/importAll" );
    }

    @RequestMapping ( value = "/channel/importAll", method = RequestMethod.POST )
    public ModelAndView importAllPost ( @RequestParameter ( value = "useNames",
            required = false ) final boolean useNames, @RequestParameter ( value = "wipe",
                    required = false ) final boolean wipe, @RequestParameter ( "file" ) final Part part, @RequestParameter (
                            value = "location", required = false ) final String location)
    {
        try
        {
            if ( location != null && !location.isEmpty () )
            {
                try ( BufferedInputStream stream = new BufferedInputStream ( new FileInputStream ( new File ( location ) ) ) )
                {
                    this.service.importAll ( stream, useNames, wipe );
                }
            }
            else
            {
                this.service.importAll ( part.getInputStream (), useNames, wipe );
            }
            return new ModelAndView ( "redirect:/channel" );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to import", e );
            return CommonController.createError ( "Import", "Channel", "Failed to import channel", e, null );
        }
    }

    private String makeExportFileName ( final String channelId )
    {
        if ( channelId != null )
        {
            return EXPORT_PATTERN.format ( new Object[] { channelId, new Date () } );
        }
        else
        {
            return EXPORT_ALL_PATTERN.format ( new Object[] { new Date () } );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/artifact/{artifactId}/attach", method = RequestMethod.GET )
    public ModelAndView attachArtifact ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId)
    {
        return withChannel ( channelId, ReadableChannel.class, channel -> {
            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            return new ModelAndView ( "/artifact/attach", "artifact", artifact.get () );
        } );
    }

    @RequestMapping ( value = "/channel/{channelId}/artifact/{artifactId}/attach", method = RequestMethod.POST )
    public ModelAndView attachArtifactPost ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, @RequestParameter (
            required = false, value = "name" ) final String name, final @RequestParameter ( "file" ) Part file)
    {
        return withChannel ( channelId, ModifiableChannel.class, channel -> {

            String targetName = name;

            final Optional<ChannelArtifactInformation> parentArtifact = channel.getArtifact ( artifactId );
            if ( !parentArtifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            try
            {
                if ( targetName == null || targetName.isEmpty () )
                {
                    targetName = file.getSubmittedFileName ();
                }

                channel.getContext ().createArtifact ( artifactId, file.getInputStream (), targetName, null );
            }
            catch ( final IOException e )
            {
                return new ModelAndView ( "/error/upload" );
            }

            return new ModelAndView ( "redirect:/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId ) + "/view" );
        } );
    }

}
