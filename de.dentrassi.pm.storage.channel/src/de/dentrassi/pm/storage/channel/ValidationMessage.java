package de.dentrassi.pm.storage.channel;

import de.dentrassi.pm.common.Severity;

public class ValidationMessage
{
    private final Severity severity;

    public ValidationMessage ( final Severity severity )
    {
        this.severity = severity;
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }
}
