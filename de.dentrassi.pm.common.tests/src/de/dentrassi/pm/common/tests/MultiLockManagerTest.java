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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import de.dentrassi.pm.common.lm.LockContext;
import de.dentrassi.pm.common.lm.LockManager;

public class MultiLockManagerTest
{
    private final static Logger logger = LoggerFactory.getLogger ( MultiLockManagerTest.class );

    private static AtomicReference<String> global1 = new AtomicReference<> ();

    private static AtomicReference<String> global2 = new AtomicReference<> ();

    private final AtomicInteger readOps = new AtomicInteger ();

    private final AtomicInteger writeOps = new AtomicInteger ();

    private int readerGroups = 0;

    private int writerGroups = 0;

    @BeforeClass
    public static void setup ()
    {
        final ILoggerFactory factory = LoggerFactory.getILoggerFactory ();
        if ( factory instanceof LoggerContext )
        {
            final LoggerContext ctx = (LoggerContext)factory;

            final PatternLayoutEncoder encoder = new PatternLayoutEncoder ();

            encoder.setPattern ( "%date %level [%thread] %logger{10} [%file:%line] %msg%n" );
            encoder.setContext ( ctx );
            encoder.start ();

            final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<> ();
            appender.setEncoder ( encoder );
            appender.start ();
            appender.setContext ( ctx );

            final ch.qos.logback.classic.Logger rootLogger = ctx.getLogger ( Logger.ROOT_LOGGER_NAME );
            rootLogger.detachAppender ( "console" );
            rootLogger.addAppender ( appender );
            if ( Boolean.getBoolean ( "trace" ) )
            {
                rootLogger.setLevel ( Level.TRACE );
            }
            else
            {
                rootLogger.setLevel ( Level.WARN );
            }
        }
    }

    @Test ( timeout = 2_000 )
    public void test1 ()
    {
        MultiLockManagerTest.logger.info ( "test1" );

        try ( LockManager lm = new LockManager () )
        {
            final List<TestThread> threads = new LinkedList<> ();

            startLoopers ( "test1-", threads, 1, () -> {
                lm.run ( () -> {
                    LockContext.access ( "a" );
                    Assert.assertNull ( global1.get () );
                    sleep ( 100 );
                } );
            } );

            sleep ( 1_000 );

            completeThreads ( threads );
        }
    }

    @Test ( timeout = 2_000 )
    public void test2 ()
    {
        MultiLockManagerTest.logger.info ( "test2" );

        try ( LockManager lm = new LockManager () )
        {
            final List<TestThread> threads = new LinkedList<> ();

            startLoopers ( "test2-", threads, 10, () -> {
                lm.run ( () -> {
                    LockContext.access ( "a" );
                    Assert.assertNull ( global1.get () );
                    sleep ( 25 );
                } );
            } );

            sleep ( 1_000 );

            completeThreads ( threads );
        }
    }

    @Test ( timeout = 10_000 )
    public void test3 ()
    {
        MultiLockManagerTest.logger.info ( "test3" );

        this.readOps.set ( 0 );
        this.writeOps.set ( 0 );

        try ( LockManager lm = new LockManager () )
        {
            final List<TestThread> threads = new LinkedList<> ();

            startReaders ( lm, threads, global1 );

            startWriter ( lm, threads, global1, "a", "v1", 250 );

            sleep ( 5_000 );

            completeThreads ( threads );
        }

        System.out.format ( "test3 - Reads: %s, Writes: %s%n", this.readOps.get (), this.writeOps.get () );

        Assert.assertTrue ( this.readOps.get () > 100 ); // more than one
        Assert.assertTrue ( this.writeOps.get () > 5 ); // more than one
    }

    @Test ( timeout = 10_000 )
    public void test4 ()
    {
        MultiLockManagerTest.logger.info ( "test4" );

        this.readOps.set ( 0 );
        this.writeOps.set ( 0 );

        try ( LockManager lm = new LockManager () )
        {
            final List<TestThread> threads = new LinkedList<> ();

            startReaders ( lm, threads, global1 );

            startWriter ( lm, threads, global1, "a", "v1", 50 );
            startWriter ( lm, threads, global1, "a", "v2", 50 );
            startWriter ( lm, threads, global1, "a", "v3", 50 );

            sleep ( 5_000 );

            completeThreads ( threads );
        }

        System.out.format ( "test4 - Reads: %s, Writes: %s%n", this.readOps.get (), this.writeOps.get () );

        Assert.assertTrue ( this.readOps.get () > 10 ); // more than one
        Assert.assertTrue ( this.writeOps.get () > 5 ); // more than one
    }

