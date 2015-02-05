/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.web;

import static javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic.PERMIT;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.annotation.HttpConstraint;
import javax.validation.Valid;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;

@Controller
@RequestMapping ( value = "/p2.repo" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class P2Controller
{
    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/{channelId}/info" )
    @Secured ( false )
    @HttpConstraint ( PERMIT )
    public ModelAndView info ( @PathVariable ( "channelId" ) final String channelId ) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final SortedMap<MetaKey, String> metaData = channel.getMetaData ();

        final P2ChannelInformation channelInfo = new P2ChannelInformation ();
        MetaKeys.bind ( channelInfo, metaData );

        model.put ( "channel", channel );
        model.put ( "channelInfo", channelInfo );

        return new ModelAndView ( "p2info", model );
    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.GET )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId ) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final SortedMap<MetaKey, String> metaData = channel.getProvidedMetaData ();

        final P2ChannelInformation channelInfo = new P2ChannelInformation ();

        MetaKeys.bind ( channelInfo, metaData );

        model.put ( "channel", channel );
        model.put ( "command", channelInfo );

        fillBreadcrumbs ( model, channel.getId (), "Edit" );

        return new ModelAndView ( "p2edit", model );
    }

    private void fillBreadcrumbs ( final Map<String, Object> model, final String channelId, final String action )
    {
        model.put ( "breadcrumbs", new Breadcrumbs ( new Entry ( "Home", "/" ), new Entry ( "Channel", "/channel/" + channelId + "/view" ), new Entry ( action ) ) );
    }

    @RequestMapping ( value = "/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final P2ChannelInformation data, final BindingResult result ) throws Exception
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();

        if ( result.hasErrors () )
        {
            model.put ( "channel", channel );
            model.put ( "command", data );
            fillBreadcrumbs ( model, channelId, "Edit" );
            return new ModelAndView ( "p2edit", model );
        }

        final Map<MetaKey, String> providedMetaData = MetaKeys.unbind ( data );

        channel.applyMetaData ( providedMetaData );

        return new ModelAndView ( "redirect:/p2.repo/" + channelId + "/info", model );
    }
}
