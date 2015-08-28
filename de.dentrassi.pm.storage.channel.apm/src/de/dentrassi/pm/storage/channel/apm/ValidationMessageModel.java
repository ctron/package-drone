package de.dentrassi.pm.storage.channel.apm;

import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.channel.ValidationMessage;

public class ValidationMessageModel
{
    private Severity severity;

    public void setSeverity ( final Severity severity )
    {
        this.severity = severity;
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }

    public static ValidationMessage toMessage ( final ValidationMessageModel model )
    {
        return new ValidationMessage ( model.getSeverity () );
    }

    public static ValidationMessageModel fromMessage ( final ValidationMessage msg )
    {
        final ValidationMessageModel result = new ValidationMessageModel ();

        result.setSeverity ( msg.getSeverity () );

        return result;
    }
}