    @Test ( timeout = 10_000 )
    public void test5a ()
    {
        MultiLockManagerTest.logger.info ( "test5" );

        performTest5 ( false );
    }

    @Test ( timeout = 10_000 )
    public void test5b ()
    {
        MultiLockManagerTest.logger.info ( "test5" );

        performTest5 ( true );
    }

    private void performTest5 ( final boolean single )
    {
        this.readOps.set ( 0 );
        this.writeOps.set ( 0 );

        try ( LockManager lm = new LockManager ( single ) )
        {
            final List<TestThread> threads = new LinkedList<> ();

            startReaders ( lm, threads, global1 );

            startWriter ( lm, threads, global1, "a", "v1", 50 );
            startWriter ( lm, threads, global1, "a", "v2", 50 );
            startWriter ( lm, threads, global1, "a", "v3", 50 );

            startWriter ( lm, threads, global2, "b", "v1", 50 );
            startWriter ( lm, threads, global2, "b", "v2", 50 );
            startWriter ( lm, threads, global2, "b", "v3", 50 );

            sleep ( 5_000 );

            completeThreads ( threads );
        }

        System.out.format ( "test4 - Reads: %s, Writes: %s%n", this.readOps.get (), this.writeOps.get () );

        Assert.assertTrue ( this.readOps.get () > 10 ); // more than one
        Assert.assertTrue ( this.writeOps.get () > 5 ); // more than one
    }

    private void startReaders ( final LockManager lm, final List<TestThread> threads, final AtomicReference<String> global )
    {
        final String prefix = String.format ( "reader-%s-", this.readerGroups++ );

        startLoopers ( prefix, threads, 10, () -> {
            lm.run ( () -> {
                LockContext.access ( "a" );
                Assert.assertNull ( global.get () );
                this.readOps.incrementAndGet ();
                sleep ( 25 );
            } );
        } );
    }

    private void startWriter ( final LockManager lm, final List<TestThread> threads, final AtomicReference<String> global, final String id, final String value, final int delay )
    {
        final String prefix = String.format ( "writer-%s-", this.writerGroups++ );

        startLoopers ( prefix, threads, 1, () -> {
            lm.run ( () -> {
                // acquire Access

                LockContext.access ( id );
                Assert.assertNull ( global.get () );

                // upgrade to writer
                LockContext.modify ( id );
                final String oldVal = global.getAndSet ( value );
                this.writeOps.incrementAndGet ();

                Assert.assertNull ( oldVal );
                Assert.assertEquals ( value, global.get () );

                sleep ( delay );

                final String val = global.getAndSet ( null );
                Assert.assertEquals ( value, val );
                Assert.assertNull ( global.get () );
            } );

            sleep ( delay );
        } );
    }

    private void completeThreads ( final List<TestThread> threads )
    {
        for ( final TestThread t : threads )
        {
            t.complete ();
        }

        for ( final TestThread t : threads )
        {
            try
            {
                t.join ();
            }
            catch ( final InterruptedException e )
            {
                e.printStackTrace ();
            }
        }

        for ( final TestThread t : threads )
        {
            Assert.assertNull ( t.getError () );
        }
    }

    private void startLoopers ( final String prefix, final List<TestThread> threads, final int count, final Runnable run )
    {
        for ( int i = 0; i < count; i++ )
        {
            final TestThread t = new TestThread ( run );
            t.setName ( prefix + i );

            threads.add ( t );
            t.start ();
        }
    }

    public static class TestThread extends Thread
    {
        private final Runnable loop;

        private Throwable error;

        private volatile boolean running = true;

        public TestThread ( final Runnable r )
        {
            this.loop = r;
        }

        public void complete ()
        {
            this.running = false;
        }

        @Override
        public void run ()
        {
            try
            {
                while ( this.running )
                {
                    this.loop.run ();
                }
            }
            catch ( final Throwable e )
            {
                this.error = e;
            }
        }

        public Throwable getError ()
        {
            return this.error;
        }
    }

    protected static void sleep ( final int millis )
    {
        try
        {
            Thread.sleep ( millis );
        }
        catch ( final InterruptedException e )
        {
            throw new IllegalStateException ( e );
        }
    }

}
