package de.dentrassi.osgi.web.form.tags;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.servlet.jsp.tagext.Tag;

import de.dentrassi.osgi.web.controller.binding.BindingError;
import de.dentrassi.osgi.web.controller.binding.BindingResult;

public class FormValueTagSupport extends FormTagSupport
{
    private static final long serialVersionUID = 1L;

    protected String path;

    public void setPath ( final String path )
    {
        this.path = path;
    }

    protected List<BindingError> getErrors ()
    {
        final BindingResult br = getBindingResult ();
        if ( br == null )
        {
            return Collections.emptyList ();
        }
        return br.getErrors ();
    }

    protected BindingResult getBindingResult ()
    {
        final BindingResult br = getCommandBindingResult ();
        if ( br == null )
        {
            return null;
        }

        return br.getChild ( this.path );
    }

    protected Object getPathValue ( final String path )
    {
        final Object command = getCommandValue ();
        if ( command == null )
        {
            return null;
        }

        try
        {
            return Beans.getFromBean ( command, path );
        }
        catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException e )
        {
            return null;
        }
    }

    protected Object getCommandValue ()
    {
        final Tag formTag = findAncestorWithClass ( this, Form.class );
        if ( formTag instanceof Form )
        {
            return ( (Form)formTag ).getCommandValue ();
        }
        else
        {
            return null;
        }
    }

    protected BindingResult getCommandBindingResult ()
    {
        final Tag formTag = findAncestorWithClass ( this, Form.class );
        if ( formTag instanceof Form )
        {
            return ( (Form)formTag ).getBindingResult ();
        }
        else
        {
            return null;
        }
    }
}
