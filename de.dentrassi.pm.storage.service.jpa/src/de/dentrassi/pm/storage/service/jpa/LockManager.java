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
package de.dentrassi.pm.storage.service.jpa;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.pm.common.utils.ThrowingRunnable;

public class LockManager<I extends Comparable<I>>
{
    private final static Logger logger = LoggerFactory.getLogger ( LockManager.class );

    private final Lock writeLock;

    public LockManager ()
    {
        this.writeLock = new ReentrantLock ();
    }

    private final Map<I, ReadWriteLock> locks = new TreeMap<> ();

    public void accessRun ( final I id, final ThrowingRunnable run )
    {
        act ( id, ReadWriteLock::readLock, ( ) -> {
            run.run ();
            return null;
        } );
    }

    public void modifyRun ( final I id, final ThrowingRunnable run )
    {
        act ( id, ReadWriteLock::writeLock, ( ) -> {
            run.run ();
            return null;
        } );
    }

    public <T> T accessCall ( final I id, final Callable<T> run )
    {
        return act ( id, ReadWriteLock::readLock, run );
    }

    public <T> T modifyCall ( final I id, final Callable<T> run )
    {
        return act ( id, ReadWriteLock::writeLock, run );
    }

    public void removeLock ( final I id )
    {
        this.writeLock.lock ();
        try
        {
            this.locks.remove ( id );
        }
        finally
        {
            this.writeLock.unlock ();
        }
    }

    protected <T> T act ( final I id, final Function<ReadWriteLock, Lock> f, final Callable<T> run )
    {
        try
        {
            return process ( id, f, run );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    protected <T> T process ( final I id, final Function<ReadWriteLock, Lock> f, final Callable<T> run ) throws Exception
    {
        final ReadWriteLock rwLock = getLock ( id );

        if ( rwLock == null )
        {
            logger.info ( "No lock found for: '{}'", id );
            return run.call ();
        }

        final long start = System.currentTimeMillis ();
        final Lock lock = f.apply ( rwLock );
        lock.lock ();
        try
        {
            if ( logger.isTraceEnabled () )
            {
                logger.trace ( "Lock operation for {} took {} ms", id, System.currentTimeMillis () - start );
            }

            return run.call ();
        }
        finally
        {
            lock.unlock ();
        }
    }

    private ReadWriteLock getLock ( final I id )
    {
        this.writeLock.lock ();
        try
        {
            ReadWriteLock lock = this.locks.get ( id );
            if ( lock == null )
            {
                lock = new ReentrantReadWriteLock ();
                this.locks.put ( id, lock );
            }
            return lock;
        }
        finally
        {
            this.writeLock.unlock ();
        }
    }
}
