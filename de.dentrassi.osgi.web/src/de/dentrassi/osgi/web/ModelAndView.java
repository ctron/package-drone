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
package de.dentrassi.osgi.web;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView
{
    public static final String REDIRECT_PREFIX = "redirect:";

    private String viewName;

    private Map<String, Object> model;

    public ModelAndView ( final String viewName, final Map<String, ?> model )
    {
        this.viewName = viewName;
        this.model = copyModel ( model );
    }

    public ModelAndView ( final String viewName, final String key, final Object value )
    {
        this.viewName = viewName;
        this.model = new HashMap<> ( 1 );
        this.model.put ( key, value );
    }

    public ModelAndView ( final String viewName )
    {
        this ( viewName, null );
    }

    public ModelAndView ()
    {
    }

    public void setModel ( final Map<String, Object> model )
    {
        this.model = copyModel ( model );
    }

    public Map<String, Object> getModel ()
    {
        return this.model;
    }

    public String getViewName ()
    {
        return this.viewName;
    }

    public void setViewName ( final String viewName )
    {
        this.viewName = viewName;
    }

    public ModelAndView put ( final String key, final Object value )
    {
        this.model.put ( key, value );
        return this;
    }

    public boolean isRedirect ()
    {
        return this.viewName.startsWith ( REDIRECT_PREFIX );
    }

    public String getRedirect ()
    {
        if ( isRedirect () )
        {
            return this.viewName.substring ( REDIRECT_PREFIX.length () );
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString ()
    {
        return String.format ( "[ModelAndView - %s]", this.viewName );
    }

    private static HashMap<String, Object> copyModel ( final Map<String, ?> model )
    {
        return model != null ? new HashMap<String, Object> ( model ) : new HashMap<String, Object> ();
    }

}
