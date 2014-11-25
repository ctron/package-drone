package de.dentrassi.pm.maven;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlHelper
{
    private static final class NodeListIterator implements Iterator<Node>
    {
        private final NodeList list;

        private int index;

        public NodeListIterator ( final NodeList list )
        {
            this.list = list;
        }

        @Override
        public Node next ()
        {
            return this.list.item ( this.index++ );
        }

        @Override
        public boolean hasNext ()
        {
            return this.index < this.list.getLength ();
        }
    }

    private final DocumentBuilder db;

    private final TransformerFactory transformerFactory;

    private final XPathFactory xpathFactory;

    public XmlHelper () throws ParserConfigurationException
    {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
        this.db = dbf.newDocumentBuilder ();

        this.transformerFactory = TransformerFactory.newInstance ();

        this.xpathFactory = XPathFactory.newInstance ();
    }

    public Document parse ( final InputStream stream ) throws Exception
    {
        return this.db.parse ( stream );
    }

    public void write ( final Document doc, final OutputStream stream ) throws Exception
    {
        final Transformer transformer = this.transformerFactory.newTransformer ();
        final DOMSource source = new DOMSource ( doc );
        final StreamResult result = new StreamResult ( stream );
        transformer.setOutputProperty ( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty ( OutputKeys.ENCODING, "UTF-8" );
        transformer.setOutputProperty ( "{http://xml.apache.org/xslt}indent-amount", "2" );
        transformer.transform ( source, result );
    }

    public String getElementValue ( final Node element, final String path ) throws Exception
    {
        for ( final Node n : iter ( path ( element, path ) ) )
        {
            return text ( n );
        }
        return null;
    }

    private String text ( final Node node )
    {
        return node.getTextContent ();
    }

    public static Iterable<Node> iter ( final NodeList list )
    {

        return new Iterable<Node> () {

            @Override
            public Iterator<Node> iterator ()
            {
                return new NodeListIterator ( list );
            }
        };
    }

    public NodeList path ( final Node node, final String path ) throws XPathExpressionException
    {
        final XPath xpath = this.xpathFactory.newXPath ();
        final XPathExpression expression = xpath.compile ( path );
        return (NodeList)expression.evaluate ( node, XPathConstants.NODESET );
    }
}
