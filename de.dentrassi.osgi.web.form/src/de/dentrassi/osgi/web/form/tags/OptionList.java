package de.dentrassi.osgi.web.form.tags;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.jsp.JspException;

public class OptionList extends OptionTagSupport
{
    private static final long serialVersionUID = 1L;

    private Collection<?> items = Collections.emptyList ();

    private String itemValue;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        for ( final Object o : this.items )
        {
            renderOption ( writer, o );
        }

        return SKIP_BODY;
    }

    protected void renderOption ( final WriterHelper writer, final Object o ) throws JspException
    {
        try
        {
            final Object result = Beans.getFromBean ( o, this.itemValue );
            renderOption ( writer, result, o, false );
        }
        catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e )
        {
            throw new JspException ( e );
        }
    }

    protected void renderOption ( final WriterHelper writer, final Object value, final Object label, final boolean selected ) throws JspException
    {
        writer.write ( "<option" );
        writer.writeAttribute ( "value", value );
        writer.writeFlagAttribute ( "selected", isSelected ( value ) );
        writer.write ( " >" );

        writer.writeEscaped ( label != null ? "" + label : "" + value );

        writer.write ( "</option>" );
    }

    public void setItems ( final Collection<?> items )
    {
        this.items = items;
    }

    public void setItemValue ( final String itemValue )
    {
        this.itemValue = itemValue;
    }
}
