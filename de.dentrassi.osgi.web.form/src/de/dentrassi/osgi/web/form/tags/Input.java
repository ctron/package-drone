/*******************************************************************************
 * Copyright (c) 2014, 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.form.tags;

import javax.servlet.jsp.JspException;

public class Input extends FormValueTagSupport
{
    private static final long serialVersionUID = 1L;

    private String type;

    private boolean disabled;

    private boolean readonly;

    private String placeholder;

    @Override
    public int doStartTag () throws JspException
    {
        final WriterHelper writer = new WriterHelper ( this.pageContext );

        writer.write ( "<input" );
        writer.writeAttribute ( "id", this.path );
        writer.writeAttribute ( "name", this.path );
        writer.writeOptionalAttribute ( "value", getPathValue ( this.path ) );
        writer.writeOptionalAttribute ( "type", this.type );
        writer.writeOptionalAttribute ( "placeholder", this.placeholder );
        writer.writeFlagAttribute ( "disabled", this.disabled );
        writer.writeFlagAttribute ( "readonly", this.readonly );
        writeDefaultAttributes ( writer );
        writer.write ( " />" );

        return SKIP_BODY;
    }

    public void setType ( final String type )
    {
        this.type = type;
    }

    public void setDisabled ( final boolean disabled )
    {
        this.disabled = disabled;
    }

    public void setReadonly ( final boolean readonly )
    {
        this.readonly = readonly;
    }

    public void setPlaceholder ( final String placeholder )
    {
        this.placeholder = placeholder;
    }

}
