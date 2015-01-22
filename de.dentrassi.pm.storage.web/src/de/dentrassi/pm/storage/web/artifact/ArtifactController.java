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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.SimpleArtifactInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.GeneratorArtifact;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.service.util.DownloadHelper;
import de.dentrassi.pm.storage.web.InterfaceExtender;
import de.dentrassi.pm.storage.web.Modifier;
import de.dentrassi.pm.storage.web.channel.ChannelController;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class ArtifactController implements InterfaceExtender
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
        final SimpleArtifactInformation info = this.service.deleteArtifact ( artifactId );
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

        final Map<String, Object> model = new HashMap<String, Object> ( 1 );
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

        return new ModelAndView ( "/artifact/attach", "artifact", artifact.getInformation () );
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

    @Override
    public List<MenuEntry> getActions ( final Object object )
    {
        if ( object instanceof Artifact )
        {
            final Artifact art = (Artifact)object;
            final ArtifactInformation ai = art.getInformation ();

            final List<MenuEntry> result = new LinkedList<> ();

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", ai.getChannelId () );
            model.put ( "artifactId", ai.getId () );

            result.add ( new MenuEntry ( "Channel", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );

            if ( ai.is ( "parentable" ) )
            {
                result.add ( new MenuEntry ( "Attach Artifact", 200, LinkTarget.createFromController ( ArtifactController.class, "attach" ).expand ( model ), Modifier.PRIMARY, null ) );
            }
            if ( ai.is ( "generator" ) )
            {
                result.add ( new MenuEntry ( "Regenerate", 300, LinkTarget.createFromController ( ArtifactController.class, "generate" ).expand ( model ), Modifier.SUCCESS, "refresh" ) );
            }
            if ( ai.is ( "deleteable" ) )
            {
                result.add ( new MenuEntry ( "Delete", 1000, LinkTarget.createFromController ( ArtifactController.class, "delete" ).expand ( model ), Modifier.DANGER, "trash" ) );
            }
            if ( art instanceof GeneratorArtifact )
            {
                final GeneratorArtifact genart = (GeneratorArtifact)art;

                if ( genart.getEditTarget () != null )
                {
                    result.add ( new MenuEntry ( "Edit", 400, genart.getEditTarget (), Modifier.DEFAULT, null ) );
                }
            }

            result.add ( new MenuEntry ( "Download", Integer.MAX_VALUE, LinkTarget.createFromController ( ArtifactController.class, "dump" ).expand ( model ), Modifier.LINK, null ) );

            return result;
        }
        return null;
    }
}
