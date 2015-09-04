package de.dentrassi.pm.storage.channel.apm.aspect;

import java.util.concurrent.Callable;

import de.dentrassi.osgi.utils.Exceptions;
import de.dentrassi.pm.common.utils.ThrowingRunnable;

public class Guard
{
    private final ThreadLocal<Integer> state = ThreadLocal.withInitial ( () -> 0 );

    private final Runnable guardRunner;

    public Guard ( final Runnable guardRunner )
    {
        this.guardRunner = guardRunner;
    }

    public void guarded ( final ThrowingRunnable action )
    {
        guarded ( () -> {
            action.run ();
            return null;
        } );
    }

    public <T> T guarded ( final Callable<T> action )
    {
        final boolean first = push ();

        try
        {
            final T result = Exceptions.wrapException ( action );

            if ( first )
            {
                // only call if the action was successful and it was the first level
                this.guardRunner.run ();
            }

            return result;
        }
        finally
        {
            pop ();
        }
    }

    private boolean push ()
    {
        final Integer level = this.state.get ();
        this.state.set ( level + 1 );
        return level == 0;
    }

    private void pop ()
    {
        this.state.set ( this.state.get () - 1 );
    }
}
