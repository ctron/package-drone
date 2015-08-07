package de.dentrassi.pm.storage.channel.provider;

import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperation;
import de.dentrassi.pm.storage.channel.ChannelService.ChannelOperationVoid;

public interface Channel
{
    public String getId ();

    public <T> T access ( ChannelOperation<T, AccessContext> operation );

    public <T> T modify ( ChannelOperation<T, ModifyContext> operation );

    public default void access ( final ChannelOperationVoid<AccessContext> operation )
    {
        access ( channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public default void modify ( final ChannelOperationVoid<ModifyContext> operation )
    {
        modify ( channel -> {
            operation.process ( channel );
            return null;
        } );
    }

    public void delete ();
}
