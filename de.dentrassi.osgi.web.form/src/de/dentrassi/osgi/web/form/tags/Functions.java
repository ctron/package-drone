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
package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.PageContext;

import de.dentrassi.osgi.web.controller.binding.BindingResult;

public class Functions
{
    public static String validationState ( final PageContext pageContext, final String command, final String path, final String okCssClass, final String errorCssClass )
    {
        final BindingResult br = (BindingResult)pageContext.getRequest ().getAttribute ( BindingResult.ATTRIBUTE_NAME );
        if ( br == null )
        {
            return "";
        }

        BindingResult result = br.getChild ( command );
        if ( result == null )
        {
            return "";
        }

        result = result.getChild ( path );
        if ( result == null )
        {
            return "";
        }

        return result.hasErrors () ? errorCssClass : okCssClass;
    }
}
