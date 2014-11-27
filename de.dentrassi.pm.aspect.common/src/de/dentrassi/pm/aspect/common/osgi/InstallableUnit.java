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
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.osgi.BundleInformation;

public class InstallableUnit
{

    private final static Logger logger = LoggerFactory.getLogger ( InstallableUnit.class );

    private String id;

    private String version;

    private String name;

    private boolean singleton;

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

        return result;
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
        addProperty ( properties, "org.eclipse.equinox.p2.name", this.name );

        final Element provides = addElement ( unit, "provides" );
        addProvided ( provides, "osgi.bundle", this.id, this.version );
        addProvided ( provides, "org.eclipse.equinox.p2.iu", this.id, this.version );
        addProvided ( provides, "org.eclipse.equinox.p2.eclipse.type", "bundle", "1.0.0" );

        final Element artifacts = addElement ( unit, "artifacts" );
        final Element a = addElement ( artifacts, "artifact" );
        a.setAttribute ( "classifier", "osgi.bundle" );
        a.setAttribute ( "id", this.id );
        a.setAttribute ( "version", this.version );

        try
        {
            final Map<String, String> td = new HashMap<String, String> ();
            td.put ( "manifest", makeManifest () );
            addTouchpoint ( unit, "org.eclipse.equinox.p2.osgi", "1.0.0", td );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to generate manifest", e );
        }

        fixSize ( properties );
        fixSize ( artifacts );
        fixSize ( provides );
        fixSize ( units );

        return doc;
    }

    private static void addProperty ( final Element properties, final String key, final String value )
    {
        final Element p = addElement ( properties, "property" );
        p.setAttribute ( "name", key );
        p.setAttribute ( "value", value );
    }

    private String makeManifest () throws IOException
    {
        final Manifest mf = new Manifest ();
        mf.getMainAttributes ().put ( Attributes.Name.MANIFEST_VERSION, "1.0" );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_SYMBOLICNAME, this.id );
        mf.getMainAttributes ().putValue ( Constants.BUNDLE_VERSION, this.version );

        final ByteArrayOutputStream out = new ByteArrayOutputStream ();
        mf.write ( out );
        out.close ();
        return out.toString ( "UTF-8" );
    }

    private void addTouchpoint ( final Element unit, final String id, final String version, final Map<String, String> td )
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
