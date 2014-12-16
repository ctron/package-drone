/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Part;
import javax.validation.Valid;

import org.osgi.framework.FrameworkUtil;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.aspect.ChannelAspectInformation;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Activator;
import de.dentrassi.pm.storage.web.menu.MenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuManager.MenuEntry;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class ChannelController implements MenuExtender
{

    private static final class ChannelNameComparator implements Comparator<Channel>
    {
        @Override
        public int compare ( final Channel o1, final Channel o2 )
        {
            final String n1 = o1.getName () != null ? o1.getName () : "";
            final String n2 = o2.getName () != null ? o2.getName () : "";

            if ( !n1.isEmpty () && n2.isEmpty () )
            {
                return -1;
            }
            if ( n1.isEmpty () && !n2.isEmpty () )
            {
                return 1;
            }

            final int rc = n1.compareTo ( n2 );
            if ( rc != 0 )
            {
                return rc;
            }

            return o1.getId ().compareTo ( o2.getId () );
        }
    }

    private StorageService service;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( ChannelController.class ).getBundleContext () );

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void start ()
    {
        this.generators.open ();
    }

    public void stop ()
    {
        this.generators.close ();
    }

    private static final List<MenuEntry> menuEntries = Collections.singletonList ( new MenuEntry ( "/channel", "Channels", 10 ) );

    @Override
    public List<MenuEntry> getEntries ()
    {
        return menuEntries;
    }

    private static final ChannelNameComparator NAME_COMPARATOR = new ChannelNameComparator ();

    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
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

        final Channel channel = this.service.createChannel ();

        result.put ( "success", String.format ( "Created channel %s", channel.getId () ) );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    public ModelAndView view ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "channel/view" );

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return new ModelAndView ( "channel/notFound", "channelId", channelId );
        }

        final List<Artifact> sortedArtifacts = new ArrayList<> ( channel.getArtifacts () );
        sortedArtifacts.sort ( Artifact.NAME_COMPARATOR );

        result.put ( "channel", channel );
        result.put ( "sortedArtifacts", sortedArtifacts );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        this.service.deleteChannel ( channelId );
        result.put ( "success", String.format ( "Deleted channel %s", channelId ) );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.GET )
    public ModelAndView add ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView mav = new ModelAndView ( "channel/add" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );

        return mav;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.POST )
    public String addPost ( @PathVariable ( "channelId" )
    final String channelId, @RequestParameter ( required = false, value = "name" ) String name, final @RequestParameter ( "file" ) Part file )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();
        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            service.createArtifact ( channelId, name, file.getInputStream (), null );
        }
        catch ( final IOException e )
        {
            return "channel/uploadError";
        }

        return "redirect:/channel/" + channelId + "/view";
    }

    @RequestMapping ( value = "/channel/{channelId}/clear", method = RequestMethod.GET )
    public String clear ( @PathVariable ( "channelId" )
    final String channelId )
    {
        this.service.clearChannel ( channelId );

        return "redirect:/channel/" + channelId + "/view";
    }

    @RequestMapping ( value = "/channel/{channelId}/aspects", method = RequestMethod.GET )
    public ModelAndView aspects ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView model = new ModelAndView ( "channel/aspects" );

        final ChannelAspectProcessor aspects = Activator.getAspects ();

        final Channel channel = this.service.getChannel ( channelId );
        final Map<String, ChannelAspectInformation> infos = aspects.getAspectInformations ();
        for ( final ChannelAspectInformation ca : channel.getAspects () )
        {
            infos.remove ( ca.getFactoryId () );
        }

        model.put ( "channel", channel );
        model.put ( "addAspects", infos.values () );

        return model;
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" )
    final String channelId, @RequestParameter ( "aspect" )
    final String aspectFactoryId )
    {
        this.service.addChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" )
    final String channelId, @RequestParameter ( "aspect" )
    final String aspectFactoryId )
    {
        this.service.removeChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return new ModelAndView ( "channel/notFound", "channelId", channelId );
        }

        final EditChannel edit = new EditChannel ();
        edit.setId ( channel.getId () );
        edit.setName ( channel.getName () );

        model.put ( "command", edit );

        return new ModelAndView ( "channel/edit", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView edit ( @PathVariable ( "channelId" )
    final String channelId, @Valid
    @FormData ( "command" )
    final EditChannel data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            this.service.updateChannel ( channelId, data.getName () );
            return new ModelAndView ( "redirect:/channel/" + channelId + "/view", model );
        }
        else
        {
            model.put ( "command", data );
            return new ModelAndView ( "channel/edit", model );
        }
    }

}
