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
package de.dentrassi.pm.sec.web.ui;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestMapping;
import de.dentrassi.osgi.web.RequestMethod;
import de.dentrassi.osgi.web.ViewResolver;
import de.dentrassi.osgi.web.controller.binding.BindingResult;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.sec.CreateUser;
import de.dentrassi.pm.sec.DatabaseUserInformation;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
@RequestMapping ( "/signup" )
public class SignupController extends AbstractUserCreationController
{
    private CoreService coreService;

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    @RequestMapping ( method = RequestMethod.GET )
    public ModelAndView signup ()
    {
        if ( !isSelfRegistrationAllowed () )
        {
            return new ModelAndView ( "signup/notAllowed" );
        }

        final ModelAndView model = new ModelAndView ( "signup/form" );

        model.put ( "command", new CreateUser () );

        return model;
    }

    private boolean isSelfRegistrationAllowed ()
    {
        return Boolean.parseBoolean ( this.coreService.getCoreProperty ( "allow-self-registration" ) );
    }

    @RequestMapping ( method = RequestMethod.POST )
    public ModelAndView signupPost ( @Valid @FormData ( "command" ) final CreateUser data, final BindingResult result )
    {
        if ( !isSelfRegistrationAllowed () )
        {
            return new ModelAndView ( "signup/notAllowed" );
        }

        if ( result.hasErrors () )
        {
            final Map<String, Object> model = new HashMap<> ( 2 );
            model.put ( "command", data );
            model.put ( "duplicateEmail", result.hasMarker ( "duplicateEmail" ) );
            return new ModelAndView ( "signup/form", model );
        }

        final DatabaseUserInformation newUser = this.storage.createUser ( data, false );

        return new ModelAndView ( String.format ( "signup/success", newUser.getId () ) );
    }

    @RequestMapping ( value = "/verifyEmail", method = RequestMethod.GET )
    public ModelAndView verify ( @RequestParameter ( "userId" ) final String userId, @RequestParameter ( "token" ) final String token )
    {
        final String error = this.storage.verifyEmail ( userId, token );
        if ( error == null )
        {
            return new ModelAndView ( "signup/emailVerified" );
        }

        return new ModelAndView ( "signup/verificationFailed", "error", error );
    }

    @RequestMapping ( value = "/requestEmail", method = RequestMethod.GET )
    public String requestEmail ()
    {
        return "signup/requestEmail";
    }

    @RequestMapping ( value = "/requestEmail", method = RequestMethod.POST )
    public ModelAndView requestEmailPost ( @Valid @FormData ( "command" ) final RequestEmail data, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        if ( result.hasErrors () )
        {
            model.put ( "command", data );
            return new ModelAndView ( "signup/requestEmail", model );
        }

        final String error = this.storage.reRequestEmail ( data.getEmail () );
        if ( error != null )
        {
            model.put ( "error", error );
            return new ModelAndView ( "signup/requestFailed", model );
        }

        return new ModelAndView ( "signup/emailRequested", model );
    }

    @RequestMapping ( value = "/reset", method = RequestMethod.GET )
    public ModelAndView resetPassword ()
    {
        return new ModelAndView ( "signup/reset", "command", new RequestEmail () );
    }

    @RequestMapping ( value = "/reset", method = RequestMethod.POST )
    public ModelAndView resetPasswordPost ( @Valid @FormData ( "command" ) final RequestEmail data, final BindingResult binding )
    {
        if ( binding.hasErrors () )
        {
            return new ModelAndView ( "signup/reset" );
        }

        final String error = this.storage.resetPassword ( data.getEmail () );

        if ( error != null )
        {
            final Map<String, Object> model = new HashMap<> ();
            model.put ( "error", error );
            return new ModelAndView ( "signup/passwordResetResult", model );
        }

        return new ModelAndView ( "signup/passwordResetResult" );
    }

    @RequestMapping ( value = "/newPassword", method = RequestMethod.GET )
    public ModelAndView newPassword ( @RequestParameter ( "email" ) final String email, @RequestParameter ( "token" ) final String token )
    {
        // TODO: check token first! Will be re-checked and enforced later, but gives better feedback

        final NewPassword data = new NewPassword ();

        data.setEmail ( email );
        data.setToken ( token );

        return new ModelAndView ( "signup/newPassword", "command", data );
    }

    @RequestMapping ( value = "/newPassword", method = RequestMethod.POST )
    public ModelAndView newPasswordPost ( @Valid @FormData ( "command" ) final NewPassword data, final BindingResult binding )
    {
        if ( binding.hasErrors () )
        {
            return new ModelAndView ( "signup/newPassword" );
        }

        try
        {
            this.storage.changePassword ( data.getEmail (), data.getToken (), data.getPassword () );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Password change", "Failed to change password", e );
        }

        return new ModelAndView ( "signup/passwordChangeResult" );
    }

}
