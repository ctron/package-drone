package de.dentrassi.pm.storage.channel.apm.aspect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import de.dentrassi.osgi.utils.Exceptions.ThrowingRunnable;

public class RegenerationTracker
{
    private final Consumer<Set<String>> func;

    private final ThreadLocal<LinkedList<Set<String>>> states = ThreadLocal.withInitial ( LinkedList::new );

    public RegenerationTracker ( final Consumer<Set<String>> regenerationFunc )
    {
        this.func = regenerationFunc;
    }

    public void run ( final ThrowingRunnable action )
    {
        run ( () -> {
            action.run ();
            return null;
        } );
    }

    public <T> T run ( final Callable<T> action )
    {
        this.states.get ().push ( new HashSet<> () );

        try
        {
            final T result = action.call ();

            final LinkedList<Set<String>> state = this.states.get ();
            if ( state.size () > 1 )
            {
                // push my items, to my parent
                final Iterator<Set<String>> i = state.iterator ();
                final Set<String> mine = i.next ();
                i.next ().addAll ( mine );
                mine.clear ();
            }
            else
            {
                // flush
                flushAll ();
            }

            return result;
        }
        catch ( final Exception e )
        {
            this.states.get ().peek ().clear (); // clear the current state, the finally will remove it
            throw new RuntimeException ( e );
        }
        finally
        {
            final Set<String> current = this.states.get ().poll ();
            if ( !current.isEmpty () )
            {
                throw new IllegalStateException ( "There are still marked artifacts in the finally section" );
            }
        }
    }

    private void flushAll ()
    {
        while ( !this.states.get ().peek ().isEmpty () )
        {
            // swap with new set
            final Set<String> current = this.states.get ().poll ();
            this.states.get ().add ( new HashSet<> () );

            // process set
            this.func.accept ( current );
        }
    }

    public void mark ( final String artifactId )
    {
        final LinkedList<Set<String>> state = this.states.get ();
        final Set<String> current = state.peek ();
        if ( current == null )
        {
            throw new IllegalStateException ( "No regeneration context" );
        }
        current.add ( artifactId );
    }
}
