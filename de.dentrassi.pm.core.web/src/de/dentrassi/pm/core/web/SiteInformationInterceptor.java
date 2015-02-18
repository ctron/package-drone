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
package de.dentrassi.pm.core.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.RequestHandler;
import de.dentrassi.osgi.web.interceptor.ModelAndViewInterceptorAdapter;
import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.MetaKeys;
import de.dentrassi.pm.core.CoreService;

public class SiteInformationInterceptor extends ModelAndViewInterceptorAdapter
{

    private final static Logger logger = LoggerFactory.getLogger ( SiteInformationInterceptor.class );

    private CoreService service;

    public void setService ( final CoreService service )
    {
        this.service = service;
    }

    @Override
    protected void postHandle ( final HttpServletRequest request, final HttpServletResponse response, final RequestHandler requestHandler, final ModelAndView modelAndView ) throws Exception
    {
        modelAndView.put ( "siteInformation", getSiteInformation () );
    }

    private SiteInformation getSiteInformation ()
    {
        try
        {
            final Map<MetaKey, String> all = this.service.list ();
            final SiteInformation data = new SiteInformation ();
            MetaKeys.bind ( data, all );
            return data;
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to get site information", e );
            return null;
        }
    }
}
