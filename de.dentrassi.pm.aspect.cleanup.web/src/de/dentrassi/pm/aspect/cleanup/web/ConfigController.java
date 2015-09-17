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
package de.dentrassi.pm.aspect.cleanup.web;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;
import com.google.gson.GsonBuilder;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.aspect.cleanup.Aggregator;
import de.dentrassi.pm.aspect.cleanup.CleanupConfiguration;
import de.dentrassi.pm.aspect.cleanup.CleanupTester;
import de.dentrassi.pm.aspect.cleanup.Field;
import de.dentrassi.pm.aspect.cleanup.Sorter;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.channel.ChannelInformation;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;
import de.dentrassi.pm.storage.web.utils.Channels;

@Controller
@RequestMapping ( "/aspect/cleanup/{channelId}/config" )
@ViewResolver ( "/WEB-INF/views/config/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private final static Logger logger = LoggerFactory.getLogger ( ConfigController.class );

    private CleanupTester tester;

    private ChannelService service;

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    public void setTester ( final CleanupTester tester )
    {
        this.tester = tester;
    }

    @Override
    public List<MenuEntry> getViews ( final HttpServletRequest request, final Object object )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( object instanceof ChannelInformation )
        {
            final ChannelInformation channel = (ChannelInformation)object;
            if ( channel.hasAspect ( "cleanup" ) && request.isUserInRole ( "MANAGER" ) )
            {
                final Map<String, String> model = new HashMap<> ();
                model.put ( "channelId", channel.getId () );
                result.add ( new MenuEntry ( "Cleanup", 7_000, LinkTarget.createFromController ( ConfigController.class, "edit" ).expand ( model ), null, null ) );
            }
        }

        return result;
    }

    @RequestMapping ( "/edit" )
    public ModelAndView edit ( @PathVariable ( "channelId" ) final String channelId, @RequestParameter (
            value = "configuration", required = false ) final String configString)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            final Map<String, Object> model = new HashMap<> ( 2 );

            model.put ( "channel", channel.getInformation () );

            CleanupConfiguration cfg;

            try
            {
                if ( configString != null && !configString.isEmpty () )
                {
                    // use the content from the input parameter
                    cfg = new GsonBuilder ().create ().fromJson ( configString, CleanupConfiguration.class );
                }
                else
                {
                    cfg = MetaKeys.bind ( makeDefaultConfiguration (), channel.getMetaData () );
                }
            }
            catch ( final Exception e )
            {
                logger.info ( "Failed to parse cleanup config", e );
                // something failed, go back to default
                cfg = makeDefaultConfiguration ();
            }

            model.put ( "command", cfg );

            fillModel ( model, channelId );

            return new ModelAndView ( "edit", model );
        } );
    }

    protected CleanupConfiguration makeDefaultConfiguration ()
    {
        CleanupConfiguration cfg;

        cfg = new CleanupConfiguration ();

        cfg.setNumberOfVersions ( 3 );
        cfg.setOnlyRootArtifacts ( true );

        final Aggregator aggregator = new Aggregator ();
        aggregator.getFields ().add ( new MetaKey ( "mvn", "groupId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "artifactId" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "version" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "classifier" ) );
        aggregator.getFields ().add ( new MetaKey ( "mvn", "extension" ) );
        cfg.setAggregator ( aggregator );

        final Sorter sorter = new Sorter ();
        sorter.getFields ().add ( new Field ( "mvn", "snapshotVersion" ) );
        cfg.setSorter ( sorter );
        return cfg;
    }

    @RequestMapping ( value = "/edit", method = RequestMethod.POST )
    public ModelAndView editPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final CleanupConfiguration cfg, final BindingResult result)
    {
        return Channels.withChannel ( this.service, channelId, ModifiableChannel.class, channel -> {
            final Map<String, Object> model = new HashMap<> ( 2 );

            model.put ( "command", cfg );
            model.put ( "channel", channel.getInformation () );
            fillModel ( model, channelId );

            try
            {
                if ( !result.hasErrors () )
                {
                    channel.applyMetaData ( MetaKeys.unbind ( cfg ) );
                }
            }
            catch ( final Exception e )
            {
                return CommonController.createError ( "Update configuration", "Failed to update cleanup configuration", e );
            }

            return new ModelAndView ( "edit", model );

        } );
    }

    @RequestMapping ( value = "/test", method = RequestMethod.POST )
    public ModelAndView testPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final CleanupConfiguration cfg, final BindingResult result)
    {
        return Channels.withChannel ( this.service, channelId, ReadableChannel.class, channel -> {

            if ( !result.hasErrors () )
            {
                final Map<String, Object> model = new HashMap<> ();

                model.put ( "command", cfg );
                model.put ( "channel", channel.getInformation () );
                fillModel ( model, channelId );

                model.put ( "result", this.tester.testCleanup ( channel.getArtifacts (), cfg ) );
                return new ModelAndView ( "testResult", model );
            }
            else
            {
                return CommonController.createError ( "Testing cleanup", "The configuration has errors", null );
            }
        } );
    }

    private void fillModel ( final Map<String, Object> model, final String channelId )
    {
        final List<Entry> entries = new LinkedList<> ();

        entries.add ( new Entry ( "Home", "/" ) );
        entries.add ( new Entry ( "Channel", "/channel/" + UrlEscapers.urlPathSegmentEscaper ().escape ( channelId ) + "/view" ) );
        entries.add ( new Entry ( "Cleanup" ) );

        model.put ( "breadcrumbs", new Breadcrumbs ( entries ) );
    }
}
