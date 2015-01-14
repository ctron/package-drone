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
package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

public class OptionTagSupport extends TagSupport
{

    private static final long serialVersionUID = 1L;

    protected Object getSelectedValue ()
    {
        final Tag parent = findAncestorWithClass ( this, Select.class );
        if ( parent instanceof Select )
        {
            return ( (Select)parent ).getSelectedValue ();
        }
        else
        {
            return null;
        }
    }

    protected boolean isSelected ( final Object value )
    {
        final Object selectedValue = getSelectedValue ();
        if ( selectedValue == value )
        {
            return true;
        }
        if ( selectedValue == null )
        {
            return false;
        }
        return selectedValue.equals ( value );
    }

}
