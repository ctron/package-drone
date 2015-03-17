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
package de.dentrassi.pm.database.schema;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.framework.Bundle;

import com.google.common.io.CharStreams;

/**
 * This is a parser for the SQL files
 * <p>
 * Basically the parser reads an UTF-8 test file, with Unix line encoding.
 * Splits the lines and processes each line with the pre-processor. Afterwards
 * the resulting lines will be concatenated again and split by semicolon (;).
 * The result is the SQL statements which will be executed.
 * </p>
 * <p>
 * The pre-processor allows to handle different database specific cases. Each
 * pre-processor command is started in a new line and with "--#". This is an SQL
 * comment with the additional "#" character. So as long as the SQL structure is
 * interrupted, this will still be a parsable SQL file.
 * </p>
 * <p>
 * The following commands are available:
 * <dl>
 * <dt>IFDEF <code>NAME</code></dt>
 * <dd>Test if a define is set. Defines are case sensitive</dd>
 * <dt>ENDIF</dt>
 * <dd>End an <code>IFDEF</code> section</dd>
 * <dt>DEFINE <code>NAME</code></dt>
 * <dd>Set a new define</dd>
 * <dt>UNDEF <code>NAME</code></dt>
 * <dd>Unset a define</dd>
 * </dl>
 * </p>
 */
public class Parser
{
    private final Bundle bundle;

    public static class State
    {

        private final boolean active;

        public State ( final boolean active )
        {
            this.active = active;
        }

        public boolean isActive ()
        {
            return this.active;
        }
    }

    private final LinkedList<State> states = new LinkedList<> ();

    private final Set<String> defines;

    public Parser ( final Bundle bundle, final Set<String> defines )
    {
        this.bundle = bundle;
        this.defines = defines == null ? new HashSet<> () : new HashSet<> ( defines );
    }

    public UpgradeTask loadTask ( final String name ) throws Exception
    {
        final URL entry = this.bundle.getEntry ( name );

        try ( final Reader r = new InputStreamReader ( entry.openStream (), StandardCharsets.UTF_8 ) )
        {
            final List<String> lines = CharStreams.readLines ( r );

            final StringBuilder sb = new StringBuilder ();
            for ( final String line : lines )
            {
                if ( line.startsWith ( "--#" ) )
                {
                    processMetaCommand ( line );
                }
                else if ( isActive () )
                {
                    sb.append ( line ).append ( '\n' );
                }
            }

            final String sql = sb.toString ();
            final String[] sqlToks = sql.split ( ";" );

            final List<String> sqls = new LinkedList<> ();

            for ( final String sqlTok : sqlToks )
            {
                final String s = sqlTok.trim ();
                if ( !s.isEmpty () )
                {
                    sqls.add ( s );
                }
            }

            if ( sqls.isEmpty () )
            {
                return null;
            }

            return new StatementTask ( sqls );
        }
    }

    private boolean isActive ()
    {
        if ( this.states.isEmpty () )
        {
            return true;
        }
        else
        {
            return this.states.peek ().isActive ();
        }
    }

    private void processMetaCommand ( final String line )
    {
        final String command = line.substring ( 3 );
        final LinkedList<String> toks = new LinkedList<> ( Arrays.asList ( command.split ( "\\s+" ) ) );
        if ( toks.isEmpty () )
        {
            return;
        }

        final String cmd = toks.poll ().toUpperCase ();
        switch ( cmd )
        {
            case "IFDEF":
                processIfDef ( toks );
                break;
            case "DEFINE":
                this.defines.add ( toks.poll () );
                break;
            case "UNDEFINE":
            case "UNDEF":
                this.defines.remove ( toks.poll () );
                break;
            case "ENDIF":
                this.states.pop ();
                break;
        }
    }

    private void processIfDef ( final LinkedList<String> toks )
    {
        final String def = toks.poll ();
        this.states.push ( new State ( isActive () && this.defines.contains ( def ) ) );
    }
}
