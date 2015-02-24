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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;

import org.eclipse.scada.utils.ExceptionHelper;
import org.osgi.framework.FrameworkUtil;

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
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
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

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ChannelController implements InterfaceExtender
{
    private StorageService service;

    private DeployAuthService deployAuthService;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( ChannelController.class ).getBundleContext () );

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void setDeployAuthService ( final DeployAuthService deployAuthService )
    {
        this.deployAuthService = deployAuthService;
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

    private static final ChannelNameComparator NAME_COMPARATOR = new ChannelNameComparator ();

    @Secured ( false )
    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView list ()
    {
        final ModelAndView result = new ModelAndView ( "channel/list" );

        final List<Channel> channels = new ArrayList<> ( this.service.listChannels () );
        channels.sort ( NAME_COMPARATOR );
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

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView view ( @PathVariable ( "channelId" ) final String channelId )
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
    public ModelAndView tree ( @PathVariable ( "channelId" ) final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "channel/tree" );

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<Object, List<SimpleArtifactInformation>> tree = new HashMap<> ();

        for ( final SimpleArtifactInformation entry : channel.getSimpleArtifacts () )
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

        return result;
    }

    @Secured ( false )
    @RequestMapping ( value = "/channel/{channelId}/details", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView details ( @PathVariable ( "channelId" ) final String channelId )
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
    public ModelAndView delete ( @PathVariable ( "channelId" ) final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        this.service.deleteChannel ( channelId );
        result.put ( "success", String.format ( "Deleted channel %s", channelId ) );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.GET )
    public ModelAndView add ( @PathVariable ( "channelId" ) final String channelId )
    {
        final ModelAndView mav = new ModelAndView ( "/channel/add" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );

        return mav;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.POST )
    public ModelAndView addPost ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( required = false,
            value = "name" ) String name, final @RequestParameter ( "file" ) Part file )
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

        return redirectDefaultView ( channelId );
    }

    @RequestMapping ( value = "/channel/{channelId}/drop", method = RequestMethod.POST )
    public void drop ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( required = false,
            value = "name" ) String name, final @RequestParameter ( "file" ) Part file, final HttpServletResponse response ) throws IOException
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
    public ModelAndView clear ( @PathVariable ( "channelId" ) final String channelId )
    {
        this.service.clearChannel ( channelId );

        return redirectDefaultView ( channelId );
    }

    protected ModelAndView redirectDefaultView ( final String channelId )
    {
        return new ModelAndView ( "redirect:/channel/" + channelId + "/view" );
    }

    @RequestMapping ( value = "/channel/{channelId}/deployKeys" )
    public ModelAndView deployKeys ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "channel", channel );
        model.put ( "deployGroups", getGroupsForChannel ( channel ) );

        return new ModelAndView ( "channel/deployKeys", model );
    }

    protected List<DeployGroup> getGroupsForChannel ( final Channel channel )
    {
        final List<DeployGroup> groups = this.deployAuthService.listGroups ( 0, -1 );
        groups.removeAll ( channel.getDeployGroups () );
        Collections.sort ( groups, DeployGroup.NAME_COMPARATOR );
        return groups;
    }

    @RequestMapping ( value = "/channel/{channelId}/addDeployGroup", method = RequestMethod.POST )
    public ModelAndView addDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId )
    {
        return modifyDeployGroup ( channelId, groupId, Channel::addDeployGroup );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeDeployGroup", method = RequestMethod.POST )
    public ModelAndView removeDeployGroup ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "groupId" ) final String groupId )
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
    public ModelAndView aspects ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final ModelAndView model = new ModelAndView ( "channel/aspects" );

        final ChannelAspectProcessor aspects = Activator.getAspects ();

        final Map<String, ChannelAspectInformation> infos = aspects.getAspectInformations ();
        for ( final ChannelAspectInformation ca : channel.getAspects () )
        {
            infos.remove ( ca.getFactoryId () );
        }

        final ArrayList<ChannelAspectInformation> addAspects = new ArrayList<> ( infos.values () );
        Collections.sort ( addAspects, ( o1, o2 ) -> o1.getLabel ().compareTo ( o2.getLabel () ) );

        model.put ( "channel", channel );
        model.put ( "addAspects", addAspects );

        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Aspects" ) ) );

        return model;
    }

    @RequestMapping ( value = "/channel/{channelId}/lock", method = RequestMethod.GET )
    public ModelAndView lock ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        channel.lock ();

        return redirectDefaultView ( channelId );
    }

    @RequestMapping ( value = "/channel/{channelId}/unlock", method = RequestMethod.GET )
    public ModelAndView unlock ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        channel.unlock ();

        return redirectDefaultView ( channelId );
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId )
    {
        this.service.addChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId )
    {
        this.service.removeChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/refreshAspect", method = RequestMethod.POST )
    public ModelAndView refreshAspect ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "aspect" ) final String aspectFactoryId )
    {
        this.service.refreshChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId )
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

        model.put ( "command", edit );

        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), Breadcrumbs.create ( "Channel", ChannelController.class, "view", "channelId", channelId ), new Entry ( "Edit" ) ) );

        return new ModelAndView ( "channel/edit", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final EditChannel data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            this.service.updateChannel ( channelId, data.getName () );
            return redirectDefaultView ( channelId );
        }
        else
        {
            model.put ( "command", data );
            return new ModelAndView ( "channel/edit", model );
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
                    result.add ( new MenuEntry ( "Delete Channel", 400, LinkTarget.createFromController ( ChannelController.class, "delete" ).expand ( model ), Modifier.DANGER, "trash" ) );
                    result.add ( new MenuEntry ( "Clear Channel", 500, LinkTarget.createFromController ( ChannelController.class, "clear" ).expand ( model ), Modifier.WARNING, null ) );

                    result.add ( new MenuEntry ( "Lock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "lock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }
                else
                {
                    result.add ( new MenuEntry ( "Unlock Channel", 600, LinkTarget.createFromController ( ChannelController.class, "unlock" ).expand ( model ), Modifier.DEFAULT, null ) );
                }

                result.add ( new MenuEntry ( "Edit", 150, "Edit Channel", 200, LinkTarget.createFromController ( ChannelController.class, "edit" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            if ( request.getRemoteUser () != null )
            {
                result.add ( new MenuEntry ( "Edit", 150, "Configure Aspects", 300, LinkTarget.createFromController ( ChannelController.class, "aspects" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            return result;
        }
        else if ( Tags.ACTION_TAG_CHANNELS.equals ( object ) )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                result.add ( new MenuEntry ( "Create Channel", 100, LinkTarget.createFromController ( ChannelController.class, "create" ), Modifier.PRIMARY, null ) );
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

            result.add ( new MenuEntry ( "List", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "Tree", 120, LinkTarget.createFromController ( ChannelController.class, "tree" ).expand ( model ), Modifier.DEFAULT, null ) );
            result.add ( new MenuEntry ( "Details", 200, LinkTarget.createFromController ( ChannelController.class, "details" ).expand ( model ), Modifier.DEFAULT, null ) );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                result.add ( new MenuEntry ( "Deploy Keys", 1000, LinkTarget.createFromController ( ChannelController.class, "deployKeys" ).expand ( model ), Modifier.DEFAULT, null ) );
            }

            return result;
        }
        return null;
    }

}
