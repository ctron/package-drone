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
package de.dentrassi.pm.storage.web.artifact;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.dentrassi.pm.storage.ArtifactInformation;
import de.dentrassi.pm.storage.service.Artifact;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.service.util.DownloadHelper;
import de.dentrassi.pm.storage.web.Activator;

@Controller
public class ArtifactController
{
    @RequestMapping ( value = "/artifact/{artifactId}/get", method = RequestMethod.GET )
    public void get ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        DownloadHelper.streamArtifact ( response, Activator.getTracker ().getStorageService (), artifactId, DownloadHelper.APPLICATION_OCTET_STREAM, true );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/dump", method = RequestMethod.GET )
    public void dump ( final HttpServletResponse response, @PathVariable ( "artifactId" )
    final String artifactId )
    {
        DownloadHelper.streamArtifact ( response, Activator.getTracker ().getStorageService (), artifactId, null, false );
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
