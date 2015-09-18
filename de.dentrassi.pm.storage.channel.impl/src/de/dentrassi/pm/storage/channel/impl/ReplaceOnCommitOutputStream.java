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
package de.dentrassi.pm.storage.channel.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ReplaceOnCommitOutputStream extends OutputStream
{
    private final OutputStream out;

    private final Path targetName;

    private final Path tmp;

    private boolean commited;

    private boolean closed;

    public ReplaceOnCommitOutputStream ( final Path targetName ) throws IOException
    {
        this.targetName = targetName;
        this.tmp = Files.createTempFile ( targetName.getParent (), targetName.getName ( targetName.getNameCount () - 1 ).toString (), ".swp" );
        this.out = Files.newOutputStream ( this.tmp );
    }

    @Override
    public void write ( final int b ) throws IOException
    {
        this.out.write ( b );
    }

    @Override
    public void write ( final byte[] b ) throws IOException
    {
        this.out.write ( b );
    }

    @Override
    public void write ( final byte[] b, final int off, final int len ) throws IOException
    {
        this.out.write ( b, off, len );
    }

    @Override
    public void flush () throws IOException
    {
        this.out.flush ();
    }

    public void commit ()
    {
        this.commited = true;
    }

    @Override
    public void close () throws IOException
    {
        if ( this.closed )
        {
            return;
        }

        this.closed = true;

        try
        {
            this.out.close ();
        }
        finally
        {
            try
            {
                if ( this.commited )
                {
                    Files.deleteIfExists ( this.targetName );
                    Files.move ( this.tmp, this.targetName, StandardCopyOption.ATOMIC_MOVE );
                }
            }
            finally
            {
                Files.deleteIfExists ( this.tmp );
            }
        }
    }

}
