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
package de.dentrassi.pm.storage.jpa;

/**
 * A copy of Severity
 * <p>
 * This is actually a copy of the Severity class, which is required so that the
 * JPA unit does not have a dependency on the base bundle, which would cause the
 * Gemini JPA system to fail and prevent the weaving from working.
 * </p>
 */
public enum ValidationSeverity
{
    INFO,
    WARNING,
    ERROR;
}
