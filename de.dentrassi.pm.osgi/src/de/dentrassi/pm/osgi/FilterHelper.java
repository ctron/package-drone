/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.osgi;

import java.util.LinkedList;
import java.util.List;

public class FilterHelper
{
    public interface Node
    {
    }

    public static class Multi implements Node
    {
        private final String operator;

        private final List<Node> nodes = new LinkedList<> ();

        public Multi ( final String operator )
        {
            this.operator = operator;
        }

        public void addNode ( final Node node )
        {
            this.nodes.add ( node );
        }

        @Override
        public String toString ()
        {
            return join ( this.nodes, this.operator );
        }

    }

    public static class Pair implements Node
    {
        private final String key;

        private final String value;

        public Pair ( final String key, final String value )
        {
            this.key = key;
            this.value = value;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getValue ()
        {
            return this.value;
        }

        @Override
        public String toString ()
        {
            return "(" + this.key + "=" + this.value + ")";
        }

    }

    public static String join ( final List<? extends Node> nodes, final String oper )
    {
        final List<String> s = new LinkedList<> ();
        for ( final Node node : nodes )
        {
            final String f = node.toString ();
            if ( !f.isEmpty () )
            {
                s.add ( f );
            }
        }

        if ( s == null || s.isEmpty () )
        {
            return "";
        }

        if ( s.size () == 1 )
        {
            return s.get ( 0 );
        }

        final StringBuilder builder = new StringBuilder ();
        builder.append ( '(' ).append ( oper );

        for ( final String tok : s )
        {
            builder.append ( tok );
        }

        builder.append ( ')' );

        return builder.toString ();
    }

    public static String or ( final List<? extends Node> nodes )
    {
        return join ( nodes, "|" );
    }

    public static String and ( final List<? extends Node> nodes )
    {
        return join ( nodes, "&" );
    }
}
