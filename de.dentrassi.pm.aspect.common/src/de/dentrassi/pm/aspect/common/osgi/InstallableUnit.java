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
package de.dentrassi.pm.aspect.common.osgi;

import static de.dentrassi.pm.common.XmlHelper.addElement;
import static de.dentrassi.pm.common.XmlHelper.fixSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.osgi.BundleInformation;
import de.dentrassi.pm.osgi.BundleInformation.BundleRequirement;
import de.dentrassi.pm.osgi.BundleInformation.PackageExport;
import de.dentrassi.pm.osgi.BundleInformation.PackageImport;

public class InstallableUnit
{
    private final static Logger logger = LoggerFactory.getLogger ( InstallableUnit.class );

    private String id;

    private String version;

    private String name;

    private boolean singleton;

    private Map<String, String> properties = new HashMap<> ();

    private Element additionalNodes;

    public static class Key
    {
        private final String namespace;

        private final String key;

        public Key ( final String namespace, final String key )
        {
            this.namespace = namespace;
            this.key = key;
        }

        public String getKey ()
        {
            return this.key;
        }

        public String getNamespace ()
        {
            return this.namespace;
        }

        @Override
        public int hashCode ()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( this.key == null ? 0 : this.key.hashCode () );
            result = prime * result + ( this.namespace == null ? 0 : this.namespace.hashCode () );
            return result;
        }

