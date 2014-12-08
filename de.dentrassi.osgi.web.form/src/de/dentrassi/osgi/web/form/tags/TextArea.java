package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.JspException;

public class TextArea extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private Integer cols;

    private Integer rows;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<textarea" );
        writer.writeAttribute ( "id", this.path );
        writer.writeAttribute ( "name", this.path );
        writer.writeOptionalAttribute ( "cols", this.cols );
        writer.writeOptionalAttribute ( "rows", this.rows );
        writeDefaultAttributes ( writer );
        writer.write ( " >" );

        writer.writeEscaped ( getPathValue ( this.path ) );

        writer.write ( "</textarea>" );

        return SKIP_BODY;
    }

    public void setCols ( final int cols )
    {
        this.cols = cols;
    }

    public void setRows ( final int rows )
    {
        this.rows = rows;
    }
}
