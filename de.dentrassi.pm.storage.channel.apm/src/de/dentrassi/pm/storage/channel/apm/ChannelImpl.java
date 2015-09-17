package de.dentrassi.pm.storage.channel.apm;

import static de.dentrassi.osgi.utils.Exceptions.wrapException;

import org.osgi.service.event.EventAdmin;

import de.dentrassi.pm.apm.StorageManager;
import de.dentrassi.pm.apm.StorageRegistration;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.IdTransformer;
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

    public ChannelImpl ( final String id, final EventAdmin eventAdmin, final MetaKey storageKey, final StorageManager manager, final ChannelProviderImpl provider )
    {
        this.id = id;

        this.storageKey = storageKey;
        this.manager = manager;

        this.provider = provider;

        this.handle = manager.registerModel ( 10_000, storageKey, new ChannelModelProvider ( eventAdmin, id ) );
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

    @Override
    public <T> T accessCall ( final ChannelOperation<T, AccessContext> operation, final IdTransformer idTransformer )
    {
        return this.manager.accessCall ( this.storageKey, AccessContext.class, model -> wrapException ( () -> {
            ( (ModifyContextImpl)model ).setIdTransformer ( idTransformer );
            return operation.process ( model );
        } ) );
    }

    @Override
    public <T> T modifyCall ( final ChannelOperation<T, ModifyContext> operation, final IdTransformer idTransformer )
    {
        return this.manager.modifyCall ( this.storageKey, ModifyContext.class, model -> wrapException ( () -> {
            ( (ModifyContextImpl)model ).setIdTransformer ( idTransformer );
            return operation.process ( model );
        } ) );
    }

    @Override
    public void delete ()
    {
        this.handle.unregister ();
        this.provider.deleteChannel ( this );
    }
}
