package de.dentrassi.pm.aspect.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import de.dentrassi.pm.meta.ChannelAspect;
import de.dentrassi.pm.meta.ChannelAspectFactory;
import de.dentrassi.pm.meta.extract.Extractor;

public class HashAspectFactory implements ChannelAspectFactory
{
    private static final Map<String, HashFunction> functions = new HashMap<> ();

    private static final String ID = "hasher";

    static
    {
        HashAspectFactory.functions.put ( "md5", Hashing.md5 () );
        HashAspectFactory.functions.put ( "sha1", Hashing.sha1 () );
        HashAspectFactory.functions.put ( "sha256", Hashing.sha256 () );
        HashAspectFactory.functions.put ( "sha512", Hashing.sha512 () );
    }

    private static class ChannelAspectImpl implements ChannelAspect
    {
        @Override
        public Extractor getExtractor ()
        {
            return new Extractor () {

                @Override
                public void extractMetaData ( final Path file, final Map<String, String> metadata ) throws Exception
                {
                    makeChecksumMetadata ( file, metadata );
                }

                @Override
                public ChannelAspect getAspect ()
                {
                    return ChannelAspectImpl.this;
                }
            };
        }

        @Override
        public String getId ()
        {
            return ID;
        }
    };

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspectImpl ();
    }

    private static void makeChecksumMetadata ( final Path file, final Map<String, String> metadata ) throws IOException
    {
        final Map<String, HashCode> result = HashHelper.createChecksums ( file, functions );
        for ( final Map.Entry<String, HashCode> entry : result.entrySet () )
        {
            metadata.put ( entry.getKey (), entry.getValue ().toString () );
        }
    }
}
