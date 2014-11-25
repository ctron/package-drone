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
package de.dentrassi.pm.storage.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import de.dentrassi.pm.storage.web.menu.DefaultMenuExtender;

@Controller
public class WelcomeController extends AbstractDefaultController
{

    @Override
    protected void fillMenu ( final DefaultMenuExtender menuExtener )
    {
        // main "home" entry is coded in the JSP tag "main"
    }

    @RequestMapping ( value = "/", method = RequestMethod.GET )
    public ModelAndView main ()
    {
        if ( Activator.getTracker ().getStorageService () != null )
        {
            return new ModelAndView ( "index" );
        }
        else
        {
            return new ModelAndView ( "redirect:/setup" );
        }
    }
}
