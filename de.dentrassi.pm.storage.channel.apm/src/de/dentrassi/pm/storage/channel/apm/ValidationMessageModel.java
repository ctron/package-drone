package de.dentrassi.pm.storage.channel.apm;

import java.util.HashSet;
import java.util.Set;

import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.storage.channel.ValidationMessage;

public class ValidationMessageModel
{
    private String aspectId;

    private Severity severity;

    private String message;

    private Set<String> artifactIds;

    public void setAspectId ( final String aspectId )
    {
        this.aspectId = aspectId;
    }

    public String getAspectId ()
    {
        return this.aspectId;
    }

    public void setSeverity ( final Severity severity )
    {
        this.severity = severity;
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public void setArtifactIds ( final Set<String> artifactIds )
    {
        this.artifactIds = artifactIds;
    }

    public Set<String> getArtifactIds ()
    {
        return this.artifactIds;
    }

    public static ValidationMessage toMessage ( final ValidationMessageModel model )
    {
        return new ValidationMessage ( model.getAspectId (), model.getSeverity (), model.getMessage (), model.getArtifactIds () );
    }

    public static ValidationMessageModel fromMessage ( final ValidationMessage msg )
    {
        final ValidationMessageModel result = new ValidationMessageModel ();

        result.setSeverity ( msg.getSeverity () );
        result.setAspectId ( msg.getAspectId () );
        result.setMessage ( msg.getMessage () );
        result.setArtifactIds ( new HashSet<> ( msg.getArtifactIds () ) );

        return result;
    }
}
