/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.osgi.web.tags;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class BytesTag extends SimpleTagSupport
{
    private final Format bytesPattern = new MessageFormat ( "{0,choice,0#0 bytes|1#1 byte|1<{0,number,integer} bytes}" );

    private final NumberFormat numberPattern1 = NumberFormat.getNumberInstance ();

    private final NumberFormat numberPattern2 = NumberFormat.getNumberInstance ();

    public BytesTag ()
    {
        this.numberPattern1.setRoundingMode ( RoundingMode.HALF_UP );
        this.numberPattern1.setMaximumFractionDigits ( 1 );
        this.numberPattern1.setGroupingUsed ( false );

        this.numberPattern2.setRoundingMode ( RoundingMode.HALF_UP );
        this.numberPattern2.setMaximumFractionDigits ( 2 );
        this.numberPattern2.setGroupingUsed ( false );
    }

    private Long amount;

    public void setAmount ( final Long amount )
    {
        this.amount = amount;
    }

    @Override
    public void doTag () throws JspException, IOException
    {
        if ( this.amount != null )
        {
            getJspContext ().getOut ().write ( bytes ( this.amount ) );
        }
    }

    public String bytes ( final long amount )
    {
        if ( amount < 1024L )
        {
            return this.bytesPattern.format ( new Object[] { amount } );
        }
        if ( amount < 1024L * 1024L )
        {
            return this.numberPattern1.format ( amount / 1024.0 ) + " KiB";
        }
        if ( amount < 1024L * 1024L * 1024L )
        {
            return this.numberPattern2.format ( amount / ( 1024.0 * 1024.0 ) ) + " MiB";
        }
        return this.numberPattern2.format ( amount / ( 1024.0 * 1024.0 * 1024.0 ) ) + " GiB";
    }
}
