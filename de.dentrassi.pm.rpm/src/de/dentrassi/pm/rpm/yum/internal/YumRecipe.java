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
package de.dentrassi.pm.rpm.yum.internal;

import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.pm.aspect.recipe.Recipe;
import de.dentrassi.pm.storage.Channel;

public class YumRecipe implements Recipe
{
    @Override
    public LinkTarget setup ( final Channel channel )
    {
        channel.addAspects ( true, "rpm", "yum" );
        return null;
    }
}
