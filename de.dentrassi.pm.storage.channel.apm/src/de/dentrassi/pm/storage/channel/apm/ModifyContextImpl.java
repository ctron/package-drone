package de.dentrassi.pm.storage.channel.apm;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.ChannelState;
import de.dentrassi.pm.storage.channel.ChannelState.Builder;
import de.dentrassi.pm.storage.channel.apm.blob.BlobStore;
import de.dentrassi.pm.storage.channel.apm.blob.BlobStore.Transaction;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public class ModifyContextImpl implements ModifyContext
{
    private final BlobStore store;

    private final ChannelModel model;

    private final SortedMap<MetaKey, String> metaData;

    private final SortedMap<MetaKey, String> modMetaData;

    private final List<ArtifactInformation> artifacts;

    private final Builder state;

    private Transaction transaction;

    public ModifyContextImpl ( final BlobStore store, final ChannelModel other )
    {
        this.store = store;
        this.model = new ChannelModel ( other );

        this.modMetaData = new TreeMap<> ( other.getMetaData () );
        this.metaData = Collections.unmodifiableSortedMap ( this.modMetaData );

        this.artifacts = other.getArtifacts ().entrySet ().stream ().map ( ArtifactModel::toInformation ).collect ( toList () );

        this.state = new ChannelState.Builder ();
        this.state.setDescription ( other.getDescription () );
    }

    public ModifyContextImpl ( final ModifyContextImpl other )
    {
        this ( other.store, other.getModel () );

        // FIXME: prevent unnecessary copies
    }

    public ChannelModel getModel ()
    {
        return this.model;
    }

    @Override
    public ChannelState getState ()
    {
        return this.state.build (); // will only create a new instance when necessary
    }

    @Override
    public SortedMap<MetaKey, String> getMetaData ()
    {
        return this.metaData;
    }

    @Override
    public Collection<ArtifactInformation> getArtifacts ()
    {
        return this.artifacts;
    }

    @Override
    public void setDetails ( final ChannelDetails details )
    {
        this.state.setDescription ( details.getDescription () );
        this.model.setDescription ( details.getDescription () );
    }

    @Override
    public void applyMetaData ( final Map<MetaKey, String> changes )
    {
        testLocked ();

        for ( final Map.Entry<MetaKey, String> entry : changes.entrySet () )
        {
            final MetaKey key = entry.getKey ();
            final String value = entry.getValue ();

            if ( value == null )
            {
                this.modMetaData.remove ( value );
                this.model.getMetaData ().remove ( value );
            }
            else
            {
                this.modMetaData.put ( key, value );
                this.model.getMetaData ().put ( key, value );
            }
        }
    }

    private void testLocked ()
    {
        if ( this.model.isLocked () )
        {
            throw new IllegalStateException ( "Channel is locked" );
        }
    }

    @Override
    public void lock ()
    {
        this.state.setLocked ( true );
        this.model.setLocked ( true );
    }

    @Override
    public void unlock ()
    {
        this.state.setLocked ( false );
        this.model.setLocked ( false );
    }

    @Override
    public ArtifactInformation createArtifact ( final InputStream source, final String name, final Map<MetaKey, String> providedMetaData )
    {
        if ( this.transaction == null )
        {
            this.transaction = this.store.start ();
        }

        final String id = UUID.randomUUID ().toString ();

        try
        {
            final long size = this.transaction.create ( id, source );
            final ArtifactInformation ai = new ArtifactInformation ( id, name, size, Instant.now (), Collections.emptySet () /* FIXME: provide real list  */ );
            this.model.addArtifact ( ai );
            this.artifacts.add ( ai );
            return ai;
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to create artifact", e );
        }
    }

    @Override
    public boolean deleteArtifact ( final String id )
    {
        if ( this.transaction == null )
        {
            this.transaction = this.store.start ();
        }

        try
        {
            return this.transaction.delete ( id );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( "Failed to delete artifact", e );
        }
    }

    public Transaction claimTransaction ()
    {
        final Transaction t = this.transaction;
        this.transaction = null;
        return t;
    }
}
