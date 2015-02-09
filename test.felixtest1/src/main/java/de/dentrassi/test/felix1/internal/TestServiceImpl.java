/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.test.felix1.internal;

import de.dentrassi.test.felix1.TestService;

public class TestServiceImpl implements TestService
{

    public void sayHello ()
    {
        System.out.println ( "Wohooo!" );
    }

}
