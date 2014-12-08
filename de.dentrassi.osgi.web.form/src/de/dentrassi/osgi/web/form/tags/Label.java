package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.JspException;

public class Label extends FormTagSupport
{
    private static final long serialVersionUID = 1L;

    private String path;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<label" );
        writer.writeOptionalAttribute ( "for", this.path );
        writeDefaultAttributes ( writer );
        writer.write ( ">" );

        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "</label>" );

        return EVAL_PAGE;
    }

    public void setPath ( final String path )
    {
        this.path = path;
    }
}
