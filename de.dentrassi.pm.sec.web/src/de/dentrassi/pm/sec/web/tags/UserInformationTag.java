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
package de.dentrassi.pm.sec.web.tags;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import de.dentrassi.pm.sec.UserInformationPrincipal;

public class UserInformationTag extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String var = "userDetails";

    public void setVar ( final String var )
    {
        this.var = var != null ? var : "userDetails";
    }

    @Override
    public int doStartTag () throws JspException
    {
        final Principal p = getPrincipal ();

        if ( p instanceof UserInformationPrincipal )
        {
            this.pageContext.setAttribute ( this.var, ( (UserInformationPrincipal)p ).getUserInformation () );
        }

        return SKIP_BODY;
    }

    protected Principal getPrincipal ()
    {
        final ServletRequest request = this.pageContext.getRequest ();
        if ( request instanceof HttpServletRequest )
        {
            return ( (HttpServletRequest)request ).getUserPrincipal ();

        }
        return null;
    }
}
