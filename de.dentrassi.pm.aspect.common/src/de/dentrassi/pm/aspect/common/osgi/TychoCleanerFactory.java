/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.aspect.common.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import de.dentrassi.pm.aspect.ChannelAspect;
import de.dentrassi.pm.aspect.ChannelAspectFactory;
import de.dentrassi.pm.aspect.listener.ChannelListener;
import de.dentrassi.pm.aspect.listener.ChannelListenerAdapter;
import de.dentrassi.pm.aspect.listener.PreAddContext;

public class TychoCleanerFactory implements ChannelAspectFactory
{
    private static final String ID = "tycho-cleaner";

    private static List<Pattern> ignoredPatterns = new LinkedList<> ();

    static
    {
        ignoredPatterns.add ( Pattern.compile ( ".*-p2artifacts.xml$" ) );
    }

    @Override
    public ChannelAspect createAspect ()
    {
        return new ChannelAspect () {

            @Override
            public String getId ()
            {
                return ID;
            }

            @Override
            public ChannelListener getChannelListener ()
            {
                return new ChannelListenerAdapter () {

                    @Override
                    public void artifactPreAdd ( final PreAddContext context )
                    {
                        final String name = context.getName ();
                        for ( final Pattern pattern : ignoredPatterns )
                        {
                            if ( pattern.matcher ( name ).matches () )
                            {
                                context.vetoAdd ();
                                return;
                            }
                        }
                    }
                };
            }
        };
    }
}
