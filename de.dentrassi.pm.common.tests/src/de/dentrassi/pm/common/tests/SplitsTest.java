package de.dentrassi.pm.common.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.dentrassi.pm.common.utils.Splits;

public class SplitsTest
{
    @Test
    public void test1 ()
    {
        final List<?> result = Splits.split ( Arrays.asList (), 0, 0 );
        assertTrue ( result.isEmpty () );
    }

    @Test
    public void test2 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 0, 0 );
        assertTrue ( result.isEmpty () );
    }

    @Test
    public void test3 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 0, 3 );
        assertEquals ( 3, result.size () );
    }

    @Test
    public void test4 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 0, 10 );
        assertEquals ( 3, result.size () );
    }

    @Test
    public void test5 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 10, 10 );
        assertEquals ( 0, result.size () );
    }

    @Test
    public void test6 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 1, 3 );
        assertEquals ( 2, result.size () );
        assertArrayEquals ( new Integer[] { 2, 3 }, result.toArray () );
    }

    @Test
    public void test7 ()
    {
        final List<?> result = Splits.split ( Arrays.asList ( 1, 2, 3 ), 1, -1 );
        assertEquals ( 2, result.size () );
        assertArrayEquals ( new Integer[] { 2, 3 }, result.toArray () );
    }
}
