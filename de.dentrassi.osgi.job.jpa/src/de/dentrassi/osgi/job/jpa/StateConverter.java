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
package de.dentrassi.osgi.job.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import de.dentrassi.osgi.job.State;

@Converter ( autoApply = true )
public class StateConverter implements AttributeConverter<State, Integer>
{
    @Override
    public Integer convertToDatabaseColumn ( final State state )
    {
        if ( state == null )
        {
            return null;
        }
        return state.getId ();
    }

    @Override
    public State convertToEntityAttribute ( final Integer id )
    {
        if ( id == null )
        {
            return null;
        }
        return State.fromId ( id );
    }
}
