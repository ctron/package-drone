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

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;

import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.aspect.group.GroupInformation;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.service.DeployAuthService;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Tags;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;
import de.dentrassi.pm.storage.web.internal.Activator;
import de.dentrassi.pm.system.SystemService;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ChannelController implements InterfaceExtender
{

    private final static Logger logger = LoggerFactory.getLogger ( ChannelController.class );

    private static final MessageFormat EXPORT_PATTERN = new MessageFormat ( "export-channel-{0}-{1,date,yyyyMMdd-HHmm}.zip" );

    private static final MessageFormat EXPORT_ALL_PATTERN = new MessageFormat ( "export-all-{0,date,yyyyMMdd-HHmm}.zip" );

    private StorageService service;

    private CoreService coreService;

    private DeployAuthService deployAuthService;

    private SystemService systemService;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( ChannelController.class ).getBundleContext () );

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void setDeployAuthService ( final DeployAuthService deployAuthService )
    {
        this.deployAuthService = deployAuthService;
    }

    public void setSystemService ( final SystemService systemService )
    {
        this.systemService = systemService;
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

        final List<Channel> channels = new ArrayList<> ( this.service.listChannels () );
        channels.sort ( ChannelNameComparator.INSTANCE );
        result.put ( "channels", channels );

        return result;
    }

    @RequestMapping ( value = "/channel/create", method = RequestMethod.GET )
    public ModelAndView create ()
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        this.service.createChannel ();

        return result;
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.GET )
    public ModelAndView createDetailed ()
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", new CreateChannel () );
        return new ModelAndView ( "channel/create", model );
    }

    @RequestMapping ( value = "/channel/createDetailed", method = RequestMethod.POST )
    public ModelAndView createDetailedPost ( @Valid @FormData ( "command" ) final CreateChannel data, final BindingResult result) throws UnsupportedEncodingException
    {
        if ( !result.hasErrors () )
        {
            final Channel channel = this.service.createChannel ( data.getName (), data.getDescription () );
            return new ModelAndView ( String.format ( "redirect:/channel/%s/view", URLEncoder.encode ( channel.getId (), "UTF-8" ) ) );
        }

        return new ModelAndView ( "channel/create" );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public void view ( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException, IOException
    {
        request.getRequestDispatcher ( "tree" ).forward ( request, response );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/viewPlain", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewPlain ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/view" );

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final List<SimpleArtifactInformation> sortedArtifacts = new ArrayList<> ( channel.getSimpleArtifacts () );
        sortedArtifacts.sort ( SimpleArtifactInformation.NAME_COMPARATOR );

        result.put ( "channel", channel );
        result.put ( "sortedArtifacts", sortedArtifacts );

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/tree", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView tree ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/tree" );

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, List<SimpleArtifactInformation>> tree = new HashMap<> ();

        for ( final SimpleArtifactInformation entry : channel.getDetailedArtifacts () )
        {
            List<SimpleArtifactInformation> list = tree.get ( entry.getParentId () );
            if ( list == null )
            {
                list = new LinkedList<> ();
                tree.put ( entry.getParentId (), list );
            }
            list.add ( entry );
        }

        result.put ( "channel", channel );
        result.put ( "treeArtifacts", tree );
        result.put ( "treeSeverityTester", new TreeTester ( tree ) );

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/validation", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView viewValidation ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final ModelAndView result = new ModelAndView ( "channel/validation" );
        result.put ( "channel", channel );
        result.put ( "messages", channel.getValidationMessages () );
        result.put ( "aspects", Activator.getAspects ().getAspectInformations () );

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/details", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView details ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "channel/details" );

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final List<SimpleArtifactInformation> sortedArtifacts = new ArrayList<> ( channel.getSimpleArtifacts () );
        sortedArtifacts.sort ( SimpleArtifactInformation.NAME_COMPARATOR );

        result.put ( "channel", channel );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        this.service.deleteChannel ( channelId );
        result.put ( "success", String.format ( "Deleted channel %s", channelId ) );

        return result;
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

            this.service.createArtifact ( channelId, name, file.getInputStream (), null );
        }
        catch ( final IOException e )
        {
            return CommonController.createError ( "Upload", "Upload failed", e );
        }

        return redirectDefaultView ( channelId, true );
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

            this.service.createArtifact ( channelId, name, file.getInputStream (), null );
        }
        catch ( final Throwable e )
        {
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
        this.service.clearChannel ( channelId );

        return redirectDefaultView ( channelId, true );
    }

    protected ModelAndView redirectDefaultView ( final String channelId, final boolean force )
    {
        return new ModelAndView ( ( force ? "redirect" : "referer" ) + ":/channel/" + channelId + "/view" );
    }

    @RequestMapping ( value = "/channel/{channelId}/deployKeys" )
    public ModelAndView deployKeys ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "channel", channel );
        model.put ( "deployGroups", getGroupsForChannel ( channel ) );

        model.put ( "sitePrefix", getSitePrefix () );

        return new ModelAndView ( "channel/deployKeys", model );
    }

    private String getSitePrefix ()
    {
        final String prefix = this.coreService.getCoreProperty ( "site-prefix", this.systemService.getDefaultSitePrefix () );
        if ( prefix != null )
        {
            return prefix;
        }
        return "http://localhost:8080";
    }

    protected List<DeployGroup> getGroupsForChannel ( final Channel channel )
    {
        final List<DeployGroup> groups = this.deployAuthService.listGroups ( 0, -1 );
        groups.removeAll ( channel.getDeployGroups () );
        Collections.sort ( groups, DeployGroup.NAME_COMPARATOR );
        return groups;
    }

    @RequestMapping ( "/channel/{channelId}/help.p2" )
    public ModelAndView helpP2 ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "channel", channel );
        model.put ( "sitePrefix", getSitePrefix () );

        model.put ( "p2Active", channel.hasAspect ( "p2.repo" ) );

        return new ModelAndView ( "channel/help/p2", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/addDeployGroup", method = RequestMethod.POST )
    public ModelAndView addDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, Channel::addDeployGroup );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeDeployGroup", method = RequestMethod.POST )
    public ModelAndView removeDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId)
    {
        return modifyDeployGroup ( channelId, groupId, Channel::removeDeployGroup );
    }

    protected ModelAndView modifyDeployGroup ( final String channelId, final String groupId, final BiConsumer<Channel, String> cons )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        cons.accept ( channel, groupId );

        return new ModelAndView ( "redirect:/channel/" + channelId + "/deployKeys" );
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/aspects", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView aspects ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final ModelAndView model = new ModelAndView ( "channel/aspects" );

        final ChannelAspectProcessor aspects = Activator.getAspects ();
        final Collection<GroupInformation> groups = aspects.getGroups ();

        model.put ( "channel", channel );

        final Set<String> assigned = channel.getAspectIds ();

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
    }

    @RequestMapping ( value = "/channel/{channelId}/viewAspectVersions", method = RequestMethod.GET )
    public ModelAndView viewAspectVersions ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, String> states = channel.getAspectStates ();

        final Map<String, Object> model = new HashMap<> ( 3 );

        final List<ChannelAspectInformation> aspects = channel.getAspects ();
        Collections.sort ( aspects, ChannelAspectInformation.NAME_COMPARATOR );

        model.put ( "channel", channel );
        model.put ( "states", states );
        model.put ( "aspects", aspects );

        return new ModelAndView ( "channel/viewAspectVersions", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/lock", method = RequestMethod.GET )
    public ModelAndView lock ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        channel.lock ();

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/unlock", method = RequestMethod.GET )
    public ModelAndView unlock ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        channel.unlock ();

        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        this.service.addChannelAspect ( channelId, aspectFactoryId, false );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspectWithDependencies", method = RequestMethod.POST )
    public ModelAndView addAspectWithDependencies ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        this.service.addChannelAspect ( channelId, aspectFactoryId, true );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        this.service.removeChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAspect", method = RequestMethod.POST )
    public ModelAndView refreshAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId)
    {
        this.service.refreshChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "referer:/channel/" + channelId + "/aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAllAspects", method = RequestMethod.GET )
    public ModelAndView refreshAllAspects ( @PathVariable ( "channelId" ) final String channelId, final HttpServletRequest request)
    {
        this.service.refreshAllChannelAspects ( channelId );
        return redirectDefaultView ( channelId, false );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final EditChannel edit = new EditChannel ();
        edit.setId ( channel.getId () );
        edit.setName ( channel.getName () );
        edit.setDescription ( channel.getDescription () );

        model.put ( "command", edit );
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );

        return new ModelAndView ( "channel/edit", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final EditChannel data, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            this.service.updateChannel ( channelId, data.getName (), data.getDescription () );
            return redirectDefaultView ( channelId, true );
        }
        else
        {
            model.put ( "command", data );
            model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );
            return new ModelAndView ( "channel/edit", model );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCache", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCache ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        model.put ( "channel", channel );
        model.put ( "cacheEntries", channel.getAllCacheEntries () );

        return new ModelAndView ( "channel/viewCache", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/viewCacheEntry", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView viewCacheEntry ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "namespace" ) final String namespace, @RequestParameter ( "key" ) final String key, final HttpServletResponse response)
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        model.put ( "channel", channel );

        try
        {
            channel.streamCacheEntry ( new MetaKey ( namespace, key ), ( cacheEntry ) -> {
                response.setContentLengthLong ( cacheEntry.getSize () );
                response.setContentType ( cacheEntry.getMimeType () );
                response.setHeader ( "Content-Disposition", String.format ( "inline; filename=%s", URLEncoder.encode ( cacheEntry.getName (), "UTF-8" ) ) );
                // response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", info.getName () ) );
                ByteStreams.copy ( cacheEntry.getStream (), response.getOutputStream () );
            } );
            return null;
        }
        catch ( final FileNotFoundException e )
        {
            return CommonController.createNotFound ( "channel cache entry", String.format ( "%s:%s", namespace, key ) );
        }
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final Channel channel = (Channel)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                if ( !channel.isLocked () )
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
                result.add ( new MenuEntry ( "Create Channel", 100, LinkTarget.createFromController ( ChannelController.class, "createDetailed" ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Import channel", 200, LinkTarget.createFromController ( ChannelController.class, "importChannel" ), Modifier.DEFAULT, "import" ) );
                result.add ( new MenuEntry ( "Maintenance", 160, "Export all channels", 300, LinkTarget.createFromController ( ChannelController.class, "exportAll" ), Modifier.DEFAULT, "export" ) );
            }

            return result;
        }
        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final Channel channel = (Channel)object;

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", channel.getId () );

            final List<MenuEntry> result = new LinkedList<> ();

            result.add ( new MenuEntry ( "Content", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "List", 120, LinkTarget.createFromController ( ChannelController.class, "viewPlain" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "Details", 200, LinkTarget.createFromController ( ChannelController.class, "details" ).expand ( model ), Modifier.DEFAULT, null ) );

            result.add ( new MenuEntry ( null, -1, "Validation", 210, LinkTarget.createFromController ( ChannelController.class, "viewValidation" ).expand ( model ), Modifier.DEFAULT, null ).setBadge ( channel.getValidationErrorCount () ) );

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
                result.add ( new MenuEntry ( "Help", Integer.MAX_VALUE, "P2 Repository", 1_000, LinkTarget.createFromController ( ChannelController.class, "helpP2" ).expand ( model ), Modifier.DEFAULT, "info-sign" ) );
            }

            return result;
        }
        return null;
    }

    @ControllerValidator ( formDataClass = CreateChannel.class )
    public void validateCreate ( final CreateChannel data, final ValidationContext ctx )
    {
        if ( data.getName () == null )
        {
            return;
        }

        final Channel other = this.service.getChannelWithAlias ( data.getName () );
        if ( other != null )
        {
            ctx.error ( "name", String.format ( "The channel name '%s' is already in use", data.getName () ) );
        }
    }

    @ControllerValidator ( formDataClass = EditChannel.class )
    public void validateEdit ( final EditChannel data, final ValidationContext ctx )
    {
        if ( data.getName () == null )
        {
            return;
        }

        final Channel other = this.service.getChannelWithAlias ( data.getName () );
        if ( other != null && !other.getId ().equals ( data.getId () ) )
        {
            ctx.error ( "name", String.format ( "The channel name '%s' is already in use", data.getName () ) );
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

}
