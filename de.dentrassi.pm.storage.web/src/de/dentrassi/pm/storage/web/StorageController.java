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
package de.dentrassi.pm.storage.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.eclipse.scada.utils.ExceptionHelper;

import de.dentrassi.osgi.utils.Strings;
import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.MessageBindingError;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.osgi.web.controller.validator.ControllerValidator;
import de.dentrassi.osgi.web.controller.validator.ValidationContext;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.storage.service.StorageService;
import de.dentrassi.pm.storage.service.StorageServiceAdmin;

@Secured
@Controller
@ViewResolver ( "/WEB-INF/views/global/%s.jsp" )
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "MANAGER" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class StorageController implements InterfaceExtender
{
    private StorageService service;

    private CoreService coreService;

    public void setService ( final StorageService service )
    {
        this.service = service;
    }

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    private StorageServiceAdmin getAdmin ()
    {
        if ( this.service instanceof StorageServiceAdmin )
        {
            return (StorageServiceAdmin)this.service;
        }
        return null;
    }

    @RequestMapping ( value = "/system/storage" )
    @HttpConstraint ( rolesAllowed = { "MANAGER", "ADMIN" } )
    public ModelAndView index ()
    {
        final Map<String, Object> model = new HashMap<> ();

        model.put ( "blobStoreLocation", this.coreService.getCoreProperty ( "blobStoreLocation" ) );

        return new ModelAndView ( "index", model );
    }

    @RequestMapping ( value = "/system/storage/wipe", method = RequestMethod.POST )
    public ModelAndView wipe ()
    {
        this.service.wipeClean ();
        return new ModelAndView ( "redirect:/channel" );
    }

    @RequestMapping ( value = "/system/storage/fileStore", method = RequestMethod.GET )
    @HttpConstraint ( rolesAllowed = "ADMIN" )
    public ModelAndView convertToFs ()
    {
        final ConfigureFileSystem data = new ConfigureFileSystem ();

        final String location = this.coreService.getCoreProperty ( "blobStoreLocation" );
        data.setLocation ( location );

        return new ModelAndView ( "convertFsForm", "command", data );
    }

    @RequestMapping ( value = "/system/storage/fileStore", method = RequestMethod.POST )
    @HttpConstraint ( rolesAllowed = "ADMIN" )
    public ModelAndView convertToFsPost ( @Valid @FormData ( "command" ) final ConfigureFileSystem data, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( "convertFsForm" );
        }

        // process

        final StorageServiceAdmin admin = getAdmin ();

        try
        {
            admin.setBlobStoreLocation ( new File ( data.getLocation () ) );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Convert storage", "Error", e, true );
        }

        // result

        return new ModelAndView ( "convertFsResult" );
    }

    @ControllerValidator ( formDataClass = ConfigureFileSystem.class )
    public void validateFsData ( final ConfigureFileSystem data, final ValidationContext ctx )
    {
        final StorageServiceAdmin admin = getAdmin ();
        if ( admin == null )
        {
            ctx.error ( "Storage service does not support file system blob store" );
            return;
        }

        final String locationString = data.getLocation ();
        if ( locationString == null || locationString.isEmpty () )
        {
            // done by @NotEmpty
            return;
        }

        final File file = new File ( locationString );

        {
            File c = file;
            while ( c != null )
            {
                if ( !c.exists () )
                {
                    c = c.getParentFile ();
                    continue;
                }
                else if ( !c.isDirectory () )
                {
                    ctx.error ( "location", new MessageBindingError ( String.format ( "The parent location '%s' is a not a directory", c ) ) );
                    return;
                }
                else if ( !c.canWrite () || !c.canExecute () || !c.canRead () )
                {
                    ctx.error ( "location", new MessageBindingError ( String.format ( "The parent location '%s' is not accessible", c ) ) );
                    return;
                }
                break;
            }
        }

        if ( file.exists () && !file.isDirectory () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "The location '%s' already exists but is not a directory", file ) ) );
            return;
        }

        if ( file.exists () && !file.canWrite () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "The directory '%s' already exists but is not writable by this application", file ) ) );
            return;
        }

        final File cfg = new File ( file, "config.properties" );
        if ( cfg.exists () && !cfg.isFile () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "The 'config.properties' file already exists, but is not a file" ) ) );
            return;
        }

        if ( cfg.exists () && !cfg.canRead () )
        {
            ctx.error ( "location", String.format ( "The 'config.properties' file already exists, but is not readable file" ) );
            return;
        }

        final Map<String, String> map = this.coreService.getCoreProperties ( "blobStoreId", "blobStoreLocation" );
        final String id = map.get ( "blobStoreId" );
        if ( id != null )
        {
            if ( cfg.exists () )
            {
                final Properties p = new Properties ();
                try ( FileInputStream stream = new FileInputStream ( cfg ) )
                {
                    p.load ( stream );
                }
                catch ( final IOException e )
                {
                    ctx.error ( "location", String.format ( "Failed to load existing configuration: " + ExceptionHelper.getMessage ( e ) ) );
                    return;
                }
                final String cfgId = p.getProperty ( "id" );
                if ( !id.equals ( cfgId ) )
                {
                    ctx.error ( "location", String.format ( "Blob storage at '%s' does belong to a different setup (this: %s, other: %s)", file, id, cfgId ) );
                    return;
                }
            }
            else
            {
                ctx.error ( "location", String.format ( "This setup is already tied to a blob store, but the target directy is not (currentId: %s, currentLocation: %s)", id, map.get ( "blobStoreLocation" ) ) );
                return;
            }
        }
    }

    @HttpConstraint ( rolesAllowed = "ADMIN" )
    @RequestMapping ( value = "/system/storage/exportAllFs", method = RequestMethod.GET )
    public ModelAndView exportAllFs ()
    {
        return new ModelAndView ( "exportAllFs" );
    }

    @HttpConstraint ( rolesAllowed = "ADMIN" )
    @RequestMapping ( value = "/system/storage/exportAllFs", method = RequestMethod.POST )
    public ModelAndView exportAllFsPost ( @Valid @FormData ( "command" ) final ExportAllFileSystemCommand command, final BindingResult result )
    {
        if ( result.hasErrors () )
        {
            return new ModelAndView ( "exportAllFs" );
        }

        File location;
        try
        {
            location = performExport ( command );
        }
        catch ( final IOException e )
        {
            return CommonController.createError ( "Spool out", null, e, true );
        }

        final String bytes = Strings.bytes ( location.length () );

        return CommonController.createSuccess ( "Spool out", "to file system", String.format ( "<strong>Complete!</strong> Successfully spooled out all channels to <code>%s</code> (%s)", location, bytes ) );
    }

    public File performExport ( final ExportAllFileSystemCommand command ) throws IOException
    {
        final File file = new File ( command.getLocation () ).getAbsoluteFile ();

        // fail if the file exists right now
        Files.createFile ( file.toPath () );

        try ( BufferedOutputStream stream = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
        {
            this.service.exportAll ( stream );
        }

        return file;
    }

    @ControllerValidator ( formDataClass = ExportAllFileSystemCommand.class )
    public void validateExportAll ( final ExportAllFileSystemCommand command, final ValidationContext ctx )
    {
        final String locationString = command.getLocation ();
        if ( locationString == null || locationString.isEmpty () )
        {
            return;
        }

        final File location = new File ( locationString ).getAbsoluteFile ();
        if ( location.isDirectory () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must not be an existing directory", location ) ) );
            return;
        }
        if ( location.exists () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must not exist", location ) ) );
            return;
        }
        if ( !location.getParentFile ().isDirectory () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must be an existing directory", location.getParentFile () ) ) );
            return;
        }
        if ( !location.getParentFile ().canWrite () )
        {
            ctx.error ( "location", new MessageBindingError ( String.format ( "'%s' must be writable by the server", location.getParentFile () ) ) );
            return;
        }
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( request.isUserInRole ( "MANAGER" ) )
        {
            result.add ( new MenuEntry ( "System", Integer.MAX_VALUE, "Storage", 200, LinkTarget.createFromController ( StorageController.class, "index" ), null, null ) );
        }

        return result;
    }
}
