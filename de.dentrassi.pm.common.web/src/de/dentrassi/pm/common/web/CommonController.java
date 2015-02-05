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
package de.dentrassi.pm.common.web;

import org.eclipse.scada.utils.ExceptionHelper;

import de.dentrassi.osgi.web.Controller;
import de.dentrassi.osgi.web.ModelAndView;
import de.dentrassi.osgi.web.ViewResolver;

@Controller
@ViewResolver ( "/WEB-INF/views/%s.jsp" )
public class CommonController
{
    public static final String NOT_FOUND_VIEW = "common/notFound";

    public static final String NOT_FOUND_VIEW_TYPE = "type";

    public static final String NOT_FOUND_VIEW_ID = "id";

    public static final String ERROR_VIEW = "common/error";

    public static final String ERROR_VIEW_TITLE = "title";

    public static final String ERROR_VIEW_RESULT = "result";

    public static final String ERROR_VIEW_MESSAGE = "message";

    public static final String ERROR_VIEW_STACKTRACE = "stacktrace";

    public static final String ERROR_VIEW_EXCEPTION = "exception";

    public static ModelAndView createNotFound ( final String type, final String id )
    {
        final ModelAndView result = new ModelAndView ( NOT_FOUND_VIEW );

        result.put ( NOT_FOUND_VIEW_ID, id );
        result.put ( NOT_FOUND_VIEW_TYPE, type );

        result.setAlternateViewResolver ( CommonController.class );

        return result;
    }

    public static ModelAndView createError ( final String title, final String result, final Throwable e )
    {
        final ModelAndView mav = new ModelAndView ( ERROR_VIEW );

        mav.put ( ERROR_VIEW_TITLE, title );
        mav.put ( ERROR_VIEW_RESULT, result );
        mav.put ( ERROR_VIEW_EXCEPTION, e );
        if ( e != null )
        {
            mav.put ( ERROR_VIEW_MESSAGE, ExceptionHelper.getMessage ( e ) );
            mav.put ( ERROR_VIEW_STACKTRACE, ExceptionHelper.formatted ( e ) );
        }

        mav.setAlternateViewResolver ( CommonController.class );

        return mav;
    }
}
