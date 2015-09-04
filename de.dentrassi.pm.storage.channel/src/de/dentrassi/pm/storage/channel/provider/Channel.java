package de.dentrassi.pm.storage.channel.provider;

import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperationVoid;
import de.dentrassi.pm.storage.channel.IdTransformer;

public interface Channel
{
    public String getId ();

    public <T> T access ( ChannelOperation<T, AccessContext> operation, IdTransformer idTransformer );

    public <T> T modify ( ChannelOperation<T, ModifyContext> operation, IdTransformer idTransformer );

    public default void access ( final ChannelOperationVoid<AccessContext> operation, final IdTransformer idTransformer )
    {
        access ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public default void modify ( final ChannelOperationVoid<ModifyContext> operation, final IdTransformer idTransformer )
    {
        modify ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public void delete ();
}
