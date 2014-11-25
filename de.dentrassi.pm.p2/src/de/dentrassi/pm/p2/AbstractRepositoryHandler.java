package de.dentrassi.pm.p2;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

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

        try
        {
            this.xml = new XmlHelper ();
        }
        catch ( final ParserConfigurationException e )
        {
            throw new RuntimeException ( e );
        }

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

    protected void fixSize ( final Element element )
    {
        element.setAttribute ( "size", "" + element.getChildNodes ().getLength () );
    }

    protected Document initRepository ( String processingType  , final String type   )
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
