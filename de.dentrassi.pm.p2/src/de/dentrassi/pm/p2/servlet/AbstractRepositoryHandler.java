/*******************************************************************************
 * Copyright (c) 2014 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.p2.servlet;

import static de.dentrassi.pm.common.XmlHelper.fixSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.Channel;

public abstract class AbstractRepositoryHandler extends AbstractChannelHandler
{
    protected final Map<String, String> properties = new HashMap<> ();

    protected XmlHelper xml;

    private byte[] data;

    private final boolean compress;

    private final String basename;

    public AbstractRepositoryHandler ( final Channel channel, final boolean compress, final String basename )
    {
        super ( channel );

        this.compress = compress;
        this.basename = basename;

        this.properties.put ( "p2.timestamp", "" + System.currentTimeMillis () );

        if ( compress )
        {
            this.properties.put ( "p2.compressed", "true" );
        }

        this.xml = new XmlHelper ();
    }

    protected void setData ( final Document doc ) throws Exception
    {
        if ( this.compress )
        {
            this.data = compress ( this.basename + ".xml", this.xml.toData ( doc ) );
        }
        else
        {
            this.data = this.xml.toData ( doc );
        }
    }

    private byte[] compress ( final String name, final byte[] data ) throws IOException
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        final ZipOutputStream zos = new ZipOutputStream ( bos );

        final ZipEntry ze = new ZipEntry ( name );
        ze.setSize ( data.length );
        zos.putNextEntry ( ze );
        zos.write ( data );
        zos.close ();

        return bos.toByteArray ();
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        if ( this.compress )
        {
            resp.setContentType ( "application/zip" );
        }
        else
        {
            resp.setContentType ( "application/xml" );
        }
        resp.setContentLength ( this.data.length );
        resp.getOutputStream ().write ( this.data );
    }

    protected Element addElement ( final Element ele, final String name )
    {
        final Element ne = ele.getOwnerDocument ().createElement ( name );
        ele.appendChild ( ne );
        return ne;
    }

    protected void addProperties ( final Element root )
    {
        final Element props = addElement ( root, "properties" );

        for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
        {
            final Element p = addElement ( props, "property" );
            p.setAttribute ( "name", entry.getKey () );
            p.setAttribute ( "value", entry.getValue () );
        }

        fixSize ( props );
    }

    protected Document initRepository ( final String processingType, final String type )
    {
        final Document doc = this.xml.create ();

        {
            final ProcessingInstruction pi = doc.createProcessingInstruction ( processingType, "version=\"1.1.0\"" );
            doc.appendChild ( pi );
        }

        final Element root = doc.createElement ( "repository" );
        doc.appendChild ( root );
        root.setAttribute ( "name", makeExternalTitle () );
        root.setAttribute ( "type", type );
        root.setAttribute ( "version", "1" );

        return doc;
    }

}
