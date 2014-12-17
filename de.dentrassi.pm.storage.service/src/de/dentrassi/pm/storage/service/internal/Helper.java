/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
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
import de.dentrassi.pm.storage.jpa.ExtractedArtifactPropertyEntity;
import de.dentrassi.pm.storage.jpa.ProvidedArtifactPropertyEntity;

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

    public static <T extends ArtifactPropertyEntity> T fillPropertyEntry ( final ArtifactEntity artifact, final Map.Entry<MetaKey, String> entry, final Supplier<T> supplier )
    {
        final T ap = supplier.get ();
        ap.setArtifact ( artifact );
        ap.setKey ( entry.getKey ().getKey () );
        ap.setNamespace ( entry.getKey ().getNamespace () );
        ap.setValue ( entry.getValue () );
        return ap;
    }
}
