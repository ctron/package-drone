package de.dentrassi.pm.meta.extract;

import java.nio.file.Path;
import java.util.Map;

public interface Extractor extends ChannelAspectFunction
{
    public void extractMetaData ( Path file, Map<String, String> metadata ) throws Exception;
}
