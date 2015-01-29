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
package de.dentrassi.pm.mail.web;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.sec.web.filter.SecurityFilter;
import de.dentrassi.pm.storage.web.InterfaceExtender;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

@Controller
@RequestMapping ( "/default.mail/config" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{

    private ConfigurationAdmin admin;

    public void setAdmin ( final ConfigurationAdmin admin )
    {
        this.admin = admin;
    }

    @RequestMapping
    public ModelAndView index ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", getCurrent () );

        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView update ( @Valid @FormData ( "command" ) final MailSettings settings, final BindingResult bindingResult )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !bindingResult.hasErrors () )
        {
            setCurrent ( settings );
        }

        return new ModelAndView ( "index", model );
    }

    protected void setCurrent ( final MailSettings settings )
    {
        try
        {
            final Configuration cfg = this.admin.getConfiguration ( "de.dentrassi.pm.mail.service.default" );

            final Dictionary<String, Object> properties = new Hashtable<> ();

            put ( properties, "username", settings.getUsername () );
            put ( properties, "password", settings.getPassword () );
            put ( properties, "properties.mail.transport.protocol", "smtp" );
            put ( properties, "properties.mail.smtp.host", settings.getHost () );
            put ( properties, "properties.mail.smtp.port", settings.getPort () );

            cfg.update ( properties );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to update mail server configuration", e );
        }
    }

    private void put ( final Dictionary<String, Object> properties, final String key, final Object value )
    {
        if ( value instanceof String && ( (String)value ).isEmpty () )
        {
            return;
        }

        if ( value != null )
        {
            properties.put ( key, value );
        }
    }

    protected MailSettings getCurrent ()
    {
        try
        {
            final Configuration cfg = this.admin.getConfiguration ( "de.dentrassi.pm.mail.service.defaultProvider", "" );
            if ( cfg == null || cfg.getProperties () == null )
            {
                return createDefault ();
            }

            final MailSettings result = new MailSettings ();

            result.setUsername ( getString ( cfg, "username" ) );
            result.setPassword ( getString ( cfg, "password" ) );
            result.setHost ( getString ( cfg, "properties.mail.smtp.host" ) );
            result.setPort ( getInteger ( cfg, "properties.mail.smtp.port" ) );

            return result;
        }
        catch ( final IOException e )
        {
            return createDefault ();
        }
    }

    private Integer getInteger ( final Configuration cfg, final String key )
    {
        final Object val = cfg.getProperties ().get ( key );
        if ( val instanceof Number )
        {
            return ( (Number)val ).intValue ();
        }

        if ( val instanceof String )
        {
            try
            {
                return Integer.parseInt ( val.toString () );
            }
            catch ( final NumberFormatException e )
            {
                return null;
            }
        }

        return null;
    }

    private String getString ( final Configuration cfg, final String key )
    {
        final Object val = cfg.getProperties ().get ( key );
        if ( val != null )
        {
            return val.toString ();
        }
        return null;
    }

    protected MailSettings createDefault ()
    {
        final MailSettings result = new MailSettings ();
        return result;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( SecurityFilter.isLoggedIn ( request ) )
        {
            result.add ( new MenuEntry ( "Administration", 100, "Mail", 700, LinkTarget.createFromController ( ConfigController.class, "index" ), null, null ) );
        }

        return result;
    }
}
