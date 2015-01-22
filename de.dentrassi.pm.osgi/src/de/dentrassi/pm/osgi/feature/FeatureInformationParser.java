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
package de.dentrassi.pm.osgi.feature;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.osgi.ParserHelper;
import de.dentrassi.pm.osgi.feature.FeatureInformation.FeatureInclude;
import de.dentrassi.pm.osgi.feature.FeatureInformation.PluginInclude;
import de.dentrassi.pm.osgi.feature.FeatureInformation.Qualifiers;
import de.dentrassi.pm.osgi.feature.FeatureInformation.Requirement;
import de.dentrassi.pm.osgi.feature.FeatureInformation.Requirement.MatchRule;

public class FeatureInformationParser
{
    private final ZipFile file;

    private final XmlHelper xml;

    public FeatureInformationParser ( final ZipFile file )
    {
        this.xml = new XmlHelper ();
        this.file = file;
    }

    public FeatureInformation parse () throws IOException
    {
        final ZipEntry ze = this.file.getEntry ( "feature.xml" );
        if ( ze == null )
        {
            return null;
        }

        Document doc;
        try ( InputStream is = this.file.getInputStream ( ze ) )
        {
            try
            {
                doc = this.xml.parse ( is );
            }
            catch ( final Exception e )
            {
                return null;
            }
        }

        final Element root = doc.getDocumentElement ();

        final String id = root.getAttribute ( "id" );
        if ( id == null )
        {
            return null;
        }

        final String version = root.getAttribute ( "version" );
        if ( version == null )
        {
            return null;
        }

        final FeatureInformation result = new FeatureInformation ();

        result.setId ( id );
        result.setVersion ( version );

        result.setProvider ( makeNull ( root.getAttribute ( "provider-name" ) ) );
        result.setLabel ( makeNull ( root.getAttribute ( "label" ) ) );
        result.setPlugin ( makeNull ( root.getAttribute ( "plugin" ) ) );

        result.setQualifiers ( Qualifiers.parse ( root ) );

        for ( final Node node : XmlHelper.iter ( root.getChildNodes () ) )
        {
            if ( ! ( node instanceof Element ) )
            {
                continue;
            }

            final Element ele = (Element)node;

            if ( "description".equals ( node.getNodeName () ) )
            {
                result.setDescriptionUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setDescription ( trim ( ele.getTextContent () ) );
            }

            if ( "copyright".equals ( node.getNodeName () ) )
            {
                result.setCopyrightUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setCopyright ( trim ( ele.getTextContent () ) );
            }

            if ( "license".equals ( node.getNodeName () ) )
            {
                result.setLicenseUrl ( makeNull ( ele.getAttribute ( "url" ) ) );
                result.setLicense ( trim ( ele.getTextContent () ) );
            }

            if ( "includes".equals ( node.getNodeName () ) )
            {
                processFeatureInclude ( result, ele );
            }

            if ( "requires".equals ( node.getNodeName () ) )
            {
                processRequirements ( result, ele );
            }

            if ( "plugin".equals ( node.getNodeName () ) )
            {
                processPluginInclude ( result, ele );
            }
        }

        attachLocalization ( result );

        return result;
    }

    private String trim ( final String text )
    {
        if ( text == null )
        {
            return text;
        }
        return text.trim ();
    }

    private String makeNull ( final String value )
    {
        if ( value == null || value.isEmpty () )
        {
            return null;
        }
        return value;
    }

    private void processRequirements ( final FeatureInformation result, final Element ele )
    {
        final Set<Requirement> reqs = result.getRequirements ();

        for ( final Element im : XmlHelper.iterElement ( ele, "import" ) )
        {
            final String feature = im.getAttribute ( "feature" );
            final String plugin = im.getAttribute ( "plugin" );
            final String vs = im.getAttribute ( "version" );
            final MatchRule match = makeMatch ( im.getAttribute ( "match" ) );

            Version version = null;
            if ( vs != null )
            {
                version = new Version ( vs );
            }

            Requirement req;
            if ( feature != null )
            {
                req = new Requirement ( Requirement.Type.FEATURE, feature, version, match );
            }
            else
            {
                req = new Requirement ( Requirement.Type.PLUGIN, plugin, version, match );
            }
            reqs.add ( req );
        }
    }

    private MatchRule makeMatch ( final String matchString )
    {
        if ( matchString == null )
        {
            return MatchRule.DEFAULT;
        }

        final MatchRule mr = MatchRule.findById ( matchString );
        if ( mr != null )
        {
            return mr;
        }
        else
        {
            return MatchRule.DEFAULT;
        }
    }

    private void processPluginInclude ( final FeatureInformation result, final Element ele )
    {
        final String id = ele.getAttribute ( "id" );
        final String vs = ele.getAttribute ( "version" );
        final Version version = new Version ( vs == null ? "0.0.0" : vs );
        final boolean unpack = Boolean.parseBoolean ( ele.getAttribute ( "unpack" ) );
        final Qualifiers q = Qualifiers.parse ( ele );

        result.getIncludedPlugins ().add ( new PluginInclude ( id, version, unpack, q ) );
    }

    private void processFeatureInclude ( final FeatureInformation result, final Element ele )
    {
        final String id = ele.getAttribute ( "id" );
        final String vs = ele.getAttribute ( "version" );
        final Version version = new Version ( vs == null ? "0.0.0" : vs );
        final String name = makeNull ( ele.getAttribute ( "name" ) );
        final boolean optional = Boolean.parseBoolean ( ele.getAttribute ( "optional" ) );

        final Qualifiers q = Qualifiers.parse ( ele );

        result.getIncludedFeatures ().add ( new FeatureInclude ( id, version, name, optional, q ) );
    }

    private void attachLocalization ( final FeatureInformation result ) throws IOException
    {
        result.setLocalization ( ParserHelper.loadLocalization ( this.file, "feature" ) );
    }
}
