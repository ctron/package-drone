package de.dentrassi.pm.testing.mvn;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

public class MavenUtil
{
    public static RepositorySystem newRepositorySystem ()
    {
        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator ();
        locator.addService ( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService ( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService ( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler ( new DefaultServiceLocator.ErrorHandler () {
            @Override
            public void serviceCreationFailed ( final Class<?> type, final Class<?> impl, final Throwable exception )
            {
                exception.printStackTrace ();
            }
        } );

        return locator.getService ( RepositorySystem.class );
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession ( final RepositorySystem system )
    {
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession ();

        final LocalRepository localRepo = new LocalRepository ( "target/local-repo" );
        session.setLocalRepositoryManager ( system.newLocalRepositoryManager ( session, localRepo ) );

        return session;
    }

}
