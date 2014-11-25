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
package de.dentrassi.pm.storage.web;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.dentrassi.pm.storage.web.menu.DefaultMenuExtender;
import de.dentrassi.pm.storage.web.menu.MenuExtender;
import de.dentrassi.pm.storage.web.setup.SetupController;

public class AbstractDefaultController implements InitializingBean, DisposableBean
{

    private ServiceRegistration<MenuExtender> menuExtenderHandle;

    @Override
    public void afterPropertiesSet () throws Exception
    {
        final DefaultMenuExtender menuExtener = new DefaultMenuExtender ();
        fillMenu ( menuExtener );
        this.menuExtenderHandle = FrameworkUtil.getBundle ( SetupController.class ).getBundleContext ().registerService ( MenuExtender.class, menuExtener, null );
    }

    protected void fillMenu ( final DefaultMenuExtender menuExtener )
    {
    }

    @Override
    public void destroy () throws Exception
    {
        if ( this.menuExtenderHandle != null )
        {
            this.menuExtenderHandle.unregister ();
            this.menuExtenderHandle = null;
        }
    }

}
