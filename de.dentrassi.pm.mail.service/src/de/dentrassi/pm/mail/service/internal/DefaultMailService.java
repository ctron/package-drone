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
package de.dentrassi.pm.mail.service.internal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.osgi.service.component.ComponentContext;

import de.dentrassi.pm.mail.service.MailService;

public class DefaultMailService implements MailService
{
    private static final String PROPERTY_PREFIX = "property.";

    private Session session;

    private Dictionary<String, Object> config;

    public DefaultMailService ()
    {
    }

    public void updated ( final ComponentContext context )
    {
        stop ();
        start ( context );
    }

    public void start ( final ComponentContext context )
    {
        this.config = context.getProperties ();

        final String username = getString ( "username" );
        final String password = getString ( "password" );

        final Properties properties = new Properties ();

        final Enumeration<String> keys = this.config.keys ();
        while ( keys.hasMoreElements () )
        {
            final String key = keys.nextElement ();
            if ( key.startsWith ( PROPERTY_PREFIX ) )
            {
                properties.put ( key.substring ( PROPERTY_PREFIX.length () ), properties.get ( key ) );
            }
        }

        Authenticator auth = null;
        if ( username != null && password != null )
        {
            auth = new Authenticator () {
                @Override
                protected PasswordAuthentication getPasswordAuthentication ()
                {
                    return new PasswordAuthentication ( username, password );
                }
            };
        }

        this.session = Session.getInstance ( properties, auth );
    }

    private String getString ( final String key )
    {
        final Object val = this.config.get ( key );
        if ( val != null )
        {
            return val.toString ();
        }
        return null;
    }

    public void stop ()
    {
        this.session = null;
    }

    @Override
    public void sendMessage ( final String to, final String subject, final String text ) throws Exception
    {
        final Message message = new MimeMessage ( this.session );

        final String from = getString ( "from" );
        if ( from != null )
        {
            message.setFrom ( new InternetAddress ( from ) );
        }
        else
        {
            message.setFrom ();
        }

        message.setSubject ( subject );
        message.setText ( text );
        message.setHeader ( "Return-Path", "<>" );

        // commit

        message.saveChanges ();

        // connect

        final Transport transport = this.session.getTransport ();
        transport.connect ();

        // send

        try
        {
            transport.sendMessage ( message, message.getAllRecipients () );
        }
        finally
        {
            // close

            transport.close ();
        }
    }
}
