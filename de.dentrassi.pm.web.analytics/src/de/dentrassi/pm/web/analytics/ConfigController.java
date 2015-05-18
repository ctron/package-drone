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
package de.dentrassi.pm.web.analytics;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.HttpConstraint;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.LinkTarget;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.ControllerInterceptor;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.sec.web.controller.HttpContraintControllerInterceptor;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/system/extend/analytics" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
@HttpConstraint ( rolesAllowed = { "ADMIN" } )
@ControllerInterceptor ( HttpContraintControllerInterceptor.class )
public class ConfigController implements InterfaceExtender
{
    private CoreService service;

    public void setService ( final CoreService service )
    {
        this.service = service;
    }

    @RequestMapping
    public ModelAndView edit ()
    {
        final Map<String, Object> model = new HashMap<> ();
        model.put ( "command", load () );

        return new ModelAndView ( "edit", model );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView editPost ( @Valid @FormData ( "command" ) final Configuration data, final BindingResult result)
    {
        if ( !result.hasErrors () )
        {
            try
            {
                final Map<MetaKey, String> md = MetaKeys.unbind ( data );
                this.service.setCoreProperties ( md );
            }
            catch ( final Exception e )
            {
                return CommonController.createError ( "Error", "Failed to update configuration", e );
            }
        }

        final Map<String, Object> model = new HashMap<> ( 1 );
        model.put ( "command", load () );
        return new ModelAndView ( "edit", model );
    }

    private Configuration load ()
    {
        final Map<MetaKey, String> md = this.service.getCoreNamespaceProperties ( Constants.NAMESPACE, Constants.KEY_TRACKING_ID, Constants.KEY_ANONYMIZE_IP, Constants.KEY_FORCE_SSL );

        final Configuration data = new Configuration ();

        try
        {
            MetaKeys.bind ( data, md );
        }
        catch ( final Exception e )
        { // ignore
        }
        return data;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        if ( !request.isUserInRole ( "ADMIN" ) )
        {
            return null;
        }

        return Collections.singletonList ( new MenuEntry ( "System", Integer.MAX_VALUE, "Analytics", 5_000, LinkTarget.createFromController ( ConfigController.class, "edit" ), null, "stats" ) );
    }
}
