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
package de.dentrassi.pm.common.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.dentrassi.pm.common.lm.LockContext;
import de.dentrassi.pm.common.lm.LockContextNotFoundException;
import de.dentrassi.pm.common.lm.LockManager;

public class SingleLockManagerTest
{
    @Test
    public void test1 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
        }
    }

    @Test
    public void test2 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( this::process2 );
        }
    }

    @Test ( expected = LockContextNotFoundException.class )
    public void test2a ()
    {
        LockContext.access ( "a" );
    }

    @Test
    public void test3 () throws Exception
    {
        try ( final LockManager lm = new LockManager () )
        {
            final Integer result = lm.call ( this::process3 );
            assertEquals ( Integer.valueOf ( 42 ), result );
        }
    }

    @Test
    public void test4 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( this::process4 );
        }
    }

    @Test ( expected = AssertionError.class )
    public void test5 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                LockContext.access ( "a" );
                processModifyA ();
            } );
        }
    }

    @Test ( expected = AssertionError.class )
    public void test6 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                LockContext.modify ( "b" );
                processModifyA ();
            } );
        }
    }

    @Test
    public void test7 ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                LockContext.access ( "a" );

                processAccessA ();

                LockContext.access ( "a" );

                processAccessA ();
            } );
        }
    }

    @Test
    public void test7a ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                LockContext.access ( "a" );

                processAccessA ();

                lm.run ( () -> {
                    LockContext.access ( "a" );

                    processAccessA ();
                } );
            } );
        }
    }

    @Test ( expected = LockContextNotFoundException.class )
    public void test7b ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                LockContext.access ( "a" );

                processAccessA ();
            } );
            processAccessA ();
        }
    }

    @Test
    public void test7c ()
    {
        try ( final LockManager lm = new LockManager () )
        {
            lm.run ( () -> {
                lm.run ( () -> {
                    LockContext.access ( "a" );

                    processAccessA ();
                } );

                // the next call has to succeed, since the outer call closes the context
                processAccessA ();
            } );
        }
    }

    private void process4 ()
    {
        LockContext.modify ( "a" );
        processModifyA ();
    }

    protected void process2 ()
    {
    }

    protected Integer process3 ()
    {
        return 42;
    }

    protected void processAccessA ()
    {
        LockContext.assertAccessLock ( "a" );
    }

    protected void processModifyA ()
    {
        LockContext.assertAccessLock ( "a" );
        LockContext.assertModifyLock ( "a" );
    }

}
