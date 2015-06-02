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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockManager implements AutoCloseable
{
    private final static Logger logger = LoggerFactory.getLogger ( LockManager.class );

    private class LockContextImpl extends LockContext
    {
        private final Set<String> resources = new HashSet<> ();

        @Override
        public void accessResource ( final String id )
        {
            performAccess ( id );
            this.resources.add ( id );
        }

        @Override
        public void modifyResource ( final String id )
        {
            performModify ( id );
            this.resources.add ( id );
        }

        @Override
        public boolean isAccessLocked ( final String id )
        {
            return checkAccess ( id );
        }

        @Override
        public boolean isModifyLocked ( final String id )
        {
            return checkModify ( id );
        }

        public void unlockAll ()
        {
            for ( final String id : this.resources )
            {
                unlockResource ( id );
            }
            this.resources.clear ();
        }
    }

    private static class Entry
    {
        final Set<Thread> readers = new HashSet<> ();

        Thread writer;

        final Set<Thread> writersWaiting = new HashSet<> ();

        @Override
        public String toString ()
        {
            return String.format ( "[readers: %s, writer: %s, writersWaiting: %s]", this.readers.size (), this.writer, this.writersWaiting.size () );
        }
    }

    private final Map<String, Entry> locks = new HashMap<> ();

    public synchronized void performAccess ( final String id )
    {
        logger.debug ( "performAccess: {}", id );

        while ( true )
        {
            Entry entry = this.locks.get ( id );

            logger.trace ( "Check lock state [access] - {} - {}", id, entry );

            if ( entry == null )
            {
                // no entry -> not locked at all
                entry = new Entry ();
                entry.readers.add ( Thread.currentThread () );
                this.locks.put ( id, entry );
                logger.trace ( "Acquired access by creation - id: {}", id );
                return;
            }

            if ( entry.writer == Thread.currentThread () )
            {
                logger.trace ( "Already acquired access - id: {}", id );
                // we already have the write lock
                return;
            }

            if ( entry.writer == null && entry.writersWaiting.isEmpty () )
            {
                // only read locked and no writer waiting -> add ourself
                entry.readers.add ( Thread.currentThread () );
                logger.trace ( "Acquired access - id: {}", id );
                return;
            }

            // somebody else holds a write lock or writers are waiting

            try
            {
                wait ();
            }
            catch ( final InterruptedException e )
            {
            }
        }
    }

    public synchronized void performModify ( final String id )
    {
        logger.debug ( "performModify: {}", id );

        boolean first = true;

        while ( true )
        {
            Entry entry = this.locks.get ( id );

            logger.trace ( "Check lock state [modify] - {} - {}", id, entry );

            if ( entry == null )
            {
                // no entry -> not locked at all
                entry = new Entry ();
                entry.writer = Thread.currentThread ();
                this.locks.put ( id, entry );
                logger.trace ( "Acquired modify by creation - id: {}", id );
                return;
            }

            if ( entry.writer == Thread.currentThread () )
            {
                // we already have the write lock
                logger.trace ( "Already acquired modify - id: {}", id );
                return;
            }

            // check after re-aqcuire check, since we then have passed this test in the past

            if ( first )
            {
                // if this is the first time we come here, remove ourself from the list of readers
                // since we want to become a writer now
                entry.readers.remove ( Thread.currentThread () );
                first = false;
            }

            // since all readers who want to be writers are removed as readers
            // we need to wait for an empty set
            final boolean noOtherReaders = entry.readers.isEmpty ();

            if ( noOtherReaders && entry.writer == null )
            {
                // unlocked or we are the only reader
                entry.writer = Thread.currentThread ();
                entry.writersWaiting.remove ( Thread.currentThread () );
                logger.trace ( "Acquired modify - id: {}", id );
                return;
            }

            // somebody else holds a read or write lock, mark as waiting

            entry.writersWaiting.add ( Thread.currentThread () );

            try
            {
                wait ();
            }
            catch ( final InterruptedException e )
            {
            }
        }
    }

    public synchronized void unlockResource ( final String id )
    {
        logger.debug ( "unlockResource: {}", id );

        final Entry entry = this.locks.get ( id );
        if ( entry == null )
        {
            return;
        }

        boolean wakeup = false;

        if ( entry.readers.remove ( Thread.currentThread () ) )
        {
            wakeup = true;
        }

        if ( entry.writer == Thread.currentThread () )
        {
            entry.writer = null;
            wakeup = true;
        }

        if ( checkRemoveEntry ( id, entry ) )
        {
            wakeup = true;
        }

        if ( wakeup )
        {
            notifyAll ();
        }
    }

    private boolean checkRemoveEntry ( final String id, final Entry entry )
    {
        if ( entry.readers.isEmpty () && entry.writer == null && entry.writersWaiting.isEmpty () )
        {
            logger.debug ( "remove lock entry: {}", id );
            this.locks.remove ( id );
            return true;
        }
        return false;
    }

    public synchronized boolean checkAccess ( final String id )
    {
        final Entry entry = this.locks.get ( id );

        if ( entry == null )
        {
            return false;
        }

        if ( entry.writer == Thread.currentThread () )
        {
            // if we have the write lock, we are also able to access the resource
            return true;
        }

        return entry.readers.contains ( Thread.currentThread () );
    }

    public synchronized boolean checkModify ( final String id )
    {
        final Entry entry = this.locks.get ( id );

        if ( entry == null )
        {
            return false;
        }

        return entry.writer == Thread.currentThread ();
    }

    private void testDisposed ()
    {
        // FIXME: implement
    }

    public void run ( final Runnable r )
    {
        testDisposed ();

        final LockContextImpl createdCtx = checkCreate ();

        try
        {
            r.run ();
        }
        finally
        {
            conditionalRemove ( createdCtx );
        }
    }

    public <V> V call ( final Callable<V> c ) throws Exception
    {
        testDisposed ();

        final LockContextImpl createdCtx = checkCreate ();

        try
        {
            return c.call ();
        }
        finally
        {
            conditionalRemove ( createdCtx );
        }
    }

    private LockContextImpl checkCreate ()
    {
        final LockContext ctx = LockContext.threadLocal.get ();

        if ( ctx == null )
        {
            final LockContextImpl newCtx = new LockContextImpl ();
            LockContext.threadLocal.set ( newCtx );
            return newCtx;
        }

        return null;
    }

    private void conditionalRemove ( final LockContextImpl ctx )
    {
        if ( ctx == null )
        {
            return;
        }

        ctx.unlockAll ();
        LockContext.threadLocal.remove ();
    }

    @Override
    public void close ()
    {
    }

}
