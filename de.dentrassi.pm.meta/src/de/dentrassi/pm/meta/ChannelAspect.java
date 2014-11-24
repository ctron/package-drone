package de.dentrassi.pm.meta;

import de.dentrassi.pm.meta.extract.Extractor;

public interface ChannelAspect
{
    /**
     * @return an extractor or <code>null</code>
     */
    public Extractor getExtractor ();

    /**
     * Get the factory id
     * 
     * @return the factory id
     */
    public String getId ();
}
