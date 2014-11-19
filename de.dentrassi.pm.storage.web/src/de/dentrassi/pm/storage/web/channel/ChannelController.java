package de.dentrassi.pm.storage.web.channel;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.dentrassi.pm.storage.service.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Activator;

@Controller
public class ChannelController
{
    @RequestMapping ( value = "/channel", method = RequestMethod.GET )
    public ModelAndView list ()
    {
        final ModelAndView result = new ModelAndView ( "channel/list" );

        result.addObject ( "channels", Activator.getTracker ().getStorageService ().listChannels () );

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

        result.addObject ( "channel", channel );

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
}
