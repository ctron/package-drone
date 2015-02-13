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
package de.dentrassi.pm.signing.pgp.internal;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.bouncycastle.openpgp.PGPException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.signing.SigningService;

public class Entry
{
    private final static Logger logger = LoggerFactory.getLogger ( Entry.class );

    private final BundleContext context;

    private final String pid;

    private ServiceRegistration<SigningService> handle;

    public Entry ( final String pid, final BundleContext context )
    {
        this.pid = pid;
        this.context = context;
    }

    public void dispose ()
    {
        if ( this.handle != null )
        {
            this.handle.unregister ();
            this.handle = null;
        }

    }

    public void update ( final Dictionary<String, ?> properties )
    {
        dispose ();

        final String keyring = (String)properties.get ( "keyring" );
        final String keyId = (String)properties.get ( "key.id" );
        final String passphrase = (String)properties.get ( "key.passphrase" );

        PgpSigningService service;
        try
        {
            service = PgpSigningService.create ( new File ( keyring ), keyId, passphrase );
        }
        catch ( IOException | PGPException e )
        {
            logger.warn ( "Failed to register signing service", e );
            return;
        }

        // register new service

        final Dictionary<String, Object> serviceProperties = new Hashtable<> ();

        serviceProperties.put ( Constants.SERVICE_PID, this.pid );

        this.handle = this.context.registerService ( SigningService.class, service, serviceProperties );
    }
}
