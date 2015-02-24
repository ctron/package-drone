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
package de.dentrassi.pm.deb.web;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
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
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.deb.ChannelConfiguration;
import de.dentrassi.pm.deb.aspect.AptChannelAspectFactory;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.signing.SigningService;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;

@Controller
@ViewResolver ( "/WEB-INF/views/config/%s.jsp" )
@Secured
@RequestMapping ( "/config/deb" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final List<MenuEntry> result = new LinkedList<> ();
            final Channel channel = (Channel)object;

            if ( channel.hasAspect ( AptChannelAspectFactory.ID ) )
            {
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );

                result.add ( new MenuEntry ( "APT Repository", 1_500, new LinkTarget ( "/apt/" + channel.getId () ), Modifier.LINK, null ) );
            }

            return result;
        }

        return null;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            final List<MenuEntry> result = new LinkedList<> ();

            final Channel channel = (Channel)object;

            if ( channel.hasAspect ( AptChannelAspectFactory.ID ) )
            {
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );

                if ( request.isUserInRole ( "MANAGER" ) )
                {
                    result.add ( new MenuEntry ( "APT", 1_500, LinkTarget.createFromController ( ConfigController.class, "edit" ).expand ( model ), null, null ) );
                }
            }

            return result;
        }

        return null;
    }

    public static class SigningServiceEntry implements Comparable<SigningServiceEntry>
    {
        private final String id;

        private final String label;

        public SigningServiceEntry ( final String id, final String label )
        {
            this.id = id;
            this.label = label == null ? id : label;
        }

        public String getId ()
        {
            return this.id;
        }

        public String getLabel ()
        {
            return this.label;
        }

        @Override
        public int compareTo ( final SigningServiceEntry o )
        {
            return this.label.compareTo ( o.label );
        }

        @Override
        public String toString ()
        {
            return String.format ( "%s (%s)", this.label, this.id );
        }
    }

    @RequestMapping ( "/channel/{channelId}/edit" )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId ) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        model.put ( "channel", channel );
        model.put ( "signingServices", getSigningServices () );

        final ChannelConfiguration dist = new ChannelConfiguration ();

        dist.setDistribution ( "default" );
        dist.setDefaultComponent ( "main" );
        dist.getArchitectures ().add ( "i386" );
        dist.getArchitectures ().add ( "amd64" );

        MetaKeys.bind ( dist, channel.getMetaData () );

        model.put ( "command", dist );

        return new ModelAndView ( "edit", model );
    }

    @RequestMapping ( value = "/channel/{channelId}/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final ChannelConfiguration cfg, final BindingResult result ) throws Exception
    {
        final Map<String, Object> model = new HashMap<> ();

        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        if ( !result.hasErrors () )
        {
            final Map<MetaKey, String> md = MetaKeys.unbind ( cfg );
            channel.applyMetaData ( md );
        }

        model.put ( "channel", channel );
        model.put ( "signingServices", getSigningServices () );

        return new ModelAndView ( "edit", model );
    }

    private List<SigningServiceEntry> getSigningServices ()
    {
        final List<SigningServiceEntry> result = new LinkedList<> ();

        final BundleContext ctx = FrameworkUtil.getBundle ( ConfigController.class ).getBundleContext ();
        Collection<ServiceReference<SigningService>> refs;
        try
        {
            refs = ctx.getServiceReferences ( SigningService.class, null );
        }
        catch ( final InvalidSyntaxException e )
        {
            return Collections.emptyList ();
        }

        if ( refs != null )
        {
            for ( final ServiceReference<SigningService> ref : refs )
            {
                final String pid = makeString ( ref.getProperty ( Constants.SERVICE_PID ) );
                final String description = makeString ( ref.getProperty ( Constants.SERVICE_DESCRIPTION ) );
                result.add ( new SigningServiceEntry ( pid, description ) );
            }
        }

        return result;
    }

    private String makeString ( final Object property )
    {
        if ( property instanceof String )
        {
            return (String)property;
        }
        return null;
    }
}
