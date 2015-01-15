/*******************************************************************************
 * Copyright (c) 2014, 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.internal;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.ArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.ChannelPropertyEntity;
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ExtractedChannelPropertyEntity;
import de.dentrassi.pm.storage.jpa.PropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedChannelPropertyEntity;

public final class Helper
{
    private Helper ()
    {
    }

    public static void convertProvidedProperties ( final Map<MetaKey, String> metadata, final ArtifactEntity artifact, final Collection<ProvidedArtifactPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( artifact, entry, ProvidedArtifactPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
        }
    }

    public static void convertExtractedProperties ( final Map<MetaKey, String> metadata, final ArtifactEntity artifact, final Collection<ExtractedArtifactPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( artifact, entry, ExtractedArtifactPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
        }
    }

    public static void convertProvidedProperties ( final Map<MetaKey, String> metadata, final ChannelEntity channel, final Collection<ProvidedChannelPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( channel, entry, ProvidedChannelPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
        }
    }

    public static void convertExtractedProperties ( final Map<MetaKey, String> metadata, final ChannelEntity channel, final Collection<ExtractedChannelPropertyEntity> props )
    {
        if ( metadata != null )
        {
            metadata.entrySet ().stream ().map ( entry -> fillPropertyEntry ( channel, entry, ExtractedChannelPropertyEntity::new ) ).collect ( Collectors.toCollection ( ( ) -> props ) );
        }
    }

    public static <T extends ArtifactPropertyEntity> T fillPropertyEntry ( final ArtifactEntity artifact, final Map.Entry<MetaKey, String> entry, final Supplier<T> supplier )
    {
        final T ap = supplier.get ();
        ap.setArtifact ( artifact );
        fillPropertyEntry ( entry, ap );
        return ap;
    }

    public static <T extends ChannelPropertyEntity> T fillPropertyEntry ( final ChannelEntity channel, final Map.Entry<MetaKey, String> entry, final Supplier<T> supplier )
    {
        final T ap = supplier.get ();
        ap.setChannel ( channel );
        fillPropertyEntry ( entry, ap );
        return ap;
    }

    protected static void fillPropertyEntry ( final Map.Entry<MetaKey, String> entry, final PropertyEntity ap )
    {
        ap.setKey ( entry.getKey ().getKey () );
        ap.setNamespace ( entry.getKey ().getNamespace () );
        ap.setValue ( entry.getValue () );
    }
}
