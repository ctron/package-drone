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
package de.dentrassi.pm.r5.handler;

import static de.dentrassi.osgi.utils.Filters.and;
import static de.dentrassi.osgi.utils.Filters.pair;
import static de.dentrassi.osgi.utils.Filters.versionRange;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.osgi.utils.Filters;
import de.dentrassi.osgi.utils.Filters.Node;
import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.common.servlet.Handler;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.bundle.BundleInformation.BundleRequirement;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageExport;
import de.dentrassi.pm.osgi.bundle.BundleInformation.PackageImport;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;

public class IndexHandler implements Handler
{

    private final XmlHelper xml;

    private byte[] data;

    private final Channel channel;

    public IndexHandler ( final Channel channel )
    {
        this.xml = new XmlHelper ();
        this.channel = channel;
    }

    @Override
    public void prepare () throws Exception
    {
        final Document doc = this.xml.create ();

        final Element rep = doc.createElementNS ( "http://www.osgi.org/xmlns/repository/v1.0.0", "repository" );
        doc.appendChild ( rep );

        rep.setAttribute ( "increment", "" + System.currentTimeMillis () );

        final String name = this.channel.getName () != null ? this.channel.getName () : this.channel.getId ();
        rep.setAttribute ( "name", name );

        this.channel.getArtifacts ().stream ().forEach ( a -> process ( rep, a ) );

        this.data = this.xml.toData ( doc );
    }

    private void process ( final Element rep, final Artifact a )
    {
        final SortedMap<MetaKey, String> md = a.getInformation ().getMetaData ();

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( md );

        if ( bi == null )
        {
            return;
        }

        if ( bi.getId () == null || bi.getVersion () == null )
        {
            return;
        }

        final Element r = XmlHelper.addElement ( rep, "resource" );

        addIdentity ( r, bi );
        addContent ( r, a, this.channel.getId () );

        {
            final List<Node> nodes = new LinkedList<> ();

            for ( final String ee : bi.getRequiredExecutionEnvironments () )
            {
                nodes.add ( pair ( "osgi.ee", ee ) );
            }
            if ( !nodes.isEmpty () )
            {
                final Map<String, String> reqs = new HashMap<> ( 1 );
                reqs.put ( "filter", Filters.or ( nodes ) );
                addRequirement ( r, "osgi.ee", reqs );
            }
        }

        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.bundle", bi.getId () );
            caps.put ( "bundle-version", bi.getVersion () );

            addCapability ( r, "osgi.wiring.bundle", caps );
        }

        for ( final BundleRequirement br : bi.getBundleRequirements () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.bundle", br.getId () ), //
            versionRange ( "bundle-version", br.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            addRequirement ( r, "osgi.wiring.bundle", reqs );
        }

        for ( final PackageExport pe : bi.getPackageExports () )
        {
            final Map<String, Object> caps = new HashMap<> ();

            caps.put ( "osgi.wiring.package", pe.getName () );

            if ( pe.getVersion () != null )
            {
                caps.put ( "version", pe.getVersion () );
            }

            addCapability ( r, "osgi.wiring.package", caps );
        }

        for ( final PackageImport pi : bi.getPackageImports () )
        {
            final Map<String, String> reqs = new HashMap<> ();

            final String filter = and ( //
            pair ( "osgi.wiring.package", pi.getName () ), //
            versionRange ( "version", pi.getVersionRange () ) //
            );

            reqs.put ( "filter", filter );

            addRequirement ( r, "osgi.wiring.package", reqs );
        }
    }

    private void addContent ( final Element r, final Artifact a, final String channelId )
    {
        final String sha256 = a.getInformation ().getMetaData ().get ( new MetaKey ( "hasher", "sha256" ) );

        if ( sha256 == null )
        {
            return;
        }

        final Map<String, Object> caps = new HashMap<> ();

        caps.put ( "osgi.content", sha256 );
        caps.put ( "size", a.getInformation ().getSize () );
        caps.put ( "mime", "application/vnd.osgi.bundle" );
        caps.put ( "url", channelId + "/artifact/" + a.getId () + "/" + a.getInformation ().getName () );

        addCapability ( r, "osgi.content", caps );
    }

    private void addIdentity ( final Element r, final BundleInformation bi )
    {
        final Map<String, Object> caps = new HashMap<> ();

        caps.put ( "osgi.identity", bi.getId () );
        caps.put ( "version", bi.getVersion () );
        caps.put ( "type", "osgi.bundle" );

        addCapability ( r, "osgi.identity", caps );
    }

    private void addCapability ( final Element resource, final String id, final Map<String, Object> caps )
    {
        final Element c = XmlHelper.addElement ( resource, "capability" );

        c.setAttribute ( "namespace", id );

        for ( final Map.Entry<String, Object> entry : caps.entrySet () )
        {
            final Element a = XmlHelper.addElement ( c, "attribute" );
            a.setAttribute ( "name", entry.getKey () );

            final Object v = entry.getValue ();

            if ( ! ( v instanceof String ) )
            {
                a.setAttribute ( "type", v.getClass ().getSimpleName () );
            }

            a.setAttribute ( "value", "" + v );
        }
    }

    private void addRequirement ( final Element resource, final String id, final Map<String, String> caps )
    {
        final Element c = XmlHelper.addElement ( resource, "requirement" );

        c.setAttribute ( "namespace", id );

        for ( final Map.Entry<String, String> entry : caps.entrySet () )
        {
            final Element a = XmlHelper.addElement ( c, "directive" );
            a.setAttribute ( "name", entry.getKey () );
            a.setAttribute ( "value", entry.getValue () );
        }
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        resp.setContentType ( "application/xml" );
        resp.getOutputStream ().write ( this.data );
    }

}
