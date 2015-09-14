package de.dentrassi.pm.storage.channel.apm.aspect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.dentrassi.pm.aspect.aggregate.AggregationContext;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.common.utils.IOConsumer;
import de.dentrassi.pm.storage.channel.ArtifactInformation;
import de.dentrassi.pm.storage.channel.ChannelDetails;
import de.dentrassi.pm.storage.channel.ChannelService.ArtifactReceiver;
import de.dentrassi.pm.storage.channel.ValidationMessage;

public class AggregationContextImpl implements AggregationContext
{
    private final AspectableContext ctx;

    private final String aspectId;

    private final String channelId;

    private final Supplier<ChannelDetails> details;

    private final Consumer<ValidationMessage> msgHandler;

    public AggregationContextImpl ( final AspectableContext ctx, final String aspectId, final String channelId, final Supplier<ChannelDetails> details, final Consumer<ValidationMessage> msgHandler )
    {
        this.ctx = ctx;
        this.aspectId = aspectId;
        this.channelId = channelId;

        this.details = details;

        this.msgHandler = msgHandler;
    }

    @Override
    public void validationMessage ( final Severity severity, final String message, final Set<String> artifactIds )
    {
        this.msgHandler.accept ( new ValidationMessage ( this.aspectId, severity, message, artifactIds ) );
    }

    @Override
    public Collection<ArtifactInformation> getArtifacts ()
    {
        return Collections.unmodifiableCollection ( this.ctx.getArtifacts ().values () );
    }

    @Override
    public String getChannelId ()
    {
        return this.channelId;
    }

    @Override
    public String getChannelDescription ()
    {
        return this.details.get ().getDescription ();
    }

    @Override
    public Map<MetaKey, String> getChannelMetaData ()
    {
        return Collections.unmodifiableMap ( this.ctx.getChannelProvidedMetaData () );
    }

    @Override
    public void createCacheEntry ( final String id, final String name, final String mimeType, final IOConsumer<OutputStream> creator ) throws IOException
    {
        this.ctx.createCacheEntry ( new MetaKey ( this.aspectId, id ), name, mimeType, creator );
    }

    @Override
    public boolean streamArtifact ( final String artifactId, final ArtifactReceiver receiver ) throws IOException
    {
        final ArtifactInformation artifact = this.ctx.getArtifacts ().get ( artifactId );
        if ( artifact == null )
        {
            return false;
        }

        return this.ctx.stream ( artifactId, stream -> receiver.consume ( artifact, stream ) );
    }

    @Override
    public boolean streamArtifact ( final String artifactId, final IOConsumer<InputStream> consumer ) throws IOException
    {
        return this.ctx.stream ( artifactId, consumer );
    }
}
