/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.web.channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dentrassi.pm.common.Severity;
import de.dentrassi.pm.common.SimpleArtifactInformation;

public class TreeTester
{

    private final Map<String, List<SimpleArtifactInformation>> tree;

    private final Map<String, Severity> cache = new HashMap<> ();

    public TreeTester ( final Map<String, List<SimpleArtifactInformation>> tree )
    {
        this.tree = tree;
    }

    public Severity getState ( final SimpleArtifactInformation artifact )
    {
        if ( this.cache.containsKey ( artifact.getId () ) )
        {
            return this.cache.get ( artifact.getId () );
        }

        final Severity sev = evalSeverity ( artifact );
        this.cache.put ( artifact.getId (), sev );
        return sev;
    }

    private Severity evalSeverity ( final SimpleArtifactInformation artifact )
    {
        final Severity sev = artifact.getOverallValidationState ();
        if ( sev == Severity.ERROR )
        {
            return sev;
        }

        final Severity childSev = getChildState ( artifact );
        if ( childSev == null )
        {
            return sev;
        }

        if ( sev == null )
        {
            return childSev;
        }

        if ( childSev.ordinal () > sev.ordinal () )
        {
            return childSev;
        }
        return sev;
    }

    private Severity getChildState ( final SimpleArtifactInformation artifact )
    {
        final List<SimpleArtifactInformation> childs = this.tree.get ( artifact.getId () );

        if ( childs == null )
        {
            return null;
        }

        Severity maxSev = null;

        for ( final SimpleArtifactInformation child : childs )
        {
            final Severity sev = getState ( child );
            if ( sev == Severity.ERROR )
            {
                return Severity.ERROR;
            }

            maxSev = sev;
        }

        return maxSev;
    }

}
