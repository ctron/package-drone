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
package de.dentrassi.pm.storage;

import java.util.Collections;
import java.util.SortedSet;

import de.dentrassi.pm.common.Severity;

public class ValidationMessage
{
    private final Severity severity;

    private final String message;

    private final SortedSet<String> artifactIds;

    private final String aspectId;

    public ValidationMessage ( final Severity severity, final String message, final String aspectId, final SortedSet<String> artifactIds )
    {
        this.severity = severity;
        this.aspectId = aspectId;
        this.message = message;
        this.artifactIds = Collections.unmodifiableSortedSet ( artifactIds );
    }

    public Severity getSeverity ()
    {
        return this.severity;
    }

    public String getAspectId ()
    {
        return this.aspectId;
    }

    public String getMessage ()
    {
        return this.message;
    }

    public SortedSet<String> getArtifactIds ()
    {
        return this.artifactIds;
    }
}
