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
package de.dentrassi.pm.storage.service.jpa;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LockManager<E, I extends Comparable<I>>
{
    private final static Logger logger = LoggerFactory.getLogger ( LockManager.class );

    private final Map<I, ReadWriteLock> locks = new TreeMap<> ();

    private final Function<E, I> idFunc;

    public LockManager ( final Function<E, I> idFunc )
    {
        this.idFunc = idFunc;
    }

    public void readLock ( final E entity )
    {
        act ( entity, ReadWriteLock::readLock, Lock::lock, "readLock" );
    }

    public void readUnlock ( final E entity )
    {
        act ( entity, ReadWriteLock::readLock, Lock::unlock, "readUnlock" );
    }

    public void writeLock ( final E entity )
    {
        act ( entity, ReadWriteLock::writeLock, Lock::lock, "writeLock" );
    }

    public void writeUnlock ( final E entity )
    {
        act ( entity, ReadWriteLock::writeLock, Lock::unlock, "writeUnlock" );
    }

    protected void act ( final E entity, final Function<ReadWriteLock, Lock> f, final Consumer<Lock> actor, final String op )
    {
        final long start = System.currentTimeMillis ();

        actor.accept ( f.apply ( getLock ( this.idFunc.apply ( entity ) ) ) );

        if ( logger.isTraceEnabled () )
        {
            logger.trace ( "Lock operation {} took {} ms", op, System.currentTimeMillis () - start );
        }
    }

    private ReadWriteLock getLock ( final I id )
    {
        synchronized ( this.locks )
        {
            ReadWriteLock lock = this.locks.get ( id );
            if ( lock == null )
            {
                lock = new ReentrantReadWriteLock ();
                this.locks.put ( id, lock );
            }
            return lock;
        }
    }
}
