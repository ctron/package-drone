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
package de.dentrassi.pm.core.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.dentrassi.pm.common.MetaKey;
import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.storage.jpa.GlobalPropertyEntity;
import de.dentrassi.pm.storage.jpa.GlobalPropertyKey;

public class CoreServiceImpl extends AbstractJpaServiceImpl implements CoreService
{

    @Override
    public String getCoreProperty ( final MetaKey key, final String defaultValue )
    {
        return doWithTransaction ( ( em ) -> {
            final GlobalPropertyEntity pe = em.find ( GlobalPropertyEntity.class, new GlobalPropertyKey ( key.getNamespace (), key.getKey () ) );
            if ( pe == null )
            {
                return defaultValue;
            }
            return pe.getValue ();
        } );
    }

    @Override
    public Map<MetaKey, String> getCoreProperties ( final Collection<MetaKey> inputKeys )
    {
        if ( inputKeys == null || inputKeys.isEmpty () )
        {
            // nothing to do
            return Collections.emptyMap ();
        }

        // make a copy ... we will remove keys

        final Set<MetaKey> keys = new HashSet<> ( inputKeys );

        // make the result holder

        final Map<MetaKey, String> result = new HashMap<> ( keys.size () );

        // Convert keys

        final Set<String> propKeys = new HashSet<> ( inputKeys.size () );
        inputKeys.stream ().map ( in -> in.getNamespace () + ":" + in.getKey () ).forEach ( propKeys::add );

        // access database

        doWithTransactionVoid ( ( em ) -> {

            final TypedQuery<Object[]> q = em.createQuery ( String.format ( "SELECT gpe.namespace,gpe.key,gpe.value from %s gpe where concat(gpe.namespace,':',gpe.key) in :KEYS", GlobalPropertyEntity.class.getName () ), Object[].class );
            q.setParameter ( "KEYS", propKeys );

            for ( final Object[] row : q.getResultList () )
            {
                final MetaKey key = new MetaKey ( (String)row[0], (String)row[1] );
                keys.remove ( key );
                result.put ( key, (String)row[2] );
            }
        } );

        // fill up the not-found keys with null

        for ( final MetaKey key : keys )
        {
            result.put ( key, null );
        }

        // return the result

        return result;
    }

    @Override
    public void setCoreProperties ( final Map<MetaKey, String> properties )
    {
        doWithTransactionVoid ( ( em ) -> {
            properties.forEach ( ( key, value ) -> internalSet ( em, key.getNamespace (), key.getKey (), value ) );
        } );
    }

    @Override
    public void setCoreProperty ( final MetaKey key, final String value )
    {
        doWithTransactionVoid ( ( em ) -> {
            internalSet ( em, key.getNamespace (), key.getKey (), value );
        } );
    }

    protected void internalSet ( final EntityManager em, final String namespace, final String key, final String value )
    {
        GlobalPropertyEntity pe = em.find ( GlobalPropertyEntity.class, new GlobalPropertyKey ( namespace, key ) );
        if ( value == null )
        {
            // delete
            if ( pe != null )
            {
                em.remove ( pe );
            }
        }
        else
        {
            if ( pe != null )
            {
                pe.setValue ( value );
            }
            else
            {
                pe = new GlobalPropertyEntity ();
                pe.setNamespace ( namespace );
                pe.setKey ( key );
                pe.setValue ( value );
            }
            em.persist ( pe );
        }
    }

    @Override
    public SortedMap<MetaKey, String> list ()
    {
        final SortedMap<MetaKey, String> result = new TreeMap<> ();

        doWithTransactionVoid ( ( em ) -> {
            final TypedQuery<GlobalPropertyEntity> q = em.createQuery ( String.format ( "select gp from %s as gp", GlobalPropertyEntity.class.getName () ), GlobalPropertyEntity.class );

            for ( final GlobalPropertyEntity gp : q.getResultList () )
            {
                result.put ( new MetaKey ( gp.getNamespace (), gp.getKey () ), gp.getValue () );
            }
        } );

        return result;
    }
}
