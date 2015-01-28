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
package de.dentrassi.osgi.web.tags;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class DefineTag extends SimpleTagSupport
{
    private String name;

    public static String ATTR_PREFIX = DefineTag.class.getName () + ".macro.";

    public void setName ( final String name )
    {
        this.name = name;
    }

    @Override
    public void setJspBody ( final JspFragment jspBody )
    {
        super.setJspBody ( jspBody );
        getJspContext ().setAttribute ( ATTR_PREFIX + this.name, jspBody, PageContext.PAGE_SCOPE );
    }
}
