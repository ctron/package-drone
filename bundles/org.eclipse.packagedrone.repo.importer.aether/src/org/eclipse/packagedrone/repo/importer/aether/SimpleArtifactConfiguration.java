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
package org.eclipse.packagedrone.repo.importer.aether;

import org.eclipse.packagedrone.utils.converter.JSON;

@JSON
public class SimpleArtifactConfiguration
{
    private String url;

    private String dependencies;

    private boolean includeSources;

    private boolean resolveDependencies;

    public void setUrl ( final String url )
    {
        this.url = url;
    }

    public String getUrl ()
    {
        return this.url;
    }

    public void setDependencies ( final String dependencies )
    {
        this.dependencies = dependencies;
    }

    public String getDependencies ()
    {
        return this.dependencies;
    }

    public void setIncludeSources ( final boolean includeSources )
    {
        this.includeSources = includeSources;
    }

    public boolean isIncludeSources ()
    {
        return this.includeSources;
    }

    public void setResolveDependencies ( final boolean resolveDependencies )
    {
        this.resolveDependencies = resolveDependencies;
    }

    public boolean isResolveDependencies ()
    {
        return this.resolveDependencies;
    }
}
