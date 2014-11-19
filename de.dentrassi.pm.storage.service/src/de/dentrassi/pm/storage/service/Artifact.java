package de.dentrassi.pm.storage.service;

import java.io.InputStream;
import java.util.function.Consumer;

public interface Artifact
{
    public Channel getChannel ();

    public String getId ();

    public void streamData ( Consumer<InputStream> consumer );
}
