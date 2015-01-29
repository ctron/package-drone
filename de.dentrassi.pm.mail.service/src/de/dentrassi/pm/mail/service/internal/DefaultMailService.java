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
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import de.dentrassi.pm.mail.service.MailService;

public class DefaultMailService implements MailService
{
    private Session session;

    private final String username;

    private final String password;

    private final String from;

    private final Properties properties;

    private Authenticator auth;

    private ServiceRegistration<MailService> handle;

    public DefaultMailService ( final String username, final String password, final Properties properties, final String from )
    {
        this.username = username;
        this.password = password;
        this.properties = properties;
        this.from = from;

        this.auth = new Authenticator () {
            @Override
            protected PasswordAuthentication getPasswordAuthentication ()
            {
                return new PasswordAuthentication ( DefaultMailService.this.username, DefaultMailService.this.password );
            }
        };
    }

    public void start ()
    {
        this.session = Session.getInstance ( this.properties, this.auth );
        final BundleContext ctx = FrameworkUtil.getBundle ( DefaultMailService.class ).getBundleContext ();

        final Dictionary<String, ?> props = new Hashtable<> ();
        this.handle = ctx.registerService ( MailService.class, this, props );
    }

    public void stop ()
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }
        this.session = null;
    }

    @Override
    public void sendMessage ( final String to, final String subject, final String text ) throws Exception
    {
        final Message message = new MimeMessage ( this.session );

        message.setFrom ( new InternetAddress ( this.from ) );
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
