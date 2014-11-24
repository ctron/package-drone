package de.dentrassi.pm.storage.service;

import java.io.InputStream;

@FunctionalInterface
public interface ArtifactReceiver
{
    public void receive ( ArtifactInformation information, InputStream stream ) throws Exception;
}
