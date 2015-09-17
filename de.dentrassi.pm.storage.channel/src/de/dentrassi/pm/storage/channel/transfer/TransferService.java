package de.dentrassi.pm.storage.channel.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.dentrassi.pm.storage.channel.ChannelId;

public interface TransferService
{
    public void exportChannel ( String channelId, OutputStream stream ) throws IOException;

    public void exportAll ( OutputStream stream ) throws IOException;

    public ChannelId importChannel ( InputStream inputStream, boolean useName ) throws IOException;

    public void importAll ( InputStream stream, boolean useNames, boolean wipe ) throws IOException;
}
