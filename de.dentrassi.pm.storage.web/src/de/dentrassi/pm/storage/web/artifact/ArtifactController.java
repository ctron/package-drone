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
package de.dentrassi.pm.storage.web.artifact;

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.sec.web.filter.SecurityFilter;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.GeneratorArtifact;
import de.dentrassi.pm.storage.channel.ChannelArtifactInformation;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.channel.ChannelController;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ArtifactController implements InterfaceExtender
{
    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/artifact/{artifactId}/generate", method = RequestMethod.GET )
    @HttpConstraint ( PERMIT )
    public ModelAndView generate ( @PathVariable ( "artifactId" ) final String artifactId)
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );
        if ( artifact == null )
        {
            return CommonController.createNotFound ( "artifact", artifactId );
        }

        if ( artifact instanceof GeneratorArtifact )
        {
            ( (GeneratorArtifact)artifact ).generate ();
        }

        return new ModelAndView ( "redirect:/channel/" + artifact.getChannel ().getId () + "/view" );
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof de.dentrassi.pm.storage.channel.ArtifactInformation )
        {
            final ChannelArtifactInformation ai = (ChannelArtifactInformation)object;

            final List<MenuEntry> result = new LinkedList<> ();

            final Map<String, Object> model = new HashMap<> ( 1 );
            model.put ( "channelId", ai.getChannelId ().getId () );
            model.put ( "artifactId", ai.getId () );

            result.add ( new MenuEntry ( "Channel", 100, LinkTarget.createFromController ( ChannelController.class, "view" ).expand ( model ), Modifier.DEFAULT, null ) );

            if ( request.isUserInRole ( "MANAGER" ) )
            {
                /* FIXME:
                if ( art instanceof GeneratorArtifact )
                {
                    final GeneratorArtifact genart = (GeneratorArtifact)art;
                
                    if ( genart.getEditTarget () != null )
                    {
                        result.add ( new MenuEntry ( "Edit", 400, genart.getEditTarget (), Modifier.DEFAULT, null ) );
                    }
                }
                */
            }

            if ( SecurityFilter.isLoggedIn ( request ) )
            {
                if ( ai.is ( "generator" ) )
                {
                    result.add ( new MenuEntry ( "Regenerate", 300, LinkTarget.createFromController ( ArtifactController.class, "generate" ).expand ( model ), Modifier.SUCCESS, "refresh" ) );
                }
            }

            result.add ( new MenuEntry ( "Download", Integer.MAX_VALUE, LinkTarget.createFromController ( ChannelController.class, "getArtifact" ).expand ( model ), Modifier.LINK, "download" ) );
            result.add ( new MenuEntry ( "View", Integer.MAX_VALUE, LinkTarget.createFromController ( ChannelController.class, "dumpArtifact" ).expand ( model ), Modifier.LINK, null ) );

            return result;
        }
        return null;
    }
}
