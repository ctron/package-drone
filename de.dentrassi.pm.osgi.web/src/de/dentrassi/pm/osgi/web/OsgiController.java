/*******************************************************************************
 * Copyright (c) 2015 Jens Reimann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jens Reimann - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.osgi.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.pm.aspect.common.osgi.OsgiAspectFactory;
import de.dentrassi.pm.osgi.bundle.BundleInformation;
import de.dentrassi.pm.osgi.feature.FeatureInformation;
import de.dentrassi.pm.storage.Artifact;
import de.dentrassi.pm.storage.Channel;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.web.InterfaceExtender;
import de.dentrassi.pm.storage.web.Modifier;
import de.dentrassi.pm.storage.web.menu.MenuEntry;

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
    public List<MenuEntry> getViews ( final Object object )
    {
        if ( object instanceof Channel )
        {
            return getChannelViews ( (Channel)object );
        }
        return null;
    }

    private List<MenuEntry> getChannelViews ( final Channel channel )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        final Map<String, Object> model = new HashMap<> ( 1 );

        model.put ( "channelId", channel.getId () );

        result.add ( new MenuEntry ( "OSGi", 500, "Bundles", 500, LinkTarget.createFromController ( OsgiController.class, "infoBundles" ).expand ( model ), Modifier.DEFAULT, null, false ) );
        result.add ( new MenuEntry ( "OSGi", 500, "Features", 600, LinkTarget.createFromController ( OsgiController.class, "infoFeatures" ).expand ( model ), Modifier.DEFAULT, null, false ) );

        return result;
    }

    @RequestMapping ( "/channel/{channelId}/infoBundles" )
    public ModelAndView infoBundles ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return new ModelAndView ( "redirect:/channelNotFound" );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "channel", channel );

        final List<BundleInformation> bundles = new LinkedList<> ();

        for ( final Artifact art : channel.getArtifacts () )
        {
            final BundleInformation bi = OsgiAspectFactory.fetchBundleInformation ( art.getInformation ().getMetaData () );
            if ( bi != null )
            {
                bundles.add ( bi );
            }
        }

        bundles.sort ( ( i1, i2 ) -> i1.getId ().compareTo ( i2.getId () ) );

        model.put ( "bundles", bundles );

        return new ModelAndView ( "infoBundles", model );
    }

    @RequestMapping ( "/channel/{channelId}/infoFeatures" )
    public ModelAndView infoFeatures ( @PathVariable ( "channelId" ) final String channelId )
    {
        final Channel channel = this.service.getChannel ( channelId );
        if ( channel == null )
        {
            return new ModelAndView ( "redirect:/channelNotFound" );
        }

        final Map<String, Object> model = new HashMap<> ();
        model.put ( "channel", channel );

        final List<FeatureInformation> features = new LinkedList<> ();

        for ( final Artifact art : channel.getArtifacts () )
        {
            final FeatureInformation fi = OsgiAspectFactory.fetchFeatureInformation ( art.getInformation ().getMetaData () );
            if ( fi != null )
            {
                features.add ( fi );
            }
        }

        features.sort ( ( i1, i2 ) -> i1.getId ().compareTo ( i2.getId () ) );

        model.put ( "features", features );

        return new ModelAndView ( "infoFeatures", model );
    }

}
