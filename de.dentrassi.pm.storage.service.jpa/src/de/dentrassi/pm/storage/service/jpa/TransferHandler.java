/*******************************************************************************
 * Copyright (c)  2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.io.ByteStreams;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.jpa.ArtifactEntity;
import de.dentrassi.pm.storage.jpa.AttachedArtifactEntity;
import de.dentrassi.pm.storage.jpa.ChannelEntity;
import de.dentrassi.pm.storage.jpa.GeneratorArtifactEntity;
import de.dentrassi.pm.storage.jpa.PropertyEntity;
import de.dentrassi.pm.storage.jpa.RootArtifactEntity;

/**
 * A handler for exporting and importing channel data
 */
public class TransferHandler extends AbstractHandler implements StreamServiceHelper
{

    public static class PropertyComparator implements Comparator<PropertyEntity>
    {
        public static final Comparator<PropertyEntity> COMPARATOR = new PropertyComparator ();

        @Override
        public int compare ( final PropertyEntity o1, final PropertyEntity o2 )
        {
            final int rc = o1.getNamespace ().compareTo ( o2.getNamespace () );
            if ( rc != 0 )
            {
                return rc;
            }
            return o1.getKey ().compareTo ( o2.getKey () );
        }
    }

    public TransferHandler ( final EntityManager em, final LockManager<String> lockManager )
    {
        super ( em, lockManager );
    }

    public void exportChannel ( final String channelId, final OutputStream stream ) throws IOException
    {
        this.lockManager.accessCall ( channelId, ( ) -> {
            final ChannelEntity channel = getCheckedChannel ( channelId );
            final ZipOutputStream zos = new ZipOutputStream ( stream );

            putDataEntry ( zos, "version", "1" );
            putDataEntry ( zos, "name", channel.getName () );
            putDataEntry ( zos, "description", channel.getDescription () );
            putDirEntry ( zos, "artifacts" );
            putProperties ( zos, "properties.xml", channel.getProvidedProperties () );
            putAspects ( zos, channel.getAspects () );
            putArtifacts ( zos, "artifacts/", channel.getArtifacts (), true );

            zos.finish ();

            return null;
        } );

    }

    private void putArtifacts ( final ZipOutputStream zos, final String baseName, final Collection<? extends ArtifactEntity> artifacts, final boolean onlyRoot ) throws IOException
    {
        for ( final ArtifactEntity art : artifacts )
        {
            if ( ! ( art instanceof RootArtifactEntity || art instanceof AttachedArtifactEntity ) )
            {
                continue;
            }

            if ( onlyRoot && ! ( art instanceof RootArtifactEntity ) )
            {
                // only root artifacts in this run
                continue;
            }

            final String name = String.format ( "%s%s/", baseName, art.getId () );

            // make a dir entry
            {
                final ZipEntry ze = new ZipEntry ( name );
                ze.setComment ( art.getName () );

                final FileTime timestamp = FileTime.fromMillis ( art.getCreationTimestamp ().getTime () );

                ze.setLastModifiedTime ( timestamp );
                ze.setCreationTime ( timestamp );
                ze.setLastAccessTime ( timestamp );

                zos.putNextEntry ( ze );
                zos.closeEntry ();
            }

            // put the provided properties
            putProperties ( zos, name + "properties.xml", art.getProvidedProperties () );
            putDataEntry ( zos, name + "name", art.getName () );

            if ( art instanceof GeneratorArtifactEntity )
            {
                putDataEntry ( zos, name + "generator", ( (GeneratorArtifactEntity)art ).getGeneratorId () );
            }

            // put the blob
            try
            {
                internalStreamArtifact ( this.em, art, ( in ) -> {
                    zos.putNextEntry ( new ZipEntry ( name + "data" ) );
                    ByteStreams.copy ( in, zos );
                    zos.closeEntry ();
                } );
            }
            catch ( final Exception e )
            {
                throw new IOException ( "Failed to export artifact", e );
            }

            // put children
            putArtifacts ( zos, name, art.getChildArtifacts (), false );
        }
    }

    private void putAspects ( final ZipOutputStream zos, final Set<String> aspects ) throws IOException
    {
        final List<String> list = new ArrayList<> ( aspects );
        Collections.sort ( list );

        final StringBuilder sb = new StringBuilder ();

        for ( final String aspect : list )
        {
            sb.append ( aspect ).append ( '\n' );
        }

        putDataEntry ( zos, "aspects", sb.toString () );
    }

    private void putProperties ( final ZipOutputStream zos, final String name, final Collection<? extends PropertyEntity> properties ) throws IOException
    {
        if ( properties.isEmpty () )
        {
            return;
        }

        zos.putNextEntry ( new ZipEntry ( name ) );

        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element root = doc.createElement ( "properties" );
        doc.appendChild ( root );

        final List<PropertyEntity> list = new ArrayList<> ( properties );
        Collections.sort ( list, PropertyComparator.COMPARATOR );

        for ( final PropertyEntity pe : list )
        {
            final Element p = XmlHelper.addElement ( root, "property" );
            p.setAttribute ( "namespace", pe.getNamespace () );
            p.setAttribute ( "key", pe.getKey () );
            p.setTextContent ( pe.getValue () );
        }

        try
        {
            xml.write ( doc, zos );
            zos.closeEntry ();
        }
        catch ( final Exception e )
        {
            throw new IOException ( "Failed to serialize XML", e );
        }
    }

    private void putDirEntry ( final ZipOutputStream zos, String name ) throws IOException
    {
        if ( !name.endsWith ( "/" ) )
        {
            name = name + "/";
        }

        final ZipEntry entry = new ZipEntry ( name );
        zos.putNextEntry ( entry );
        zos.closeEntry ();
    }

    private void putDataEntry ( final ZipOutputStream stream, final String name, final String data ) throws IOException
    {
        if ( data == null )
        {
            return;
        }
        stream.putNextEntry ( new ZipEntry ( name ) );
        stream.write ( data.getBytes ( StandardCharsets.UTF_8 ) );
        stream.closeEntry ();
    }

}
