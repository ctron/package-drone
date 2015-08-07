package de.dentrassi.pm.storage.channel;

import de.dentrassi.pm.common.Validated;

public class ChannelState implements Validated
{
    private String description;

    private long numberOfArtifacts;

    private long warningCount;

    private long errorCount;

    private boolean locked;

    private ChannelState ()
    {
    }

    private ChannelState ( final ChannelState other )
    {
        this.description = other.description;

        this.numberOfArtifacts = other.numberOfArtifacts;

        this.warningCount = other.warningCount;
        this.errorCount = other.errorCount;

        this.locked = other.locked;
    }

    public String getDescription ()
    {
        return this.description;
    }

    public long getNumberOfArtifacts ()
    {
        return this.numberOfArtifacts;
    }

    @Override
    public long getValidationWarningCount ()
    {
        return this.warningCount;
    }

    @Override
    public long getValidationErrorCount ()
    {
        return this.errorCount;
    }

    public boolean isLocked ()
    {
        return this.locked;
    }

    public static class Builder
    {
        private ChannelState value;

        private boolean needFork;

        public Builder ()
        {
            this.value = new ChannelState ();
        }

        public Builder ( final ChannelState other )
        {
            this.value = other != null ? new ChannelState ( other ) : new ChannelState ();
        }

        public Builder ( final ChannelState other, final ChannelDetails details )
        {
            this ( other );

            if ( details != null )
            {
                this.value.description = details.getDescription ();
            }
        }

        public void setDescription ( final String description )
        {
            checkFork ();
            this.value.description = description;
        }

        public void setNumberOfArtifacts ( final long numberOfArtifacts )
        {
            checkFork ();
            this.value.numberOfArtifacts = numberOfArtifacts;
        }

        public void setLocked ( final boolean locked )
        {
            checkFork ();
            this.value.locked = locked;
        }

        private void checkFork ()
        {
            if ( this.needFork )
            {
                this.needFork = false;
                this.value = new ChannelState ( this.value );
            }
        }

        public ChannelState build ()
        {
            this.needFork = true;
            return this.value;
        }
    }

}
