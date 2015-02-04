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
package de.dentrassi.pm.storage.web.deploy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import de.dentrassi.osgi.web.controller.binding.PathVariable;
import de.dentrassi.osgi.web.controller.binding.RequestParameter;
import de.dentrassi.osgi.web.controller.form.FormData;
import de.dentrassi.pm.common.web.CommonController;
import de.dentrassi.pm.common.web.InterfaceExtender;
import de.dentrassi.pm.common.web.Modifier;
import de.dentrassi.pm.common.web.menu.MenuEntry;
import de.dentrassi.pm.sec.web.controller.Secured;
import de.dentrassi.pm.sec.web.controller.SecuredControllerInterceptor;
import de.dentrassi.pm.sec.web.filter.SecurityFilter;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.service.DeployAuthService;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs;
import de.dentrassi.pm.storage.web.breadcrumbs.Breadcrumbs.Entry;

@Controller
@RequestMapping ( "/deploy/auth" )
@ViewResolver ( "/WEB-INF/views/deploy/auth/%s.jsp" )
@Secured
@ControllerInterceptor ( SecuredControllerInterceptor.class )
public class DeployAuthController implements InterfaceExtender
{
    public final static Object GROUP_ACTION_TAG = new Object ();

    private static final int PAGE_SIZE = 25;

    private DeployAuthService service;

    public void setService ( final DeployAuthService service )
    {
        this.service = service;
    }

    @Override
    public List<MenuEntry> getMainMenuEntries ( final HttpServletRequest request )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( SecurityFilter.isLoggedIn ( request ) )
        {
            result.add ( new MenuEntry ( "Administration", 10_000, "Deploy Keys", 2_000, LinkTarget.createFromController ( DeployAuthController.class, "listGroups" ), null, null ) );
        }

