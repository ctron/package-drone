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
package de.dentrassi.pm.mail.web;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.mail.service.java.DefaultMailService;
import de.dentrassi.pm.sec.DatabaseDetails;
import de.dentrassi.pm.sec.UserInformation;
import de.dentrassi.pm.sec.UserInformationPrincipal;
import de.dentrassi.pm.sec.web.controller.HttpConstraints;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;

@Controller
@RequestMapping ( "/default.mail/config" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private final static Logger logger = LoggerFactory.getLogger ( ConfigController.class );

    private ConfigurationAdmin admin;

    private volatile DefaultMailService mailService;

    private static final Method METHOD_INDEX = LinkTarget.getControllerMethod ( ConfigController.class, "index" );

    public void setMailService ( final DefaultMailService mailService )
    {
        this.mailService = mailService;
    }

    public void unsetMailService ( final DefaultMailService mailService )
    {
        this.mailService = null;
    }

    public void setAdmin ( final ConfigurationAdmin admin )
    {
        this.admin = admin;
    }

    @RequestMapping
    public ModelAndView index ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", getCurrent () );

        fillModel ( model );

        return new ModelAndView ( "index", model );
    }

    private void fillModel ( final Map<String, Object> model )
    {
        model.put ( "servicePresent", this.mailService != null );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView update ( @Valid @FormData ( "command" ) final MailSettings settings, final BindingResult bindingResult )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( !bindingResult.hasErrors () )
        {
            setCurrent ( settings );
        }

        // poor man's wait

        int i = 10;
        while ( this.mailService == null && i > 0 )
        {
            try
            {
                Thread.sleep ( 100 );
            }
            catch ( final InterruptedException e )
            {
                break;
            }
            i--;
        }

        fillModel ( model );

        return new ModelAndView ( "index", model );
    }

    protected void setCurrent ( final MailSettings settings )
    {
        try
        {
            final Configuration cfg = this.admin.getConfiguration ( DefaultMailService.SERVICE_PID );

            final Dictionary<String, Object> properties = new Hashtable<> ();

            put ( properties, "username", settings.getUsername () );
            put ( properties, "password", settings.getPassword () );
            put ( properties, "from", settings.getFrom () );
            put ( properties, "prefix", settings.getPrefix () );
            put ( properties, DefaultMailService.PROPERTY_PREFIX + "mail.transport.protocol", "smtp" );
            put ( properties, DefaultMailService.PROPERTY_PREFIX + "mail.smtp.host", settings.getHost () );
            put ( properties, DefaultMailService.PROPERTY_PREFIX + "mail.smtp.port", settings.getPort () );
            if ( settings.isEnableStartTls () )
            {
                put ( properties, DefaultMailService.PROPERTY_PREFIX + "mail.smtp.starttls.enable", "true" );
            }

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
            final Configuration cfg = this.admin.getConfiguration ( DefaultMailService.SERVICE_PID );
            if ( cfg == null || cfg.getProperties () == null )
            {
                return createDefault ();
            }

            final MailSettings result = new MailSettings ();

            result.setUsername ( getString ( cfg, "username" ) );
            result.setPassword ( getString ( cfg, "password" ) );
            result.setFrom ( getString ( cfg, "from" ) );
            result.setPrefix ( getString ( cfg, "prefix" ) );
            result.setHost ( getString ( cfg, DefaultMailService.PROPERTY_PREFIX + "mail.smtp.host" ) );
            result.setPort ( getInteger ( cfg, DefaultMailService.PROPERTY_PREFIX + "mail.smtp.port" ) );

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

        if ( HttpConstraints.isCallAllowed ( METHOD_INDEX, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 100, "Mail", 700, LinkTarget.createFromController ( METHOD_INDEX ), null, null ) );
        }

        return result;
    }

    @RequestMapping ( value = "/sendTest", method = RequestMethod.POST )
    public ModelAndView sendTest ( @RequestParameter ( "testEmailReceiver" ) final String email, final Principal principal )
    {
        final Map<String, Object> model = new HashMap<> ();

        try
        {
            String user = principal.getName ();

            if ( principal instanceof UserInformationPrincipal )
            {
                final UserInformation userInfo = ( (UserInformationPrincipal)principal ).getUserInformation ();
                final DatabaseDetails dbDetails = userInfo.getDetails ( DatabaseDetails.class );
                if ( dbDetails != null && dbDetails.getEmail () != null )
                {
                    user = dbDetails.getEmail ();
                }
            }

            this.mailService.sendMessage ( email, "Test Mail", "This is an automated test message requested by: " + user );
        }
        catch ( final Throwable e )
        {
            logger.warn ( "Failed to send test e-mail", e );
            return CommonController.createError ( "Test Mail", "Result", null, e, true );
        }

        return new ModelAndView ( "testSent", model );
    }
}
