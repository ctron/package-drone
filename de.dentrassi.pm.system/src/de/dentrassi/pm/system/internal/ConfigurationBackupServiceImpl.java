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
package de.dentrassi.pm.system.internal;

import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.dentrassi.pm.common.XmlHelper;
import de.dentrassi.pm.system.ConfigurationBackupService;

public class ConfigurationBackupServiceImpl implements ConfigurationBackupService
{
    private ConfigurationAdmin configAdmin;

    private final XmlHelper xml = new XmlHelper ();

    public void setConfigAdmin ( final ConfigurationAdmin configAdmin )
    {
        this.configAdmin = configAdmin;
    }

    @Override
    public void createConfigurationBackup ( final OutputStream stream )
    {
        try
        {
            processBackup ( stream );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException ( "Failed to export backup", e );
        }
    }

    private void processBackup ( final OutputStream stream ) throws Exception
    {
        final ZipOutputStream zos = new ZipOutputStream ( stream );

        final Configuration[] cfgs = this.configAdmin.listConfigurations ( null );
        if ( cfgs != null )
        {
            storeConfigurations ( zos, cfgs );
        }

        zos.close ();
    }

    private void storeConfigurations ( final ZipOutputStream zos, final Configuration[] cfgs ) throws Exception
    {
        zos.putNextEntry ( new ZipEntry ( "configurations.xml" ) );

        final Document doc = this.xml.create ();
        final Element root = doc.createElement ( "configuration" );
        doc.appendChild ( root );
        root.setAttribute ( "version", "1" );

        for ( final Configuration cfg : cfgs )
        {
            final Element entry = XmlHelper.addElement ( root, "entry" );

            if ( cfg.getFactoryPid () != null )
            {
                entry.setAttribute ( "factoryPid", cfg.getFactoryPid () );
            }
            else if ( cfg.getPid () != null )
            {
                entry.setAttribute ( "pid", cfg.getPid () );
            }

            for ( final String key : Collections.list ( cfg.getProperties ().keys () ) )
            {
                final Object value = cfg.getProperties ().get ( key );
                final Element prop = XmlHelper.addElement ( entry, "property" );
                prop.setAttribute ( "key", key );
                if ( value != null )
                {
                    prop.setAttribute ( "type", value.getClass ().getName () );
                    prop.setTextContent ( value.toString () );
                }
            }
        }

        this.xml.write ( doc, zos );
        zos.closeEntry ();
    }
}
