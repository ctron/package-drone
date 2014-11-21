package de.dentrassi.pm.storage.web.artifact;

import java.io.FileNotFoundException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.Activator;

@Controller
public class ArtifactController
{
    @RequestMapping ( value = "/artifact/{artifactId}/get", method = RequestMethod.GET )
    public void get ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        final StorageService service = Activator.getTracker ().getStorageService ();

        try
        {
            service.streamArtifact ( artifactId, ( info, stream ) -> {
                response.setContentType ( "application/octet-stream" );
                try
                {
                    response.setContentLengthLong ( info.getLength () );
                    response.setHeader ( "Content-Disposition", String.format ( "attachment; filename=%s", info.getName () ) );
                    ByteStreams.copy ( stream, response.getOutputStream () );
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
}
