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
package de.dentrassi.pm.setup.web;

import java.lang.reflect.Method;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.osgi.framework.FrameworkUtil;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.mail.service.MailService;
import de.dentrassi.pm.sec.web.controller.HttpConstraints;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.setup.web.internal.Activator;

@Controller
@RequestMapping ( value = "/config" )
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = "ADMIN" )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private static final String SYSPROP_STORAGE_BASE = "drone.storage.base";

    private final static Method METHOD_MAIN = LinkTarget.getControllerMethod ( ConfigController.class, "config" );

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( HttpConstraints.isCallAllowed ( METHOD_MAIN, request ) )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Storage Setup", 100, LinkTarget.createFromController ( METHOD_MAIN ), Modifier.DEFAULT, null, false, 0 ) );
        }

        return result;
    }

    private void fillData ( final Map<String, Object> model )
    {
        model.put ( "sysProp", System.getProperty ( SYSPROP_STORAGE_BASE ) );
        model.put ( "freeSpacePercent", freeSpace () );
    }

    private Double freeSpace ()
    {
        try
        {
            final String base = System.getProperty ( SYSPROP_STORAGE_BASE );
            final Path p = Paths.get ( base );

            final FileStore store = Files.getFileStore ( p );
            return (double)store.getUnallocatedSpace () / (double)store.getTotalSpace ();
        }
        catch ( final Exception e )
        {
            return null;
        }
    }

    @RequestMapping
    public ModelAndView config ()
    {
        final Map<String, Object> model = new HashMap<> ();

        final StorageConfiguration command = new StorageConfiguration ();
        model.put ( "command", command );
        fillData ( model );

        return new ModelAndView ( "/config/index", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView configPost ( @Valid @FormData ( "command" ) final StorageConfiguration data, final BindingResult result)
    {
        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", data );

        fillData ( model );

        if ( !result.hasErrors () )
        {
            // FIXME: apply

            // now wait until the configuration was performed in the background

            try
            {
                Activator.getTracker ().waitForService ( 5000 );
            }
            catch ( final InterruptedException e )
            {
            }
        }

        // either we still have something to do here, or we are fully set up

        if ( isMailServicePresent () || result.hasErrors () )
        {
            return new ModelAndView ( "/config/index", model );
        }
        else
        {
            return new ModelAndView ( "redirect:/setup" );
        }
    }

    protected boolean isMailServicePresent ()
    {
        return FrameworkUtil.getBundle ( ConfigController.class ).getBundleContext ().getServiceReference ( MailService.class ) != null;
    }

}
