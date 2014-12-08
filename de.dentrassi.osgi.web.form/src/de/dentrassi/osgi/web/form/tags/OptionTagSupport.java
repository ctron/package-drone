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
