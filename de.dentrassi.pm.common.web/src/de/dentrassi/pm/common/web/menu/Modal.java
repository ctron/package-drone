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
package de.dentrassi.pm.common.web.menu;

import de.dentrassi.pm.common.web.Modifier;

public class Modal
{
    public static enum ButtonFunction
    {
        CLOSE,
        SUBMIT
    }

    public static class Button
    {
        private final String label;

        private final String icon;

        private final Object modifier;

        private final ButtonFunction function;

        public Button ( final ButtonFunction function, final String label )
        {
            this ( function, label, null, null );
        }

        public Button ( final ButtonFunction function, final String label, final String icon, final Modifier modifier )
        {
            this.function = function;
            this.label = label;
            this.icon = icon;
            this.modifier = modifier != null ? modifier : Modifier.DEFAULT;
        }

        public ButtonFunction getFunction ()
        {
            return this.function;
        }

        public String getIcon ()
        {
            return this.icon;
        }

        public String getLabel ()
        {
            return this.label;
        }

        public Object getModifier ()
        {
            return this.modifier;
        }
    }

    private String title;

    private String body;

    private Button[] buttons;

    public Modal ()
    {
    }

    public Modal ( final String title, final Button... buttons )
    {
        this.title = title;
        this.buttons = buttons;
    }

    public Modal ( final String title, final String body, final Button... buttons )
    {
        this.title = title;
        this.body = body;
        this.buttons = buttons;
    }

    public String getBody ()
    {
        return this.body;
    }

    public Button[] getButtons ()
    {
        return this.buttons;
    }

    public String getTitle ()
    {
        return this.title;
    }

    public void setBody ( final String body )
    {
        this.body = body;
    }

    public void setButtons ( final Button[] buttons )
    {
        this.buttons = buttons;
    }

    public void setTitle ( final String title )
    {
        this.title = title;
    }
}
