package de.dentrassi.pm.storage.channel.apm;

import java.util.concurrent.Callable;

import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.apm.StorageRegistration;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.provider.AccessContext;
import de.dentrassi.pm.storage.channel.provider.Channel;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public class ChannelImpl implements Channel
{
    private final String id;

    private final MetaKey storageKey;

    private final StorageManager manager;

    private final ChannelProviderImpl provider;

    private final StorageRegistration handle;

    public ChannelImpl ( final String id, final MetaKey storageKey, final StorageManager manager, final ChannelProviderImpl provider )
    {
        this.id = id;
        this.storageKey = storageKey;
        this.manager = manager;

        this.provider = provider;

        this.handle = manager.registerModel ( 10_000, storageKey, new ChannelModelProvider ( id ) );
    }

    public void dispose ()
    {
        this.handle.unregister ();
    }

    @Override
    public String getId ()
    {
        return this.id;
    }

    private static <T> T ignoreException ( final Callable<T> callable )
    {
        try
        {
            return callable.call ();
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( e );
        }
    }

    @Override
    public <T> T access ( final ChannelOperation<T, AccessContext> operation )
    {
        return this.manager.accessCall ( this.storageKey, AccessContext.class, model -> ignoreException ( () -> operation.process ( model ) ) );
    }

    @Override
    public <T> T modify ( final ChannelOperation<T, ModifyContext> operation )
    {
        return this.manager.modifyCall ( this.storageKey, ModifyContext.class, model -> ignoreException ( () -> operation.process ( model ) ) );
    }

    @Override
    public void delete ()
    {
        this.handle.unregister ();
        this.provider.deleteChannel ( this );
    }
}
