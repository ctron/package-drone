package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.JspException;

public class Select extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    private Object selectedValue;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<select" );
        writer.writeOptionalAttribute ( "id", this.path );
        writer.writeOptionalAttribute ( "name", this.path );
        writeDefaultAttributes ( writer );
        writer.write ( " >" );

        this.selectedValue = getPathValue ( this.path );

        return EVAL_BODY_INCLUDE;
    }

    public Object getSelectedValue ()
    {
        return this.selectedValue;
    }

    @Override
    public int doEndTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "</select>" );

        return EVAL_PAGE;
    }

    @Override
    public void setPath ( final String path )
    {
        this.path = path;
    }
}