        @Override
        public boolean equals ( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( ! ( obj instanceof Key ) )
            {
                return false;
            }
            final Key other = (Key)obj;
            if ( this.key == null )
            {
                if ( other.key != null )
                {
                    return false;
                }
            }
            else if ( !this.key.equals ( other.key ) )
            {
                return false;
            }
            if ( this.namespace == null )
            {
                if ( other.namespace != null )
                {
                    return false;
                }
            }
            else if ( !this.namespace.equals ( other.namespace ) )
            {
                return false;
            }
            return true;
        }
    }

    public static class Requirement
    {
        private final VersionRange range;

        private final boolean optional;

        private final Boolean greedy;

        public Requirement ( final VersionRange range, final boolean optional, final Boolean greedy )
        {
            this.range = range;
            this.optional = optional;
            this.greedy = greedy;
        }

        public VersionRange getRange ()
        {
            return this.range;
        }

        public boolean isOptional ()
        {
            return this.optional;
        }

        public Boolean getGreedy ()
        {
            return this.greedy;
        }

    }

    private Map<Key, Requirement> requires = new HashMap<> ();

    private Map<Key, String> provides = new HashMap<> ();

    public void setAdditionalNodes ( final Element additionalNodes )
    {
        this.additionalNodes = additionalNodes;
    }

    public Element getAdditionalNodes ()
    {
        return this.additionalNodes;
    }

    public void setRequires ( final Map<Key, Requirement> requires )
    {
        this.requires = requires;
    }

    public Map<Key, Requirement> getRequires ()
    {
        return this.requires;
    }

    public void setProvides ( final Map<Key, String> provides )
    {
        this.provides = provides;
    }

    public Map<Key, String> getProvides ()
    {
        return this.provides;
    }

    public void setProperties ( final Map<String, String> properties )
    {
        this.properties = properties;
    }

    public Map<String, String> getProperties ()
    {
        return this.properties;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public String getName ()
    {
        return this.name;
    }

    public void setSingleton ( final boolean singleton )
    {
        this.singleton = singleton;
    }

    public boolean isSingleton ()
    {
        return this.singleton;
    }

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public static InstallableUnit fromBundle ( final BundleInformation bundle )
    {
        final InstallableUnit result = new InstallableUnit ();

        result.setId ( bundle.getId () );
        result.setVersion ( bundle.getVersion () );
        result.setName ( bundle.getName () );
        result.setSingleton ( bundle.isSingleton () );

        result.getProvides ().put ( new Key ( "osgi.bundle", bundle.getId () ), bundle.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.iu", bundle.getId () ), bundle.getVersion () );
        result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.eclipse.type", "bundle" ), "1.0.0" );

        for ( final PackageExport pe : bundle.getPackageExports () )
        {
            result.getProvides ().put ( new Key ( "java.package", pe.getName () ), makeVersion ( pe.getVersion () ) );
        }

        for ( final String loc : bundle.getLocalization ().keySet () )
        {
            result.getProvides ().put ( new Key ( "org.eclipse.equinox.p2.localization", loc ), "1.0.0" );
        }

        final Map<String, String> props = result.getProperties ();
        props.put ( "org.eclipse.equinox.p2.name", bundle.getName () );
        props.put ( "org.eclipse.equinox.p2.provider", bundle.getVendor () );

        for ( final Map.Entry<String, Properties> le : bundle.getLocalization ().entrySet () )
        {
            final String locale = le.getKey ();
            for ( final Map.Entry<Object, Object> pe : le.getValue ().entrySet () )
            {
                props.put ( locale + "." + pe.getKey (), "" + pe.getValue () );
            }
        }

        for ( final PackageImport pi : bundle.getPackageImports () )
        {
            result.getRequires ().put ( new Key ( "java.package", pi.getName () ), new Requirement ( pi.getVersionRange (), pi.isOptional (), pi.isOptional () ? false : null ) );
        }

        for ( final BundleRequirement br : bundle.getBundleRequirements () )
        {
            result.getRequires ().put ( new Key ( "osgi.bundle", br.getId () ), new Requirement ( br.getVersionRange (), br.isOptional (), br.isOptional () ? false : null ) );
        }

        final XmlHelper xml = new XmlHelper ();
        final Document doc = xml.create ();
        final Element root = doc.createElement ( "root" );

        try
        {
            final Map<String, String> td = new HashMap<String, String> ();
            td.put ( "manifest", makeManifest ( bundle.getId (), bundle.getVersion () ) );
            addTouchpoint ( root, "org.eclipse.equinox.p2.osgi", "1.0.0", td );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate manifest", e );
        }

        result.setAdditionalNodes ( root );

        return result;
    }

    private static String makeVersion ( final Version version )
    {
        if ( version == null )
        {
            return "0.0.0";
        }
        return version.toString ();
    }

    public Document toXml ()
    {
        final XmlHelper xml = new XmlHelper ();

        final Document doc = xml.create ();
        final Element root = doc.createElement ( "units" );
        doc.appendChild ( root );

        final Element units = addElement ( root, "units" );

        final Element unit = addElement ( units, "unit" );
        unit.setAttribute ( "id", this.id );
        unit.setAttribute ( "version", this.version );
        unit.setAttribute ( "singleton", "" + this.singleton );

        final Element update = addElement ( unit, "update" );
        update.setAttribute ( "id", this.id );
        update.setAttribute ( "range", "[0.0.0," + this.version + ")" );
        update.setAttribute ( "severity", "0" );

        final Element properties = addElement ( unit, "properties" );
        for ( final Map.Entry<String, String> entry : this.properties.entrySet () )
        {
            addProperty ( properties, entry.getKey (), entry.getValue () );
        }

        final Element provides = addElement ( unit, "provides" );
        for ( final Map.Entry<Key, String> entry : this.provides.entrySet () )
        {
            addProvided ( provides, entry.getKey ().getNamespace (), entry.getKey ().getKey (), entry.getValue () );
        }

        final Element requires = addElement ( unit, "requires" );
        for ( final Map.Entry<Key, Requirement> entry : this.requires.entrySet () )
        {
            final Element p = addElement ( requires, "required" );
            p.setAttribute ( "namespace", entry.getKey ().getNamespace () );
            p.setAttribute ( "name", entry.getKey ().getKey () );
            p.setAttribute ( "range", makeString ( entry.getValue ().getRange () ) );
            if ( entry.getValue ().isOptional () )
            {
                p.setAttribute ( "optional", "true" );

            }
            if ( entry.getValue ().getGreedy () != null )
            {
                p.setAttribute ( "greedy", "" + entry.getValue ().getGreedy () );
            }
        }

        final Element artifacts = addElement ( unit, "artifacts" );
        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", "osgi.bundle" );
        a.setAttribute ( "id", this.id );
        a.setAttribute ( "version", this.version );

        if ( this.additionalNodes != null )
        {
            for ( final Node node : XmlHelper.iter ( this.additionalNodes.getChildNodes () ) )
            {
                if ( node instanceof Element )
                {
                    unit.appendChild ( unit.getOwnerDocument ().adoptNode ( node.cloneNode ( true ) ) );
                }
            }
        }

        fixSize ( requires );
        fixSize ( properties );
        fixSize ( artifacts );
        fixSize ( provides );
        fixSize ( units );

        return doc;
    }

    private String makeString ( final VersionRange range )
    {
        if ( range == null )
        {
            return "0.0.0";
        }
        else
        {
            return range.toString ();
        }
    }

    private static void addProperty ( final Element properties, final String key, final String value )
    {
        final Element p = addElement ( properties, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    private static String makeManifest ( final String id, final String version ) throws IOException
    {
        final Manifest mf = new Manifest ();
        mf.getMainAttributes ().put ( Attributes.Name.MANIFEST_VERSION, "1.0" );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_SYMBOLICNAME, id );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_VERSION, version );

        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        mf.write ( out );
        out.close ();
        return out.toString ( "UTF-8" );
    }

    private static void addTouchpoint ( final Element unit, final String id, final String version, final Map<String, String> td )
    {
        final Element touchpoint = addElement ( unit, "touchpoint" );
        touchpoint.setAttribute ( "id", id );
        touchpoint.setAttribute ( "version", version );

        final Element touchpointData = addElement ( unit, "touchpointData" );
        final Element is = addElement ( touchpointData, "instructions" );
        for ( final Map.Entry<String, String> entry : td.entrySet () )
        {
            final Element i = addElement ( is, "instruction" );
            i.setAttribute ( "key", entry.getKey () );
            final Text v = i.getOwnerDocument ().createTextNode ( entry.getValue () );
            i.appendChild ( v );
        }

        fixSize ( is );
        fixSize ( touchpointData );
    }

    private static void addProvided ( final Element provides, final String namespace, final String name, final String version )
    {
        final Element p = addElement ( provides, "provided" );
        p.setAttribute ( "namespace", namespace );
        p.setAttribute ( "name", name );
        p.setAttribute ( "version", version );
    }
}
