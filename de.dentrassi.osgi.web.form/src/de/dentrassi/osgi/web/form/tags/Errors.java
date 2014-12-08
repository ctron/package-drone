package de.dentrassi.osgi.web.form.tags;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import de.dentrassi.osgi.web.controller.binding.BindingError;

public class Errors extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private Iterator<BindingError> iter;

    private String var = "i";

    @Override
    public int doStartTag () throws JspException
    {
        final List<BindingError> errors = getErrors ();
        this.iter = errors.iterator ();

        if ( !this.iter.hasNext () )
        {
            return SKIP_BODY;
        }

        this.pageContext.setAttribute ( this.var, this.iter.next () );

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doAfterBody () throws JspException
    {
        if ( this.iter.hasNext () )
        {
            this.pageContext.setAttribute ( this.var, this.iter.next () );
            return EVAL_BODY_AGAIN;
        }
        else
        {
            return SKIP_BODY;
        }
    }

    @Override
    public int doEndTag () throws JspException
    {
        return EVAL_PAGE;
    }

    public void setVar ( final String var )
    {
        this.var = var;
    }
}
