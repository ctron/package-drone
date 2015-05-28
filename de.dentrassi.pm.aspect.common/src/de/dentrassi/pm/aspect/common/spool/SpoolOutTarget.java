package de.dentrassi.pm.aspect.common.spool;

import java.io.IOException;
import java.io.OutputStream;

import de.dentrassi.pm.common.utils.IOConsumer;

@FunctionalInterface
public interface SpoolOutTarget
{
    public void spoolOut ( final String fileName, final String mimeType, final IOConsumer<OutputStream> stream ) throws IOException;
}
