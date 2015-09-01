package de.dentrassi.pm.storage.channel.internal;

import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.dentrassi.pm.apm.AbstractSimpleStorageModelProvider;
import de.dentrassi.pm.apm.StorageContext;
import de.dentrassi.pm.storage.channel.deploy.DeployGroup;

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
        return context.getBasePath ().resolve ( "channels.json" );
    }

    protected Gson createGson ()
    {
        final GsonBuilder builder = new GsonBuilder ();
        builder.setPrettyPrinting ();
        builder.serializeNulls ();
        builder.setDateFormat ( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
        builder.registerTypeAdapter ( DeployGroup.class, new DeployGroupTypeAdapter () );
        return builder.create ();
    }

    @Override
    protected void persistWriteModel ( final StorageContext context, final ChannelServiceModify writeModel ) throws Exception
    {
        try ( final ReplaceOnCommitOutputStream roc = new ReplaceOnCommitOutputStream ( makePath ( context ) );
              final Writer writer = new OutputStreamWriter ( roc, StandardCharsets.UTF_8 ); )
        {
            createGson ().toJson ( writeModel.getModel (), writer );
            roc.commit ();
        }
    }

    @Override
    protected ChannelServiceModify loadWriteModel ( final StorageContext context ) throws Exception
    {
        try ( Reader reader = Files.newBufferedReader ( makePath ( context ), StandardCharsets.UTF_8 ) )
        {
            final ChannelServiceModel model = createGson ().fromJson ( reader, ChannelServiceModel.class );
            return new ChannelServiceModify ( model != null ? model : new ChannelServiceModel () );
        }
        catch ( final NoSuchFileException e )
        {
            return new ChannelServiceModify ( new ChannelServiceModel () );
        }
    }

}
