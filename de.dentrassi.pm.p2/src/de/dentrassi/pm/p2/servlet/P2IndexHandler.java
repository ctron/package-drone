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
package de.dentrassi.pm.p2.servlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.dentrassi.pm.storage.service.Channel;

public class P2IndexHandler implements Handler
{
    public P2IndexHandler ( final Channel channel )
    {
    }

    @Override
    public void prepare () throws Exception
    {
    }

    @Override
    public void process ( final HttpServletRequest req, final HttpServletResponse resp ) throws Exception
    {
        resp.setContentType ( "text/plain" );
        final PrintWriter w = resp.getWriter ();

        w.println ( "version=1" );
        w.println ( "metadata.repository.factory.order=content.xml,\\!" );
        w.println ( "artifact.repository.factory.order=artifacts.xml,\\!" );
    }
}
