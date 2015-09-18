package de.dentrassi.pm.storage.channel.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.dentrassi.osgi.profiler.Profile;
import de.dentrassi.osgi.profiler.Profile.Handle;
import de.dentrassi.pm.aspect.ChannelAspectProcessor;
import de.dentrassi.pm.common.ChannelAspectInformation;
import de.dentrassi.pm.storage.channel.ChannelId;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.provider.ModifyContext;

public class ModifiableChannelAdapter extends ReadableChannelAdapter implements ModifiableChannel
{
    private final ModifyContext context;

    private final ChannelAspectProcessor aspectProcessor;

    public ModifiableChannelAdapter ( final ChannelId descriptor, final ModifyContext context, final ChannelAspectProcessor aspectProcessor )
    {
        super ( descriptor, context );
        this.context = context;
        this.aspectProcessor = aspectProcessor;
    }

    @Override
    public ModifyContext getContext ()
    {
        return this.context;
    }

    @Override
    public void addAspects ( final boolean withDependencies, final String... aspectIds )
    {
        final Set<String> aspects = new HashSet<> ( Arrays.asList ( aspectIds ) );

        if ( withDependencies )
        {
            getContext ().addAspects ( expandDependencies ( aspects ) );
        }
        else
        {
            getContext ().addAspects ( aspects );
        }
    }

    private Set<String> expandDependencies ( final Set<String> aspects )
    {
        try ( Handle handle = Profile.start ( ModifiableChannelAdapter.class.getName () + ".expandDependencies" ) )
        {
            final Map<String, ChannelAspectInformation> all = this.aspectProcessor.getAspectInformations ();

            final Set<String> result = new HashSet<> ();
            final TreeSet<String> requested = new TreeSet<> ();
            requested.addAll ( aspects );

            while ( !requested.isEmpty () )
            {
                final String id = requested.pollFirst ();

                if ( result.add ( id ) )
                {
                    final ChannelAspectInformation asp = all.get ( id );

                    final Set<String> reqs = new HashSet<> ( asp.getRequires () );
                    reqs.removeAll ( requested ); // remove all which are already present
                    requested.addAll ( reqs ); // add to request list
                }
            }

            return result;
        }
    }

}
