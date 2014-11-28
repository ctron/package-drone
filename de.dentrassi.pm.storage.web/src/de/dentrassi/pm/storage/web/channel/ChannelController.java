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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import de.dentrassi.pm.aspect.ChannelAspectInformation;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Activator;

@Controller
public class ChannelController
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

    private static final ChannelNameComparator NAME_COMPARATOR = new ChannelNameComparator ();

    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
    public ModelAndView list ()
    {
        final ModelAndView result = new ModelAndView ( "channel/list" );

        final List<Channel> channels = new ArrayList<> ( Activator.getTracker ().getStorageService ().listChannels () );
        channels.sort ( NAME_COMPARATOR );
        result.addObject ( "channels", channels );

        return result;
    }

    @RequestMapping ( value = "/channel/create", method = RequestMethod.GET )
    public ModelAndView create ()
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        final StorageService service = Activator.getTracker ().getStorageService ();

        final Channel channel = service.createChannel ();

        result.addObject ( "success", String.format ( "Created channel %s", channel.getId () ) );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/view", method = RequestMethod.GET )
    public ModelAndView view ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "channel/view" );

        final StorageService service = Activator.getTracker ().getStorageService ();

        final Channel channel = service.getChannel ( channelId );
        if ( channel == null )
        {
            return new ModelAndView ( "channel/notFound", "channelId", channelId );
        }

        final List<Artifact> sortedArtifacts = new ArrayList<> ( channel.getArtifacts () );
        sortedArtifacts.sort ( Artifact.NAME_COMPARATOR );

        result.addObject ( "channel", channel );
        result.addObject ( "sortedArtifacts", sortedArtifacts );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView result = new ModelAndView ( "redirect:/channel" );

        final StorageService service = Activator.getTracker ().getStorageService ();

        service.deleteChannel ( channelId );
        result.addObject ( "success", String.format ( "Deleted channel %s", channelId ) );

        return result;
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.GET )
    public String add ( @PathVariable ( "channelId" )
    final String channelId )
    {
        return "channel/add";
    }

    @RequestMapping ( value = "/channel/{channelId}/add", method = RequestMethod.POST )
    public String addPost ( @PathVariable ( "channelId" )
    final String channelId, @RequestParam ( required = false, value = "name" ) String name, final @RequestParam ( "file" ) MultipartFile file )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();
        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getOriginalFilename ();
            }

            service.createArtifact ( channelId, name, file.getInputStream () );
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
        final StorageService service = Activator.getTracker ().getStorageService ();
        service.clearChannel ( channelId );

        return "redirect:/channel/" + channelId + "/view";
    }

    @RequestMapping ( value = "/channel/{channelId}/aspects", method = RequestMethod.GET )
    public ModelAndView aspects ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final ModelAndView model = new ModelAndView ( "channel/aspects" );

        final StorageService service = Activator.getTracker ().getStorageService ();
        final ChannelAspectProcessor aspects = Activator.getAspects ();

        final Channel channel = service.getChannel ( channelId );
        final Map<String, ChannelAspectInformation> infos = aspects.getAspectInformations ();
        for ( final ChannelAspectInformation ca : channel.getAspects () )
        {
            infos.remove ( ca.getFactoryId () );
        }

        model.addObject ( "channel", channel );
        model.addObject ( "addAspects", infos.values () );

        return model;
    }

    @RequestMapping ( value = "/channel/{channelId}/addAspect", method = RequestMethod.POST )
    public ModelAndView addAspect ( @PathVariable ( "channelId" )
    final String channelId, @RequestParam ( "aspect" )
    final String aspectFactoryId )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();
        service.addChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/removeAspect", method = RequestMethod.POST )
    public ModelAndView removeAspect ( @PathVariable ( "channelId" )
    final String channelId, @RequestParam ( "aspect" )
    final String aspectFactoryId )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();
        service.removeChannelAspect ( channelId, aspectFactoryId );
        return new ModelAndView ( String.format ( "redirect:aspects", channelId ) );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" )
    final String channelId )
    {
        final Map<String, Object> model = new HashMap<> ();

        final StorageService service = Activator.getTracker ().getStorageService ();

        final Channel channel = service.getChannel ( channelId );
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
    @ModelAttribute ( "command" )
    final EditChannel data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !result.hasErrors () )
        {
            final StorageService service = Activator.getTracker ().getStorageService ();
            service.updateChannel ( channelId, data.getName () );
            return new ModelAndView ( "redirect:/channel/" + channelId + "/view", model );
        }
        else
        {
            model.put ( "command", data );
            return new ModelAndView ( "channel/edit", model );
        }
    }

}
