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
package de.dentrassi.pm.importer.aether;

import java.nio.file.Path;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.DefaultServiceLocator.ErrorHandler;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper
{

    public static RepositorySystem newRepositorySystem ()
    {
        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator ();

        locator.addService ( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService ( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService ( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler ( new ErrorHandler () {
            @Override
            public void serviceCreationFailed ( final Class<?> type, final Class<?> impl, final Throwable exception )
            {
                final Logger logger = LoggerFactory.getLogger ( impl );
                logger.warn ( "Service creation failed: " + type.getName (), exception );
            }
        } );

        return locator.getService ( RepositorySystem.class );
    }

    public static RepositorySystemSession newRepositorySystemSession ( final Path tempDir, final RepositorySystem system )
    {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession ();

        final LocalRepository localRepo = new LocalRepository ( tempDir.toFile () );
        session.setLocalRepositoryManager ( system.newLocalRepositoryManager ( session, localRepo ) );

        session.setTransferListener ( new LoggerTransferListener () );
        // session.setRepositoryListener ( new ConsoleRepositoryListener () );

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static RemoteRepository newCentralRepository ()
    {
        return new RemoteRepository.Builder ( "central", "default", "http://central.maven.org/maven2/" ).build ();
    }

    public static RemoteRepository newRemoteRepository ( final String url )
    {
        return new RemoteRepository.Builder ( "central", "default", url ).build ();
    }
}
