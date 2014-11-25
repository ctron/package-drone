package de.dentrassi.pm.storage.web.artifact;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.ArtifactInformation;
import de.dentrassi.pm.storage.service.MetaKey;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Activator;

@Controller
public class ArtifactController
{
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    @RequestMapping ( value = "/artifact/{artifactId}/get", method = RequestMethod.GET )
    public void get ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        streamArtifact ( response, artifactId, APPLICATION_OCTET_STREAM, true );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/dump", method = RequestMethod.GET )
    public void dump ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        streamArtifact ( response, artifactId, null, false );
    }

    protected void streamArtifact ( final HttpServletResponse response, final String artifactId, final String mimetype, final boolean download )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();

        try
        {
            service.streamArtifact ( artifactId, ( info, stream ) -> {

                String mt = mimetype;
                if ( mt == null )
                {
                    mt = getMimeType ( service.getArtifact ( artifactId ) );
                }
                response.setContentType ( mt );

                try
                {
                    response.setContentLengthLong ( info.getLength () );
                    if ( download )
                    {
                        response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", info.getName () ) );
                    }
                    final long size = ByteStreams.copy ( stream, response.getOutputStream () );
                    System.out.format ( "%s bytes copied%n", size );
                }
                catch ( final Exception e )
                {
                    throw new RuntimeException ( e );
                }
            } );
        }
        catch ( final FileNotFoundException e )
        {
            response.setStatus ( HttpServletResponse.SC_NOT_FOUND );
        }
    }

    private String getMimeType ( final Artifact artifact )
    {
        final String mimetype = artifact.getMetaData ().get ( new MetaKey ( "mime", "type" ) );
        return mimetype == null ? APPLICATION_OCTET_STREAM : mimetype;
    }

    @RequestMapping ( value = "/artifact/{artifactId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "artifactId" )
    final String artifactId )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();

        final ArtifactInformation info = service.deleteArtifact ( artifactId );

        return new ModelAndView ( "redirect:/channel/" + info.getChannelId () + "/view" );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/view", method = RequestMethod.GET )
    public ModelAndView view ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();

        final Artifact artifact = service.getArtifact ( artifactId );

        final Map<String, Object> model = new HashMap<String, Object> ();
        model.put ( "artifact", artifact );

        return new ModelAndView ( "artifact/view", model );
    }
}
