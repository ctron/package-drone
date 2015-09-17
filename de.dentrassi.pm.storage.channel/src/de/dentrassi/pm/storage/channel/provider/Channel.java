package de.dentrassi.pm.storage.channel.provider;

import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperationVoid;
import de.dentrassi.pm.storage.channel.IdTransformer;

public interface Channel
{
    public String getId ();

    public <T> T accessCall ( ChannelOperation<T, AccessContext> operation, IdTransformer idTransformer );

    public <T> T modifyCall ( ChannelOperation<T, ModifyContext> operation, IdTransformer idTransformer );

    public default void accessRun ( final ChannelOperationVoid<AccessContext> operation, final IdTransformer idTransformer )
    {
        accessCall ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public default void modifyRun ( final ChannelOperationVoid<ModifyContext> operation, final IdTransformer idTransformer )
    {
        modifyCall ( channel -> {
            operation.process ( channel );
            return null;
        } , idTransformer );
    }

    public void delete ();
}
