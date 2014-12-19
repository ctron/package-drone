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
package de.dentrassi.osgi.web.controller.binding;

import java.util.List;

public interface BindingResult
{
    public static final String ATTRIBUTE_NAME = BindingResult.class.getName ();

    public boolean hasErrors ();

    public BindingResult getChild ( String name );

    public void addChild ( String name, BindingResult bindingResult );

    public void addError ( BindingError error );

    public List<BindingError> getErrors ();
}