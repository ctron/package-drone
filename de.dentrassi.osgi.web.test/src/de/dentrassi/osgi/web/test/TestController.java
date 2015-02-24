/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.test;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;

@Controller
@RequestMapping ( "/a" )
@ViewResolver ( "WEB-INF/views/%s.jsp" )
public class TestController
{
    public ModelAndView mainA ()
    {
        return new ModelAndView ( "index" );
    }

    @RequestMapping ( "/b" )
    public ModelAndView mainB ()
    {
        return new ModelAndView ( "index" );
    }
}
