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

public abstract class AbstractSimpleStorageModelProvider<V, W> implements StorageModelProvider<V, W>
{
    private V viewModel;

    private StorageContext context;

    @Override
    public V getViewModel ()
    {
        return this.viewModel;
    }

    @Override
    public void start ( final StorageContext context ) throws Exception
    {
        this.context = context;

        final W writeModel = loadWriteModel ( context );
        this.viewModel = renderViewModel ( writeModel );
        updateWriteModel ( writeModel );
    }

    @Override
    public void stop ()
    {
    }

    protected void updateWriteModel ( final W writeModel )
    {
    }

    @Override
    public void persistWriteModel ( final W writeModel ) throws Exception
    {
        final V newViewModel = renderViewModel ( writeModel );

        persistWriteModel ( this.context, writeModel );
        updateWriteModel ( writeModel );

        this.viewModel = newViewModel;
    }

    protected abstract void persistWriteModel ( StorageContext context, final W writeModel ) throws Exception;

    protected abstract V renderViewModel ( final W writeModel );

    protected abstract W loadWriteModel ( StorageContext context ) throws Exception;

}
