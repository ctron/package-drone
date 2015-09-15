/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.rpm.web;

import javax.servlet.annotation.HttpConstraint;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.web.utils.Channels;
import de.dentrassi.pm.system.SitePrefixService;

@Controller
@RequestMapping ( value = "/ui/yum" )
@ViewResolver ( "/WEB-INF/views/yum/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class YumController
{
    private ChannelService service;

    private SitePrefixService sitePrefixService;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void setSitePrefixService ( final SitePrefixService sitePrefixService )
    {
        this.sitePrefixService = sitePrefixService;
    }

    @RequestMapping ( value = "/help/{channelId}" )
    public ModelAndView help ( @PathVariable ( "channelId" ) final String channelId)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {
            final ModelAndView model = new ModelAndView ( "help" );

            model.put ( "channel", channel.getInformation () );
            model.put ( "sitePrefix", this.sitePrefixService.getSitePrefix () );

            return model;
        } );
    }
}
