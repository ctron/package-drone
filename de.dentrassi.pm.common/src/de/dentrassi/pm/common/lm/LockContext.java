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
package de.dentrassi.pm.common.lm;

import java.util.function.Consumer;

public abstract class LockContext
{
    static ThreadLocal<LockContext> threadLocal = new ThreadLocal<> ();

    public static void with ( final Consumer<LockContext> runner ) throws LockContextNotFoundException
    {
        final LockContext ctx = threadLocal.get ();
        if ( ctx == null )
        {
            throw new LockContextNotFoundException ();
        }

        runner.accept ( ctx );
    }

    public abstract void accessResource ( final String id );

    public abstract void modifyResource ( final String id );

    public abstract boolean isAccessLocked ( final String id );

    public abstract boolean isModifyLocked ( final String id );

    public static void access ( final String id ) throws LockContextNotFoundException
    {
        with ( ctx -> ctx.accessResource ( id ) );
    }

    public static void modify ( final String id ) throws LockContextNotFoundException
    {
        with ( ctx -> ctx.modifyResource ( id ) );
    }

    public static void assertAccessLock ( final String id ) throws LockContextNotFoundException, AssertionError
    {
        with ( ctx -> {
            if ( !ctx.isAccessLocked ( id ) )
            {
                throw new AssertionError ();
            }
        } );
    }

    public static void assertModifyLock ( final String id ) throws LockContextNotFoundException, AssertionError
    {
        with ( ctx -> {
            if ( !ctx.isModifyLocked ( id ) )
            {
                throw new AssertionError ();
            }
        } );
    }

}
