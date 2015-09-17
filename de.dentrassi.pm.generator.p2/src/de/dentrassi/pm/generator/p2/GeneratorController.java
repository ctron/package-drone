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
package de.dentrassi.pm.generator.p2;

import static de.dentrassi.pm.storage.web.utils.Channels.redirectViewArtifact;
import static de.dentrassi.pm.storage.web.utils.Channels.redirectViewChannel;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Part;
import javax.validation.Valid;

import org.osgi.framework.FrameworkUtil;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.generator.GeneratorProcessor;
import de.dentrassi.pm.generator.p2.xml.CategoryXmlGenerator;
import de.dentrassi.pm.storage.channel.ChannelService;
import de.dentrassi.pm.storage.channel.ChannelService.By;
import de.dentrassi.pm.storage.channel.ModifiableChannel;
import de.dentrassi.pm.storage.channel.ReadableChannel;
import de.dentrassi.pm.storage.web.utils.Channels;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class GeneratorController
{
    private ChannelService service;

    private final GeneratorProcessor generators = new GeneratorProcessor ( FrameworkUtil.getBundle ( GeneratorController.class ).getBundleContext () );

    public GeneratorController ()
    {
    }

    public void start ()
    {
        this.generators.open ();
    }

    public void stop ()
    {
        this.generators.close ();
    }

    public void setService ( final ChannelService service )
    {
        this.service = service;
    }

    @RequestMapping ( value = "/generators/p2.feature/channel/{channelId}/artifact/{artifactId}/editFeature",
            method = RequestMethod.GET )
    public ModelAndView editFeature ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId) throws Exception
    {
        return Channels.withArtifact ( this.service, channelId, artifactId, ReadableChannel.class, ( channel, artifact ) -> {

            final Map<String, Object> model = new HashMap<> ( 3 );

            model.put ( "artifactId", artifactId );

            final FeatureData data = new FeatureData ();
            MetaKeys.bind ( data, artifact.getMetaData () );

            model.put ( "command", data );
            model.put ( "channelId", artifact.getChannelId ().getId () );

            return new ModelAndView ( "edit", model );
        } );
    }

    @RequestMapping ( value = "/generators/p2.feature/channel/{channelId}/artifact/{artifactId}/editFeature",
            method = RequestMethod.POST )
    public ModelAndView editFeaturePost ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, @Valid @FormData ( "command" ) final FeatureData data, final BindingResult result) throws Exception
    {
        if ( result.hasErrors () )
        {
            final ModelAndView mav = new ModelAndView ( "edit" );
            mav.put ( "artifactId", artifactId );
            return mav;
        }

        final Map<MetaKey, String> providedMetaData = MetaKeys.unbind ( data );

        return Channels.withArtifact ( this.service, channelId, artifactId, ModifiableChannel.class, ( channel, artifact ) -> {
            channel.getContext ().applyMetaData ( artifactId, providedMetaData );

            return redirectViewArtifact ( channelId, artifactId );
        } );
    }

    @RequestMapping ( value = "/generators/p2.feature/channel/{channelId}/createFeature", method = RequestMethod.GET )
    public ModelAndView createFeature ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView mav = new ModelAndView ( "create" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );
        mav.put ( "command", new FeatureData () );

        return mav;
    }

    @RequestMapping ( value = "/generators/p2.feature/channel/{channelId}/createFeature", method = RequestMethod.POST )
    public ModelAndView createFeaturePost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final FeatureData data, final BindingResult result)
    {
        if ( result.hasErrors () )
        {
            final ModelAndView mav = new ModelAndView ( "create" );
            mav.put ( "generators", this.generators.getInformations ().values () );
            mav.put ( "channelId", channelId );
            return mav;
        }

        final Map<MetaKey, String> providedMetaData = new HashMap<> ();
        providedMetaData.put ( new MetaKey ( FeatureGenerator.ID, "id" ), data.getId () );
        providedMetaData.put ( new MetaKey ( FeatureGenerator.ID, "version" ), data.getVersion () );
        providedMetaData.put ( new MetaKey ( FeatureGenerator.ID, "description" ), data.getDescription () );
        providedMetaData.put ( new MetaKey ( FeatureGenerator.ID, "provider" ), data.getProvider () );
        providedMetaData.put ( new MetaKey ( FeatureGenerator.ID, "label" ), data.getLabel () );

        final String name = String.format ( "%s-%s.feature", data.getId (), data.getVersion () );

        this.service.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
            channel.getContext ().createGeneratorArtifact ( FeatureGenerator.ID, new ByteArrayInputStream ( new byte[0] ), name, providedMetaData );
        } );

        return Channels.redirectViewChannel ( channelId );
    }

    // category

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/artifact/{artifactId}/editCategory",
            method = RequestMethod.GET )
    public ModelAndView editCategory ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId) throws Exception
    {
        return Channels.withArtifact ( this.service, channelId, artifactId, ReadableChannel.class, ( channel, artifact ) -> {

            final Map<String, Object> model = new HashMap<> ( 3 );

            model.put ( "artifactId", artifactId );

            final CategoryData data = new CategoryData ();
            MetaKeys.bind ( data, artifact.getMetaData () );

            model.put ( "command", data );
            model.put ( "channelId", artifact.getChannelId ().getId () );

            return new ModelAndView ( "editCategory", model );
        } );
    }

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/artifact/{artifactId}/editCategory",
            method = RequestMethod.POST )
    public ModelAndView editCategoryPost ( @PathVariable ( "channelId" ) final String channelId, @PathVariable ( "artifactId" ) final String artifactId, @Valid @FormData ( "command" ) final CategoryData data, final BindingResult result) throws Exception
    {
        if ( result.hasErrors () )
        {
            final ModelAndView mav = new ModelAndView ( "editCategory" );
            mav.put ( "artifactId", artifactId );
            return mav;
        }

        final Map<MetaKey, String> providedMetaData = MetaKeys.unbind ( data );

        return Channels.withArtifact ( this.service, channelId, artifactId, ModifiableChannel.class, ( channel, artifact ) -> {
            channel.getContext ().applyMetaData ( artifactId, providedMetaData );

            return redirectViewArtifact ( channelId, artifactId );
        } );
    }

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/createCategory", method = RequestMethod.GET )
    public ModelAndView createCategory ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView mav = new ModelAndView ( "createCategory" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );
        mav.put ( "command", new CategoryData () );

        return mav;
    }

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/createCategory",
            method = RequestMethod.POST )
    public ModelAndView createCategoryPost ( @PathVariable ( "channelId" ) final String channelId, @Valid @FormData ( "command" ) final CategoryData data, final BindingResult result) throws Exception
    {
        if ( result.hasErrors () )
        {
            final ModelAndView mav = new ModelAndView ( "createCategory" );
            mav.put ( "generators", this.generators.getInformations ().values () );
            mav.put ( "channelId", channelId );
            return mav;
        }

        final Map<MetaKey, String> providedMetaData = MetaKeys.unbind ( data );

        final String name = String.format ( "%s.category", data.getId () );

        this.service.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
            channel.getContext ().createGeneratorArtifact ( CategoryGenerator.ID, new ByteArrayInputStream ( new byte[0] ), name, providedMetaData );
        } );

        return Channels.redirectViewChannel ( channelId );
    }

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/createCategoryXml",
            method = RequestMethod.GET )
    public ModelAndView createCategoryXml ( @PathVariable ( "channelId" ) final String channelId)
    {
        final ModelAndView mav = new ModelAndView ( "createCategoryXml" );

        mav.put ( "generators", this.generators.getInformations ().values () );
        mav.put ( "channelId", channelId );

        return mav;
    }

    @RequestMapping ( value = "/generators/p2.category/channel/{channelId}/createCategoryXml",
            method = RequestMethod.POST )
    public ModelAndView createCategoryXmlPost ( @PathVariable ( "channelId" ) final String channelId, final @RequestParameter ( "file" ) Part file, final BindingResult result) throws Exception
    {
        if ( result.hasErrors () )
        {
            final ModelAndView mav = new ModelAndView ( "createCategoryXml" );
            mav.put ( "generators", this.generators.getInformations ().values () );
            mav.put ( "channelId", channelId );
            return mav;
        }

        final String name = file.getSubmittedFileName ();

        this.service.accessRun ( By.id ( channelId ), ModifiableChannel.class, channel -> {
            channel.getContext ().createGeneratorArtifact ( CategoryXmlGenerator.ID, new ByteArrayInputStream ( new byte[0] ), name, null );
        } );

        return redirectViewChannel ( channelId );
    }
}
