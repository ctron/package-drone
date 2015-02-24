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
package de.dentrassi.osgi.job;

import org.eclipse.scada.utils.ExceptionHelper;

public class ErrorInformation
{
    private String message;

    private String formatted;

    private String rootFormatted;

    public String getMessage ()
    {
        return this.message;
    }

    public void setMessage ( final String message )
    {
        this.message = message;
    }

    public String getFormatted ()
    {
        return this.formatted;
    }

    public void setFormatted ( final String formatted )
    {
        this.formatted = formatted;
    }

    public String getRootFormatted ()
    {
        return this.rootFormatted;
    }

    public void setRootFormatted ( final String rootFormatted )
    {
        this.rootFormatted = rootFormatted;
    }

    public static ErrorInformation createFrom ( final Throwable e )
    {
        if ( e == null )
        {
            return null;
        }

        final ErrorInformation err = new ErrorInformation ();
        err.setFormatted ( ExceptionHelper.formatted ( e ) );
        err.setMessage ( ExceptionHelper.getMessage ( e ) );
        err.setRootFormatted ( ExceptionHelper.formatted ( ExceptionHelper.getRootCause ( e ) ) );
        return err;
    }

}
