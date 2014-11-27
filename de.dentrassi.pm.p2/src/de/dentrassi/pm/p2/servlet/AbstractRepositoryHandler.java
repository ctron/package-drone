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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.storage.service.Channel;

public abstract class AbstractRepositoryHandler implements Handler
{
    protected final Channel channel;

    protected final Map<String, String> properties = new HashMap<> ();

    protected XmlHelper xml;

    private byte[] data;

    public AbstractRepositoryHandler ( final Channel channel )
    {
        this.channel = channel;
        this.properties.put ( "p2.timestamp", "" + System.currentTimeMillis () );

        this.xml = new XmlHelper ();
    }

    protected void setData ( final Document doc ) throws Exception
    {
        this.data = this.xml.toData ( doc );
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        resp.setContentType ( "application/xml" );
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
        root.setAttribute ( "name", String.format ( "Package Drone - Channel: %s", this.channel.getId () ) );
        root.setAttribute ( "type", type );
        root.setAttribute ( "version", "1" );

        return doc;
    }

}
