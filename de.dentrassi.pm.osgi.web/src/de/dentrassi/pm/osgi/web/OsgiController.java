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
package de.dentrassi.pm.osgi.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.common.DetailedArtifactInformation;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.feature.FeatureInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;

@Controller
@RequestMapping ( "/osgi.info" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class OsgiController implements InterfaceExtender
{
    private StorageService service;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        if ( object instanceof Channel )
        {
            return getChannelViews ( (Channel)object );
        }
        return null;
    }

    private List<MenuEntry> getChannelViews ( final Channel channel )
    {
        if ( !channel.hasAspect ( "osgi" ) )
        {
            return null;
        }

        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = new HashMap<> ( 1 );

        model.put ( "channelId", channel.getId () );

        result.add ( new MenuEntry ( "OSGi", 500, "Bundles", 500, LinkTarget.createFromController ( OsgiController.class, "infoBundles" ).expand ( model ), Modifier.DEFAULT, null, false, 0 ) );
        result.add ( new MenuEntry ( "OSGi", 500, "Features", 600, LinkTarget.createFromController ( OsgiController.class, "infoFeatures" ).expand ( model ), Modifier.DEFAULT, null, false, 0 ) );

        return result;
    }

    public static class ArtifactBundleInformation extends BundleInformation
    {
        private String artifactId;

        public void setArtifactId ( final String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getArtifactId ()
        {
            return this.artifactId;
        }
    }

    public static class ArtifactFeatureInformation extends FeatureInformation
    {
        private String artifactId;

        public void setArtifactId ( final String artifactId )
        {
            this.artifactId = artifactId;
        }

        public String getArtifactId ()
        {
            return this.artifactId;
        }
    }

    @RequestMapping ( "/channel/{channelId}/infoBundles" )
    public ModelAndView infoBundles ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "channel", channel );

        final List<ArtifactBundleInformation> bundles = new LinkedList<> ();

        for ( final DetailedArtifactInformation art : channel.getDetailedArtifacts () )
        {
            final ArtifactBundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getMetaData (), ArtifactBundleInformation.class );
            if ( bi != null )
            {
                bi.setArtifactId ( art.getId () );
                bundles.add ( bi );
            }
        }

        bundles.sort ( ( i1, i2 ) -> {
            final int rc = i1.getId ().compareTo ( i2.getId () );
            if ( rc != 0 )
            {
                return rc;
            }
            return i1.getVersion ().compareTo ( i2.getVersion () );
        } );

        model.put ( "bundles", bundles );

        return new ModelAndView ( "infoBundles", model );
    }

    @RequestMapping ( "/artifact/{artifactId}/viewBundle" )
    public ModelAndView viewBundle ( @PathVariable ( "artifactId" ) final String artifactId)
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );
        if ( artifact == null )
        {
            return CommonController.createNotFound ( "artifact", artifactId );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "artifact", artifact );

        final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( artifact.getInformation ().getMetaData () );
        model.put ( "bundle", bi );

        final List<Entry> breadcrumbs = new LinkedList<> ();
        breadcrumbs.add ( new Entry ( "Home", "/" ) );
        breadcrumbs.add ( new Entry ( "Channel", "/osgi.info/channel/" + artifact.getChannel ().getId () + "/infoBundles" ) );
        breadcrumbs.add ( new Entry ( "Artifact", "/artifact/" + artifact.getId () + "/view" ) );
        breadcrumbs.add ( new Entry ( "Bundle Information" ) );
        model.put ( "breadcrumbs", new Breadcrumbs ( breadcrumbs ) );

        model.put ( "fullManifest", artifact.getInformation ().getMetaData ().get ( new MetaKey ( "osgi", "fullManifest" ) ) );

        return new ModelAndView ( "viewBundle", model );
    }

    @RequestMapping ( "/artifact/{artifactId}/viewFeature" )
    public ModelAndView viewFeature ( @PathVariable ( "artifactId" ) final String artifactId)
    {
        final Artifact artifact = this.service.getArtifact ( artifactId );
        if ( artifact == null )
        {
            return CommonController.createNotFound ( "artifact", artifactId );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "artifact", artifact );

        final FeatureInformation bi = OsgiAspectFactory.fetchFeatureInformation ( artifact.getInformation ().getMetaData () );
        model.put ( "feature", bi );

        final List<Entry> breadcrumbs = new LinkedList<> ();
        breadcrumbs.add ( new Entry ( "Home", "/" ) );
        breadcrumbs.add ( new Entry ( "Channel", "/osgi.info/channel/" + artifact.getChannel ().getId () + "/infoBundles" ) );
        breadcrumbs.add ( new Entry ( "Artifact", "/artifact/" + artifact.getId () + "/view" ) );
        breadcrumbs.add ( new Entry ( "Feature Information" ) );
        model.put ( "breadcrumbs", new Breadcrumbs ( breadcrumbs ) );

        return new ModelAndView ( "viewFeature", model );
    }

    @RequestMapping ( "/channel/{channelId}/infoFeatures" )
    public ModelAndView infoFeatures ( @PathVariable ( "channelId" ) final String channelId)
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return CommonController.createNotFound ( "channel", channelId );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "channel", channel );

        final List<FeatureInformation> features = new LinkedList<> ();

        for ( final DetailedArtifactInformation art : channel.getDetailedArtifacts () )
        {
            final ArtifactFeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getMetaData (), ArtifactFeatureInformation.class );
            if ( fi != null )
            {
                fi.setArtifactId ( art.getId () );
                features.add ( fi );
            }
        }

        features.sort ( ( i1, i2 ) -> {
            final int rc = i1.getId ().compareTo ( i2.getId () );
            if ( rc != 0 )
            {
                return rc;
            }
            return i1.getVersion ().compareTo ( i2.getVersion () );
        } );

        model.put ( "features", features );

        return new ModelAndView ( "infoFeatures", model );
    }

}
