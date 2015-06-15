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
package de.dentrassi.pm.sec.web.ui.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import de.dentrassi.pm.sec.web.captcha.CaptchaService;

public class Activator implements BundleActivator
{
    private static Activator INSTANCE;

    private ServiceTracker<CaptchaService, CaptchaService> captchaServiceTracker;

    @Override
    public void start ( final BundleContext context ) throws Exception
    {
        INSTANCE = this;
        this.captchaServiceTracker = new ServiceTracker<> ( context, CaptchaService.class, null );
        this.captchaServiceTracker.open ();
    }

    @Override
    public void stop ( final BundleContext context ) throws Exception
    {
        this.captchaServiceTracker.close ();
        this.captchaServiceTracker = null;
        INSTANCE = null;
    }

    public static CaptchaService getCaptchaService ()
    {
        return INSTANCE.captchaServiceTracker.getService ();
    }

}
