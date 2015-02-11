/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.maven;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.pm.common.ArtifactInformation;
import de.dentrassi.pm.common.MetaKey;

public class ChannelData
{

    public static abstract class Node
    {
    }

    public static class DirectoryNode extends Node
    {
        private final Map<String, Node> nodes = new HashMap<> ();

        public Map<String, Node> getNodes ()
        {
            return this.nodes;
        }
    }

    public static class DataNode extends Node
    {
        private final byte[] data;

        private final String mimeType;

        public DataNode ( final byte[] data, final String mimeType )
        {
            this.data = data;
            this.mimeType = mimeType;
        }

        public DataNode ( final String data, final String mimeType )
        {
            this.data = data.getBytes ( StandardCharsets.UTF_8 );
            this.mimeType = mimeType;
        }

        public String getMimeType ()
        {
            return this.mimeType;
        }

        public byte[] getData ()
        {
            return this.data;
        }
    }

    public static class ArtifactNode extends Node
    {
        private final String artifactId;

        public ArtifactNode ( final String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getArtifactId ()
        {
            return this.artifactId;
        }
    }

    private final DirectoryNode root = new DirectoryNode ();

    public void add ( final MavenInformation info, final ArtifactInformation art )
    {
        final String[] gn = info.getGroupId ().split ( "\\." );
        final DirectoryNode groupNode = getGroup ( gn );

        final DirectoryNode artifactBase = addDirNode ( groupNode, info.getArtifactId () );
        final DirectoryNode versionNode = addDirNode ( artifactBase, info.getVersion () );

        addNode ( versionNode, info.makeName (), new ArtifactNode ( art.getId () ) );

        addCheckSum ( versionNode, info.makeName (), art, "md5" );
        addCheckSum ( versionNode, info.makeName (), art, "sha1" );
    }

    private void addCheckSum ( final DirectoryNode versionNode, final String name, final ArtifactInformation art, final String string )
    {
        final String data = art.getMetaData ().get ( new MetaKey ( "hasher", string ) );
        if ( data == null )
        {
            return;
        }

        addNode ( versionNode, name + "." + string, new DataNode ( data, "text/plain" ) );
    }

    private DirectoryNode getGroup ( final String[] gn )
    {
        final LinkedList<String> dir = new LinkedList<> ( Arrays.asList ( gn ) );

        DirectoryNode current = this.root;
        while ( !dir.isEmpty () )
        {
            current = addDirNode ( current, dir.pollFirst () );
        }

        return current;
    }

    private <T extends Node> T addNode ( final DirectoryNode current, final String seg, final T node )
    {
        if ( current.nodes.containsKey ( seg ) )
        {
            throw new IllegalStateException ( String.format ( "Invalid hierarchy. %s is already used.", seg ) );
        }

        current.nodes.put ( seg, node );

        return node;
    }

    private DirectoryNode addDirNode ( final DirectoryNode current, final String seg )
    {
        Node g = current.nodes.get ( seg );

        if ( g == null )
        {
            g = new DirectoryNode ();
            current.nodes.put ( seg, g );
            return (DirectoryNode)g;
        }

        if ( g instanceof DirectoryNode )
        {
            return (DirectoryNode)g;
        }

        throw new IllegalStateException ( String.format ( "Invalid group hierarchy. %s is of type %s.", seg, g.getClass () ) );
    }

    public Node findNode ( final Deque<String> segs )
    {
        Node current = this.root;
        while ( !segs.isEmpty () )
        {
            if ( ! ( current instanceof DirectoryNode ) )
            {
                return null;
            }

            final String n = segs.pollFirst ();
            final Node node = ( (DirectoryNode)current ).nodes.get ( n );
            if ( node == null )
            {
                return null;
            }

            current = node;
        }

        return current;
    }

    public String toJson ()
    {
        final Gson gson = makeGson ( false );
        return gson.toJson ( this );
    }

    @Override
    public String toString ()
    {
        final Gson gson = makeGson ( true );
        return gson.toJson ( this );
    }

    public static ChannelData fromJson ( final String json )
    {
        final Gson gson = makeGson ( false );
        return gson.fromJson ( json, ChannelData.class );
    }

    private static Gson makeGson ( final boolean pretty )
    {
        final GsonBuilder gb = new GsonBuilder ();

        if ( pretty )
        {
            gb.setPrettyPrinting ();
        }

        gb.registerTypeAdapter ( Node.class, new NodeAdapter () );

        return gb.create ();
    }

}
