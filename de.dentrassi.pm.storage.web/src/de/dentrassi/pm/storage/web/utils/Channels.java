package de.dentrassi.pm.storage.web.utils;

import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;

import java.util.Optional;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.ReadableChannel;

public final class Channels
{
    private Channels ()
    {
    }

    @FunctionalInterface
    public interface ArtifactOperation<T extends ReadableChannel>
    {
        public ModelAndView process ( T target, ChannelArtifactInformation artifact ) throws Exception;
    }

    public static <T> ModelAndView withChannel ( final ChannelService service, final String channelId, final Class<T> clazz, final ChannelOperation<ModelAndView, T> operation )
    {
        try
        {
            return service.accessCall ( By.id ( channelId ), clazz, operation );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    public static <T extends ReadableChannel> ModelAndView withArtifact ( final ChannelService service, final String channelId, final String artifactId, final Class<T> clazz, final ArtifactOperation<T> operation )
    {
        return Channels.withChannel ( service, channelId, clazz, channel -> {

            final Optional<ChannelArtifactInformation> artifact = channel.getArtifact ( artifactId );
            if ( !artifact.isPresent () )
            {
                return CommonController.createNotFound ( "artifact", artifactId );
            }

            return operation.process ( channel, artifact.get () );
        } );
    }

    public static ModelAndView redirectViewArtifact ( final ChannelArtifactInformation artifact )
    {
        return redirectViewArtifact ( artifact.getChannelId ().getId (), artifact.getId () );
    }

    public static ModelAndView redirectViewArtifact ( final String channelId, final String artifactId )
    {
        return new ModelAndView ( "redirect:/channel/" + urlPathSegmentEscaper ().escape ( channelId ) + "/artifacts/" + urlPathSegmentEscaper ().escape ( artifactId ) + "/view" );
    }

    public static ModelAndView redirectViewChannel ( final ChannelId channel )
    {
        return redirectViewChannel ( channel.getId () );
    }

    public static ModelAndView redirectViewChannel ( final String channelId )
    {
        return new ModelAndView ( "redirect:" + channelTarget ( channelId ) );
    }

    public static String channelTarget ( final String channelId )
    {
        return "/channel/" + urlPathSegmentEscaper ().escape ( channelId ) + "/view";
    }
}
