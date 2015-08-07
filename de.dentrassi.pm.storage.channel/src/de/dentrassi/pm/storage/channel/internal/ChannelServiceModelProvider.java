package de.dentrassi.pm.storage.channel.internal;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import de.dentrassi.pm.apm.AbstractSimpleStorageModelProvider;
import de.dentrassi.pm.apm.StorageContext;

public class ChannelServiceModelProvider extends AbstractSimpleStorageModelProvider<ChannelServiceAccess, ChannelServiceModify>
{

    public ChannelServiceModelProvider ()
    {
        super ( ChannelServiceAccess.class, ChannelServiceModify.class );
    }

    @Override
    protected ChannelServiceAccess makeViewModelTyped ( final ChannelServiceModify writeModel )
    {
        return writeModel;
    }

    @Override
    protected ChannelServiceModify cloneWriteModel ( final ChannelServiceModify writeModel )
    {
        return new ChannelServiceModify ( writeModel );
    }

    private Path makePath ( final StorageContext context )
    {
        return context.getBasePath ().resolve ( "channelMappings.properties" );
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ChannelServiceModify writeModel ) throws Exception
    {
        try ( Writer writer = Files.newBufferedWriter ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            final Properties p = new Properties ();
            for ( final Map.Entry<String, String> entry : writeModel.getMap ().entrySet () )
            {
                final String id = entry.getKey ();
                final String name = entry.getValue ();

                if ( id != null && name != null && !id.isEmpty () && !name.isEmpty () )
                {
                    p.put ( id, name );
                }
            }
            p.store ( writer, null );
        }
    }

    @Override
    protected ChannelServiceModify loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            final Properties p = new Properties ();
            p.load ( reader );

            final BiMap<String, String> map = HashBiMap.create ( p.size () );

            for ( final Map.Entry<Object, Object> entry : p.entrySet () )
            {
                if ( entry.getKey () instanceof String && entry.getValue () instanceof String )
                {
                    map.put ( (String)entry.getKey (), (String)entry.getValue () );
                }
            }

            return new ChannelServiceModify ( map );
        }
        catch ( final NoSuchFileException e )
        {
            return new ChannelServiceModify ( HashBiMap.create () );
        }
    }

}
