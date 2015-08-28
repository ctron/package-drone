package de.dentrassi.pm.storage.channel.apm.internal;

import java.util.LinkedList;
import java.util.List;

public class Finally
{
    private final List<Runnable> runnables = new LinkedList<> ();

    public Finally ()
    {
    }

    public void add ( final Runnable r )
    {
        this.runnables.add ( r );
    }

    public void runAll ()
    {
        LinkedList<Throwable> errors = null;

        for ( final Runnable r : this.runnables )
        {
            try
            {
                r.run ();
            }
            catch ( final Throwable e )
            {
                if ( errors == null )
                {
                    errors = new LinkedList<> ();
                }
                errors.add ( e );
            }
        }

        if ( errors != null )
        {
            final RuntimeException e = new RuntimeException ( "Failed to process finally", errors.pollFirst () );
            errors.stream ().forEach ( e::addSuppressed );
            throw e;
        }
    }
}
