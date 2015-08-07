package de.dentrassi.pm.storage.channel.apm;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.LongSerializationPolicy;

import de.dentrassi.pm.apm.AbstractSimpleStorageModelProvider;
import de.dentrassi.pm.apm.StorageContext;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.apm.blob.BlobStore;
import de.dentrassi.pm.storage.channel.apm.blob.BlobStore.Transaction;
import de.dentrassi.pm.storage.channel.provider.AccessContext;

public class ChannelModelProvider extends AbstractSimpleStorageModelProvider<AccessContext, ModifyContextImpl>
{
    private final static Logger logger = LoggerFactory.getLogger ( ChannelModelProvider.class );

    private final String channelId;

    private BlobStore store;

    public ChannelModelProvider ( final String channelId )
    {
        super ( AccessContext.class, ModifyContextImpl.class );

        this.channelId = channelId;
    }

    @Override
    public void start ( final StorageContext context ) throws Exception
    {
        this.store = new BlobStore ( makeBasePath ( context, this.channelId ).resolve ( "blobs" ) );
        super.start ( context );
    }

    @Override
    public void stop ()
    {
        super.stop ();
        this.store.close ();
    }

    @Override
    protected AccessContext makeViewModelTyped ( final ModifyContextImpl writeModel )
    {
        return writeModel;
    }

    @Override
    protected ModifyContextImpl cloneWriteModel ( final ModifyContextImpl writeModel )
    {
        return new ModifyContextImpl ( writeModel );
    }

    public static Path makeBasePath ( final StorageContext context, final String channelId )
    {
        return context.getBasePath ().resolve ( Paths.get ( "channels", channelId ) );
    }

    public static Path makeStatePath ( final StorageContext context, final String channelId )
    {
        return makeBasePath ( context, channelId ).resolve ( "state.json" );
    }

    private Gson createGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();

        builder.setPrettyPrinting ();
        builder.serializeNulls ();
        builder.setLongSerializationPolicy ( LongSerializationPolicy.STRING );
        builder.registerTypeAdapter ( MetaKey.class, new JsonDeserializer<MetaKey> () {

            @Override
            public MetaKey deserialize ( final JsonElement json, final Type type, final JsonDeserializationContext ctx ) throws JsonParseException
            {
                return MetaKey.fromString ( json.getAsString () );
            }
        } );

        return builder.create ();
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ModifyContextImpl writeModel ) throws Exception
    {
        Transaction t = writeModel.claimTransaction ();

        try
        {
            final Path path = makeStatePath ( context, this.channelId );
            Files.createDirectories ( path.getParent () );

            if ( t != null )
            {
                t.commit ();
                t = null;
            }

            try ( Writer writer = Files.newBufferedWriter ( path, StandardCharsets.UTF_8 ) )
            {
                final Gson gson = createGson ();
                gson.toJson ( writeModel.getModel (), writer );
            }
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to persist model", e );
            throw e;
        }
        finally
        {
            if ( t != null )
            {
                t.rollback ();
            }
        }
    }

    @Override
    protected ModifyContextImpl loadWriteModel ( final StorageContext context ) throws Exception
    {
        final Path path = makeStatePath ( context, this.channelId );

        try ( Reader reader = Files.newBufferedReader ( path, StandardCharsets.UTF_8 ) )
        {
            final Gson gson = createGson ();
            final ChannelModel model = gson.fromJson ( reader, ChannelModel.class );
            return new ModifyContextImpl ( this.store, model );
        }
        catch ( final NoSuchFileException e )
        {
            // create a new model
            return new ModifyContextImpl ( this.store, new ChannelModel () );
        }
    }

}
