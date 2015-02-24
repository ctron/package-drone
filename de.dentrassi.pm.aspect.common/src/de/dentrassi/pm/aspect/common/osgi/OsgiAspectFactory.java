/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.common.osgi;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.extract.Extractor;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.feature.FeatureInformation;

public class OsgiAspectFactory implements ChannelAspectFactory
{
    private final static Logger logger = LoggerFactory.getLogger ( OsgiAspectFactory.class );

    public static final String ID = "osgi";

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public Extractor getExtractor ()
        {
            return new OsgiExtractor ( ChannelAspectImpl.this );
        }

        @Override
        public String getId ()
        {
            return ID;
        }
    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

    public static <T extends BundleInformation> T fetchBundleInformation ( final Map<MetaKey, String> metadata, final Class<T> clazz )
    {
        final String string = metadata.get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_BUNDLE_INFORMATION ) );
        if ( string == null )
        {
            return null;
        }

        try
        {
            return BundleInformation.fromJson ( string, clazz );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to parse bundle information", e );
            return null;
        }
    }

    public static <T extends FeatureInformation> T fetchFeatureInformation ( final Map<MetaKey, String> metadata, final Class<T> clazz )
    {
        final String string = metadata.get ( new MetaKey ( OsgiAspectFactory.ID, OsgiExtractor.KEY_FEATURE_INFORMATION ) );
        if ( string == null )
        {
            return null;
        }

        try
        {
            return FeatureInformation.fromJson ( string, clazz );
        }
        catch ( final Exception e )
        {
            logger.debug ( "Failed to parse feature information", e );
            return null;
        }
    }

    public static BundleInformation fetchBundleInformation ( final Map<MetaKey, String> metadata )
    {
        return fetchBundleInformation ( metadata, BundleInformation.class );
    }

    public static FeatureInformation fetchFeatureInformation ( final Map<MetaKey, String> metadata )
    {
        return fetchFeatureInformation ( metadata, FeatureInformation.class );
    }

}
