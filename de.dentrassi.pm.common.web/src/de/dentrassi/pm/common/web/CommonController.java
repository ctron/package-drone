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

    public static ModelAndView createNotFound ( final String type, final String id )
    {
        final ModelAndView result = new ModelAndView ( NOT_FOUND_VIEW );

        result.put ( NOT_FOUND_VIEW_ID, id );
        result.put ( NOT_FOUND_VIEW_TYPE, type );

        result.setAlternateViewResolver ( CommonController.class );

        return result;
    }
}
