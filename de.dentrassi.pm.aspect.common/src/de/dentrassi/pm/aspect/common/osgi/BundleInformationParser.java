package de.dentrassi.pm.aspect.common.osgi;

import java.io.IOException;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;

public class BundleInformationParser
{
    private final Manifest manifest;

    public BundleInformationParser ( final Manifest manifest )
    {
        this.manifest = manifest;
    }

    public BundleInformation parse () throws IOException
    {
        final BundleInformation result = new BundleInformation ();

        final String name = makeName ( this.manifest.getMainAttributes ().getValue ( Constants.BUNDLE_SYMBOLICNAME ) );
        if ( name == null )
        {
            return null;
        }

        result.setId ( name );

        return result;
    }

    private static String makeName ( final String value )
    {
        if ( value == null )
        {
            return null;
        }

        return value.split ( ";", 2 )[0];
    }
}
