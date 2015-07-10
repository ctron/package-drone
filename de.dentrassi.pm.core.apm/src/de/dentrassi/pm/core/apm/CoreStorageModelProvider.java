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
package de.dentrassi.pm.core.apm;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.dentrassi.pm.apm.AbstractSimpleStorageModelProvider;
import de.dentrassi.pm.apm.StorageContext;
import de.dentrassi.pm.common.MetaKey;

public class CoreStorageModelProvider extends AbstractSimpleStorageModelProvider<CoreServiceViewModel, CoreServiceModel>
{
    private CoreServiceModel writeModel;

    @Override
    public void updateWriteModel ( final CoreServiceModel writeModel )
    {
        this.writeModel = writeModel;
    }

    @Override
    public CoreServiceModel cloneWriteModel ()
    {
        return new CoreServiceModel ( this.writeModel );
    }

    @Override
    protected CoreServiceViewModel renderViewModel ( final CoreServiceModel writeModel )
    {
        return new CoreServiceViewModel ( writeModel.getProperties () );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final CoreServiceModel writeModel ) throws Exception
    {
        try ( Writer writer = Files.newBufferedWriter ( makePath ( context ) ) )
        {
            final Properties p = new Properties ();
            for ( final Map.Entry<MetaKey, String> entry : writeModel.getProperties ().entrySet () )
            {
                final String value = entry.getValue ();
                if ( value == null )
                {
                    continue;
                }

                p.put ( entry.getKey ().toString (), entry.getValue () );
            }
            p.store ( writer, null );
        }
    }

    @Override
    protected CoreServiceModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        final Properties p = new Properties ();
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ) ) )
        {
            p.load ( reader );
        }
        catch ( final NoSuchFileException e )
        {
            // simply ignore
        }

        // now convert to a hash set

        final Map<MetaKey, String> result = new HashMap<> ( p.size () );

        for ( final String key : p.stringPropertyNames () )
        {
            final MetaKey metaKey = MetaKey.fromString ( key );
            result.put ( metaKey, p.getProperty ( key ) );
        }

        return new CoreServiceModel ( result );
    }

    private Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "core.properties" );
    }
}
