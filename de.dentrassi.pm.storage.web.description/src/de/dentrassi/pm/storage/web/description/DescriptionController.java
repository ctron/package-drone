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
package de.dentrassi.pm.storage.web.description;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ChannelNotFoundException;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.ReadableChannel;

@Controller
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class DescriptionController implements InterfaceExtender
{
    private static final MetaKey KEY_DESCRIPTION = new MetaKey ( "sys", "description" );

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/channel/{channelId}/description" )
    public ModelAndView channelDescription ( @PathVariable ( "channelId" ) final String channelId)
    {
        try
        {
            return this.service.accessCall ( By.id ( channelId ), ReadableChannel.class, channel -> {
                final Map<String, Object> model = new HashMap<> ( 1 );
                model.put ( "channel", channel.getInformation () );
                return new ModelAndView ( "description", model );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    @RequestMapping ( value = "/channel/{channelId}/description", method = RequestMethod.POST )
    public ModelAndView channelDescriptionPost ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter ( "data" ) final String data)
    {
        try
        {
            return this.service.accessCall ( By.id ( channelId ), ModifiableChannel.class, channel -> {

                channel.applyMetaData ( Collections.singletonMap ( KEY_DESCRIPTION, data ) );

                final Map<String, Object> model = new HashMap<> ( 1 );
                model.put ( "channel", channel.getInformation () );
                return new ModelAndView ( "description", model );
            } );
        }
        catch ( final ChannelNotFoundException e )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }
    }

    // interface extensions

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( ! ( object instanceof ChannelId ) )
        {
            return null;
        }

        final ChannelId channel = (ChannelId)object;

        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "channelId", channel.getId () );

        result.add ( new MenuEntry ( "Help", 100_000, "Description", 1_000, LinkTarget.createFromController ( DescriptionController.class, "channelDescription" ).expand ( model ), Modifier.DEFAULT, "comment" ) );

        return result;
    }
}
