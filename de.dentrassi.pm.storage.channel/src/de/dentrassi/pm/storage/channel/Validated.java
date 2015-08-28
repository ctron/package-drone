package de.dentrassi.pm.storage.channel;

import java.util.Collection;

import de.dentrassi.pm.common.Severity;

public interface Validated
{
    public Collection<ValidationMessage> getValidationMessages ();

    public default long getValidationErrorCount ()
    {
        return getValidationMessages ().stream ().filter ( msg -> Severity.ERROR == msg.getSeverity () ).count ();
    }

    public default long getValidationWarningCount ()
    {
        return getValidationMessages ().stream ().filter ( msg -> Severity.WARNING == msg.getSeverity () ).count ();
    }

    /**
     * Get the overall validation state
     * <p>
     * This method will return {@link Severity#ERROR} if there are errors
     * present. If not then it will return {@link Severity#WARNING} if there are
     * warnings present. If not, then it will return <code>null</code>.
     * </p>
     *
     * @return the calculated overall validation state
     */
    public default Severity getOverallValidationState ()
    {
        if ( getValidationErrorCount () > 0 )
        {
            return Severity.ERROR;
        }
        else if ( getValidationWarningCount () > 0 )
        {
            return Severity.WARNING;
        }
        return null;
    }
}
