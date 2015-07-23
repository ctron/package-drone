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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import org.eclipse.scada.utils.io.RecursiveDeleteVisitor;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dentrassi.pm.common.MetaKey;

public class BaseTest
{
    private static Path basePath;

    @BeforeClass
    public static void setup () throws IOException
    {
        basePath = Paths.get ( ".", "target", "test" ).toAbsolutePath ();
        if ( Files.exists ( basePath ) )
        {
            Files.walkFileTree ( basePath, new RecursiveDeleteVisitor () );
        }
    }

    /**
     * Test a simple init and close
     */
    @Test
    public void test1 ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        mgr.close ();
    }

    /**
     * Test a plain registration and diposal
     */
    @Test
    public void test2a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        final StorageRegistration reg = mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        reg.unregister ();
        mgr.close ();
    }

    /**
     * Test a registration and unregistration after disposal
     * <p>
     * Although the unregister method is called after the close method, this
     * must not cause any troubles.
     * </p>
     */
    @Test
    public void test2b ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        final StorageRegistration reg = mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        mgr.close ();
        reg.unregister ();
    }

    /**
     * Register a model after the manager was closed. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test2c ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        mgr.close ();
        mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
    }

    /**
     * Register the same model twice. Expect failure!
     */
    @Test ( expected = IllegalArgumentException.class )
    public void test2d ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        mgr.registerModel ( 1, new MetaKey ( "mock", "1" ), new MockStorageProvider ( "1", "foo" ) );
        mgr.close ();
    }

    /**
     * Access a model
     */
    @Test
    public void test3a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );
        final StorageRegistration reg = mgr.registerModel ( 1, new MetaKey ( "mock", "3a" ), new MockStorageProvider ( "3a", "foo" ) );

        mgr.accessRun ( new MetaKey ( "mock", "3a" ), MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        reg.unregister ();
        mgr.close ();
    }

    /**
     * Access and modify a model
     */
    @Test
    public void test3b ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key = new MetaKey ( "mock", "3b" );

        final StorageRegistration reg = mgr.registerModel ( 1, key, new MockStorageProvider ( "3b", "foo" ) );

        mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        mgr.modifyRun ( key, MockStorageModel.class, m -> {
            m.setValue ( "bar" );
        } );

        mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "bar", m.getValue () );
        } );

        reg.unregister ();
        mgr.close ();
    }

    /**
     * Access and modify a model. During the modification generate an error.
     * Access the model again.
     * <p>
     * This should test of the model is being rolled back due to a failure
     * during the modification
     * </p>
     */
    @Test
    public void test4a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key = new MetaKey ( "mock", "4a" );

        final StorageRegistration reg = mgr.registerModel ( 1, key, new MockStorageProvider ( "4a", "foo" ) );

        mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        Exception ex = null;
        try
        {
            mgr.modifyRun ( key, MockStorageModel.class, m -> {
                m.setValue ( "bar" );
                throw new RuntimeException ();
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        mgr.accessRun ( key, MockStorageViewModel.class, m -> {
            assertEquals ( "foo", m.getValue () );
        } );

        reg.unregister ();
        mgr.close ();
    }

    /**
     * Access three models in the correct lock order
     */
    @Test
    public void test5a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5a1" );
        final MetaKey key2 = new MetaKey ( "mock", "5a2" );
        final MetaKey key3 = new MetaKey ( "mock", "5a3" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );

        mgr.close ();
    }

    /**
     * Access the same model ... test re-locking
     */
    @Test
    public void test5b ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5b1" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key1, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key1, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );

        mgr.close ();
    }

    /**
     * Lock two models in the wrong lock order. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5c ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5c1" );
        final MetaKey key2 = new MetaKey ( "mock", "5c2" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        mgr.accessRun ( key2, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key1, MockStorageViewModel.class, m2 -> {
            } );
        } );

        mgr.close ();
    }

    /**
     * Lock three models in the correct lock order. Do it twice.
     */
    @Test
    public void test5d ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5d1" );
        final MetaKey key2 = new MetaKey ( "mock", "5d2" );
        final MetaKey key3 = new MetaKey ( "mock", "5d3" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                } );
            } );
        } );

        mgr.close ();
    }

    /**
     * Lock three models in the correct lock order. But re-lock an already
     * locked model. Do it twice.
     */
    @Test
    public void test5e ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5e1" );
        final MetaKey key2 = new MetaKey ( "mock", "5e2" );
        final MetaKey key3 = new MetaKey ( "mock", "5e3" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                    } );
                } );
            } );
        } );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                    } );
                } );
            } );
        } );

        mgr.close ();
    }

    /**
     * Lock three models in the wrong lock order. But re-lock an already
     * locked model before. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5f ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5f1" );
        final MetaKey key2 = new MetaKey ( "mock", "5f2" );
        final MetaKey key3 = new MetaKey ( "mock", "5f3" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                    mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                    } );
                } );
            } );
        } );

        mgr.close ();
    }

    /**
     * Re-lock a model and make changes. Only the outer lock should persist.
     */
    @Test
    public void test5g ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5g1" );
        final MetaKey key2 = new MetaKey ( "mock", "5g2" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
                mgr.modifyRun ( key1, MockStorageModel.class, m1a -> {
                    m1a.setValue ( "bar" );
                } );
            } );
        } );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "bar", m1.getValue () );
        } );

        mgr.close ();
    }

    /**
     * Try to upgrade read lock to a write lock. Expect failure!
     */
    @Test ( expected = IllegalStateException.class )
    public void test5h ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "5h1" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1r -> {
            mgr.modifyRun ( key1, MockStorageModel.class, m1w -> {
            } );
        } );

        mgr.close ();
    }

    /**
     * Re-lock key2 and modify in sub-call and fail in upper call. Expect
     * rollback.
     */
    @Test
    public void test6a ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "6a1" );
        final MetaKey key2 = new MetaKey ( "mock", "6a2" );
        final MetaKey key3 = new MetaKey ( "mock", "6a3" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );
        mgr.registerModel ( 3, key3, new MockStorageProvider ( key3.getKey (), "foo" ) );

        Exception ex = null;
        try
        {
            mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
                m1.setValue ( "bar" );
                mgr.modifyRun ( key2, MockStorageModel.class, m2 -> {
                    mgr.accessRun ( key3, MockStorageViewModel.class, m3 -> {
                        mgr.modifyRun ( key2, MockStorageModel.class, m2a -> {
                            m2a.setValue ( "bar" );
                        } );
                        throw new RuntimeException ( "failure" );
                    } );
                } );
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "foo", m1.getValue () );
        } );
        mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
            assertEquals ( "foo", m2.getValue () );
        } );

        mgr.close ();
    }

    /**
     * Cause an internal failure by specifying the wrong model class. Expect
     * rollback.
     */
    @Test
    public void test6b ()
    {
        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "6b1" );
        final MetaKey key2 = new MetaKey ( "mock", "6b2" );

        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );
        mgr.registerModel ( 2, key2, new MockStorageProvider ( key2.getKey (), "foo" ) );

        Exception ex = null;
        try
        {
            mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
                m1.setValue ( "bar" );

                // the next line specifies the wrong model class
                mgr.modifyRun ( key2, MockStorageViewModel.class, m2 -> {
                    // no-op
                } );
            } );
        }
        catch ( final Exception e )
        {
            ex = e;
        }

        assertNotNull ( ex );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            assertEquals ( "foo", m1.getValue () );
        } );
        mgr.accessRun ( key2, MockStorageViewModel.class, m2 -> {
            assertEquals ( "foo", m2.getValue () );
        } );

        mgr.close ();
    }

    /**
     * Test plain "after" execution.
     */
    @Test
    public void test7a ()
    {
        final LinkedList<String> result = new LinkedList<> ();

        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "7a1" );
        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        mgr.accessRun ( key1, MockStorageViewModel.class, m1 -> {
            StorageManager.executeAfterPersist ( () -> result.add ( "1" ) );
            // this was executed immediately
            assertArrayEquals ( new Object[] { "1" }, result.toArray () );
        } );

        mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {
            StorageManager.executeAfterPersist ( () -> result.add ( "2" ) );
            // this was scheduled for later
            assertArrayEquals ( new Object[] { "1" }, result.toArray () );
        } );

        // finally check all
        System.out.println ( result );
        assertArrayEquals ( new Object[] { "1", "2" }, result.toArray () );

        mgr.close ();
    }

    /**
     * Test plain "after" execution.
     */
    @Test
    public void test7b ()
    {
        final LinkedList<String> result = new LinkedList<> ();

        final StorageManager mgr = new StorageManager ( basePath );

        final MetaKey key1 = new MetaKey ( "mock", "7b1" );
        mgr.registerModel ( 1, key1, new MockStorageProvider ( key1.getKey (), "foo" ) );

        mgr.modifyRun ( key1, MockStorageModel.class, m1 -> {

            StorageManager.executeAfterPersist ( () -> result.add ( "1" ) );
            // scheduled
            assertArrayEquals ( new Object[] {}, result.toArray () );

            mgr.accessRun ( key1, MockStorageViewModel.class, m1a -> {
                StorageManager.executeAfterPersist ( () -> result.add ( "2" ) );
                // should be scheduled as well, since we have an outer modify call
                assertArrayEquals ( new Object[] {}, result.toArray () );
            } );

            // again, no change
            assertArrayEquals ( new Object[] {}, result.toArray () );
        } );

        // finally check all
        System.out.println ( result );
        assertArrayEquals ( new Object[] { "1", "2" }, result.toArray () );

        mgr.close ();
    }

}
