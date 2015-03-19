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
package de.dentrassi.pm.storage.jpa;

import static javax.persistence.FetchType.LAZY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * An entry in the channel cache
 * <p>
 * This is intended for channel aggregators to store binary data which might be
 * re-used by repository adapters. For example the P2 adapter can create a
 * complete metadata file and store it in the channel cache. Speeding up further
 * requests to the P2 adapter by just serving the file from the cache.
 * </p>
 * <p>
 * The reason such content is not stored as artifact is due to the fact that the
 * channel aggregators would create artifacts then, which would require that the
 * channel gets aggregated as well. So the channel cache is outside the
 * aggregation process.
 * </p>
 */
@Entity
@Table ( name = "CHANNEL_CACHE" )
@IdClass ( ChannelCacheKey.class )
public class ChannelCacheEntity
{
    @Id
    @ManyToOne ( fetch = LAZY )
    @JoinColumn ( name = "CHANNEL_ID", referencedColumnName = "ID" )
    private ChannelEntity channel;

    @Id
    @Column ( name = "NS" )
    private String namespace;

    @Id
    @Column ( name = "\"KEY\"" )
    private String key;

    private long size;

    @Column ( length = 255 )
    private String name;

    @Column ( name = "MIME_TYPE", length = 255, nullable = false )
    private String mimeType;

    @Column ( name = "DATA" )
    @Lob
    private byte[] data;

    public ChannelEntity getChannel ()
    {
        return this.channel;
    }

    public void setChannel ( final ChannelEntity channel )
    {
        this.channel = channel;
    }

    public String getNamespace ()
    {
        return this.namespace;
    }

    public void setNamespace ( final String namespace )
    {
        this.namespace = namespace;
    }

    public String getKey ()
    {
        return this.key;
    }

    public void setKey ( final String key )
    {
        this.key = key;
    }

    public byte[] getData ()
    {
        return this.data;
    }

    public void setData ( final byte[] data )
    {
        this.data = data;
    }

    public long getSize ()
    {
        return this.size;
    }

    public void setSize ( final long size )
    {
        this.size = size;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getMimeType ()
    {
        return this.mimeType;
    }

    public void setMimeType ( final String mimeType )
    {
        this.mimeType = mimeType;
    }

}
