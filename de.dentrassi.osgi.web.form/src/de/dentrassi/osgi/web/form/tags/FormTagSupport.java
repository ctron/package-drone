package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class FormTagSupport extends TagSupport
{
    private static final long serialVersionUID = 1L;

    private String cssClass;

    protected void writeDefaultAttributes ( final WriterHelper writer ) throws JspException
    {
        writer.writeOptionalAttribute ( "class", this.cssClass );
    }

    public void setCssClass ( final String cssClass )
    {
        this.cssClass = cssClass;
    }

}
