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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.GeneratorArtifact;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.service.util.DownloadHelper;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class ArtifactController
{
    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/artifact/{artifactId}/get", method = RequestMethod.GET )
    public void get ( final HttpServletResponse response, @PathVariable ( "artifactId" ) final String artifactId )
    {
        DownloadHelper.streamArtifact ( response, this.service, artifactId, DownloadHelper.APPLICATION_OCTET_STREAM, true );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/dump", method = RequestMethod.GET )
    public void dump ( final HttpServletResponse response, @PathVariable ( "artifactId" ) final String artifactId )
    {
        DownloadHelper.streamArtifact ( response, this.service, artifactId, null, false );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/delete", method = RequestMethod.GET )
    public ModelAndView delete ( @PathVariable ( "artifactId" ) final String artifactId )
    {
        final ArtifactInformation info = this.service.deleteArtifact ( artifactId );
        if ( info == null )
        {
            return new ModelAndView ( "redirect:/" );
        }

        return new ModelAndView ( "redirect:/channel/" + info.getChannelId () + "/view" );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/view", method = RequestMethod.GET )
    public ModelAndView view ( @PathVariable ( "artifactId" ) final String artifactId )
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );

        final Map<String, Object> model = new HashMap<String, Object> ();
        model.put ( "artifact", artifact );

        return new ModelAndView ( "artifact/view", model );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/generate", method = RequestMethod.GET )
    public ModelAndView generate ( @PathVariable ( "artifactId" ) final String artifactId )
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );
        if ( artifact instanceof GeneratorArtifact )
        {
            ( (GeneratorArtifact)artifact ).generate ();
        }

        return new ModelAndView ( "redirect:/channel/" + artifact.getChannel ().getId () + "/view" );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/attach", method = RequestMethod.GET )
    public ModelAndView attach ( @PathVariable ( "artifactId" ) final String artifactId )
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );

        return new ModelAndView ( "/artifact/attach", "artifact", artifact );
    }

    @RequestMapping ( value = "/artifact/{artifactId}/attach", method = RequestMethod.POST )
    public ModelAndView attachPost ( @PathVariable ( "artifactId" ) final String artifactId, @RequestParameter ( required = false,
            value = "name" ) String name, final @RequestParameter ( "file" ) Part file )
    {
        Artifact artifact;
        try
        {
            if ( name == null || name.isEmpty () )
            {
                name = file.getSubmittedFileName ();
            }

            artifact = this.service.createAttachedArtifact ( artifactId, name, file.getInputStream (), null );
        }
        catch ( final IOException e )
        {
            return new ModelAndView ( "/error/upload" );
        }

        return new ModelAndView ( "redirect:/channel/" + artifact.getChannel ().getId () + "/view" );
    }
}