        return result;
    }

    @Override
    public List<MenuEntry> getActions ( final HttpServletRequest request, final Object object )
    {
        final List<MenuEntry> result = new LinkedList<> ();

        if ( SecurityFilter.isLoggedIn ( request ) )
        {
            if ( GROUP_ACTION_TAG.equals ( object ) )
            {
                result.add ( new MenuEntry ( "Add group", 2_000, LinkTarget.createFromController ( DeployAuthController.class, "addGroup" ), Modifier.PRIMARY, null ) );
            }
            else if ( object instanceof DeployGroup )
            {
                final DeployGroup dg = (DeployGroup)object;
                final Map<String, Object> model = new HashMap<> ();

                model.put ( "groupId", dg.getId () );

                result.add ( new MenuEntry ( "Create key", 2_000, LinkTarget.createFromController ( DeployAuthController.class, "createDeployKey" ).expand ( model ), Modifier.PRIMARY, null ) );
                result.add ( new MenuEntry ( "Edit", 3_000, LinkTarget.createFromController ( DeployAuthController.class, "editGroup" ).expand ( model ), Modifier.DEFAULT, null ) );
            }
        }
        return result;
    }

    protected void addBreadcrumbs ( final String action, final DeployGroup group, final Map<String, Object> model )
    {
        final List<Entry> entries = new LinkedList<> ();

        entries.add ( new Entry ( "Home", "/" ) );
        entries.add ( Breadcrumbs.create ( "Deploy Groups", DeployAuthController.class, "listGroups" ) );
        if ( group != null )
        {
            entries.add ( Breadcrumbs.create ( "Group", DeployAuthController.class, "viewGroup", Collections.singletonMap ( "groupId", group.getId () ) ) );
        }
        entries.add ( new Entry ( action ) );

        model.put ( "breadcrumbs", new Breadcrumbs ( entries ) );
    }

    @RequestMapping ( value = "/group", method = RequestMethod.GET )
    public ModelAndView listGroups ( @RequestParameter ( required = false, value = "position" ) Integer position )
    {
        final ModelAndView result = new ModelAndView ( "listGroups" );

        if ( position == null )
        {
            position = 0;
        }

        final List<DeployGroup> list = this.service.listGroups ( position, PAGE_SIZE + 1 );

        final boolean prev = position > 0;
        boolean next;

        if ( list.size () > PAGE_SIZE )
        {
            // check if we have more
            next = true;
            list.remove ( list.size () - 1 );
        }
        else
        {
            next = false;
        }

        result.put ( "groups", list );

        result.put ( "prev", prev );
        result.put ( "next", next );
        result.put ( "position", position );
        result.put ( "pageSize", PAGE_SIZE );

        return result;
    }

    @RequestMapping ( value = "/key", method = RequestMethod.GET )
    public ModelAndView listKeys ( @RequestParameter ( required = false, value = "position" ) Integer position )
    {
        final ModelAndView result = new ModelAndView ( "listKeys" );

        if ( position == null )
        {
            position = 0;
        }

        final List<DeployGroup> list = this.service.listGroups ( position, PAGE_SIZE + 1 );

        final boolean prev = position > 0;
        boolean next;

        if ( list.size () > PAGE_SIZE )
        {
            // check if we have more
            next = true;
            list.remove ( list.size () - 1 );
        }
        else
        {
            next = false;
        }

        result.put ( "keys", list );

        result.put ( "prev", prev );
        result.put ( "next", next );
        result.put ( "position", position );
        result.put ( "pageSize", PAGE_SIZE );

        return result;
    }

    @RequestMapping ( value = "/key/{keyId}/delete", method = RequestMethod.GET )
    public ModelAndView deleteKeyForGroup ( @PathVariable ( "keyId" ) final String keyId )
    {
        final DeployKey key = this.service.deleteKey ( keyId );

        if ( key != null && key.getGroupId () != null )
        {
            return new ModelAndView ( "redirect:/deploy/auth/group/" + key.getGroupId () + "/view" );
        }
        else
        {
            return new ModelAndView ( "redirect:/deploy/auth/key" );
        }
    }

    @RequestMapping ( value = "/addGroup", method = RequestMethod.GET )
    public ModelAndView addGroup ()
    {
        final Map<String, Object> model = new HashMap<String, Object> ();

        addBreadcrumbs ( "Add group", null, model );

        return new ModelAndView ( "addGroup", model );
    }

    @RequestMapping ( value = "/addGroup", method = RequestMethod.POST )
    public ModelAndView addGroupPost ( @RequestParameter ( "name" ) final String name )
    {
        try
        {
            this.service.createGroup ( name );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Create deploy group", "Failed to create deploy group", e );
        }
        return new ModelAndView ( "redirect:/deploy/auth/group" );
    }

    @RequestMapping ( value = "/group/{groupId}/view" )
    public ModelAndView viewGroup ( @PathVariable ( "groupId" ) final String groupId )
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "group", group );
        addBreadcrumbs ( "View", null, model );

        return new ModelAndView ( "viewGroup", model );
    }

    @RequestMapping ( value = "/group/{groupId}/edit" )
    public ModelAndView editGroup ( @PathVariable ( "groupId" ) final String groupId )
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", group );
        addBreadcrumbs ( "Edit", group, model );

        return new ModelAndView ( "editGroup", model );
    }

    @RequestMapping ( value = "/group/{groupId}/edit", method = RequestMethod.POST )
    public ModelAndView editGroupPost ( @PathVariable ( "groupId" ) final String groupId, @Valid @FormData ( "command" ) final DeployGroup group, final BindingResult result )
    {
        final Map<String, Object> model = new HashMap<> ();

        group.setId ( groupId );

        if ( !result.hasErrors () )
        {
            this.service.updateGroup ( group );
        }

        model.put ( "command", group );
        addBreadcrumbs ( "Edit", group, model );

        return new ModelAndView ( "editGroup", model );
    }

    @RequestMapping ( value = "/key/{keyId}/edit" )
    public ModelAndView editKey ( @PathVariable ( "keyId" ) final String keyId )
    {
        final DeployKey key = this.service.getKey ( keyId );

        if ( key == null )
        {
            return CommonController.createNotFound ( "Deploy Key", keyId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "command", key );

        return new ModelAndView ( "editKey", model );
    }

    @RequestMapping ( value = "/key/{keyId}/edit", method = RequestMethod.POST )
    public ModelAndView editKeyPost ( @PathVariable ( "keyId" ) final String groupId, @Valid @FormData ( "command" ) DeployKey key, final BindingResult result )
    {
        key.setId ( groupId );

        if ( !result.hasErrors () )
        {
            key = this.service.updateKey ( key );
            if ( key != null && key.getGroupId () != null )
            {
                return new ModelAndView ( "redirect:/deploy/auth/group/" + key.getGroupId () + "/view" );
            }
        }

        return new ModelAndView ( "editKey", Collections.singletonMap ( "command", key ) );
    }

    @RequestMapping ( value = "/group/{groupId}/createKey" )
    public ModelAndView createDeployKey ( @PathVariable ( "groupId" ) final String groupId )
    {
        final DeployGroup group = this.service.getGroup ( groupId );

        if ( group == null )
        {
            return CommonController.createNotFound ( "Deploy Group", groupId );
        }

        final Map<String, Object> model = new HashMap<> ();

        model.put ( "group", group );
        addBreadcrumbs ( "Create key", group, model );

        return new ModelAndView ( "createDeployKey", model );
    }

    @RequestMapping ( value = "/group/{groupId}/createKey", method = RequestMethod.POST )
    public ModelAndView createDeployKeyPost ( @PathVariable ( "groupId" ) final String groupId, @RequestParameter ( value = "name",
            required = false ) final String name )
    {
        try
        {
            this.service.createDeployKey ( groupId, name );
            return new ModelAndView ( "redirect:/deploy/auth/group/" + groupId + "/view" );
        }
        catch ( final Exception e )
        {
            return CommonController.createError ( "Create deploy key", "Failed to create deploy key", e );
        }
    }
}
