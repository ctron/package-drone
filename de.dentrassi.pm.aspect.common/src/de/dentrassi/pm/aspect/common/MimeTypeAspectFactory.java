package de.dentrassi.pm.aspect.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import de.dentrassi.pm.meta.ChannelAspect;
import de.dentrassi.pm.meta.ChannelAspectFactory;
import de.dentrassi.pm.meta.extract.Extractor;

public class MimeTypeAspectFactory implements ChannelAspectFactory
{
    public static final String ID = "mime";

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public ChannelAspect getAspect ()
                {
                    return ChannelAspectImpl.this;
                }

                @Override
                public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
                {
                    final String type = Files.probeContentType ( file );
                    if ( type != null )
                    {
                        metadata.put ( "type", type );
                    }
                }
            };
        }

        @Override
        public String getId ()
        {
            return ID;
        }

    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

}
