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
package de.dentrassi.pm.apm;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class MockStorageProvider extends AbstractSimpleStorageModelProvider<MockStorageViewModel, MockStorageModel>
{
    private final String key;

    private final String initialValue;

    private MockStorageModel writeModel;

    public MockStorageProvider ( final String key, final String initialValue )
    {
        this.key = key;
        this.initialValue = initialValue;
    }

    @Override
    public MockStorageModel cloneWriteModel ()
    {
        return new MockStorageModel ( this.writeModel );
    }

    @Override
    protected MockStorageViewModel renderViewModel ( final MockStorageModel writeModel )
    {
        return new MockStorageViewModel ( writeModel.getValue () );
    }

    @Override
    protected void updateWriteModel ( final MockStorageModel writeModel )
    {
        this.writeModel = writeModel;
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final MockStorageModel writeModel ) throws Exception
    {
        Files.createDirectories ( context.getBasePath () );
        final Path path = context.getBasePath ().resolve ( this.key );

        try ( ObjectOutputStream os = new ObjectOutputStream ( Files.newOutputStream ( path ) ) )
        {
            os.writeObject ( writeModel );
        }
    }

    @Override
    protected MockStorageModel loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( ObjectInputStream is = new ObjectInputStream ( Files.newInputStream ( context.getBasePath ().resolve ( this.key ) ) ) )
        {
            return (MockStorageModel)is.readObject ();
        }
        catch ( final NoSuchFileException e )
        {
            return new MockStorageModel ( this.initialValue );
        }
    }

}
