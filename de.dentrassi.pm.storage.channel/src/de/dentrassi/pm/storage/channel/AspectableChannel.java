package de.dentrassi.pm.storage.channel;

public interface AspectableChannel
{
    public void addAspects ( boolean withDependencies, String... aspectIds );

    public void removeAspects ( String... aspectIds );

    public void refreshAspects ( String... aspectIds );
}
