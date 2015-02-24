/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.pm.storage.service.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.common.utils.Tokens;
import de.dentrassi.pm.storage.DeployGroup;
import de.dentrassi.pm.storage.DeployKey;
import de.dentrassi.pm.storage.jpa.DeployGroupEntity;
import de.dentrassi.pm.storage.jpa.DeployKeyEntity;
import de.dentrassi.pm.storage.service.DeployAuthService;

public class DeployAuthServiceImpl extends AbstractJpaServiceImpl implements DeployAuthService
{

    @Override
    public List<DeployGroup> listGroups ( final int position, final int count )
    {
        return doWithTransaction ( ( em ) -> {
            final TypedQuery<DeployGroupEntity> q = em.createQuery ( String.format ( "select dg from %s as dg order by dg.name, dg.id asc", DeployGroupEntity.class.getName () ), DeployGroupEntity.class );

            q.setFirstResult ( position );
            if ( count > 0 )
            {
                q.setMaxResults ( count );
            }

            final List<DeployGroupEntity> resultList = q.getResultList ();

            final List<DeployGroup> result = new ArrayList<> ( resultList.size () );

            for ( final DeployGroupEntity dg : resultList )
            {
                result.add ( convert ( dg ) );
            }

            return result;
        } );
    }

    @Override
    public DeployGroup createGroup ( final String name )
    {
        return doWithTransaction ( ( em ) -> {
            final DeployGroupEntity dg = new DeployGroupEntity ();
            dg.setName ( name );
            em.persist ( dg );
            return convert ( dg );
        } );
    }

    static DeployGroup convert ( final DeployGroupEntity dg )
    {
        if ( dg == null )
        {
            return null;
        }

        final DeployGroup result = new DeployGroup ();

        result.setId ( dg.getId () );
        result.setName ( dg.getName () );

        final Collection<DeployKeyEntity> keys = dg.getKeys ();
        if ( !keys.isEmpty () )
        {
            final List<DeployKey> newKeys = new ArrayList<> ( keys.size () );
            for ( final DeployKeyEntity dk : keys )
            {
                newKeys.add ( convert ( dk ) );
            }
            result.setKeys ( newKeys );
        }

        return result;
    }

    static DeployKey convert ( final DeployKeyEntity dk )
    {
        if ( dk == null )
        {
            return null;
        }

        final DeployKey result = new DeployKey ();

        result.setId ( dk.getId () );
        result.setName ( dk.getName () );
        result.setKey ( dk.getKeyData () );
        result.setCreationTimestamp ( dk.getCreationTimestamp () );

        if ( dk.getGroup () != null )
        {
            result.setGroupId ( dk.getGroup ().getId () );
        }

        return result;
    }

    @Override
    public DeployKey deleteKey ( final String keyId )
    {
        return doWithTransaction ( ( em ) -> {

            final DeployKeyEntity key = em.find ( DeployKeyEntity.class, keyId );

            DeployKey result = null;
            if ( key != null )
            {
                result = convert ( key );
                em.remove ( key );
            }
            return result;
        } );
    }

    @Override
    public void deleteGroup ( final String groupId )
    {
        doWithTransactionVoid ( ( em ) -> {
            final DeployGroupEntity ref = em.getReference ( DeployGroupEntity.class, groupId );
            em.remove ( ref );
        } );
    }

    @Override
    public void updateGroup ( final DeployGroup group )
    {
        if ( group == null )
        {
            throw new IllegalArgumentException ( "Argument must not be null" );
        }

        doWithTransactionVoid ( ( em ) -> {
            final DeployGroupEntity dg = getGroupChecked ( em, group.getId () );
            dg.setName ( group.getName () );

            em.persist ( dg );
        } );
    }

    @Override
    public DeployKey updateKey ( final DeployKey key )
    {
        if ( key == null )
        {
            throw new IllegalArgumentException ( "Argument must not be null" );
        }

        return doWithTransaction ( ( em ) -> {
            final DeployKeyEntity dk = getKeyChecked ( em, key.getId () );
            dk.setName ( key.getName () );

            em.persist ( dk );
            return convert ( dk );
        } );
    }

    @Override
    public DeployGroup getGroup ( final String groupId )
    {
        return doWithTransaction ( ( em ) -> convert ( em.find ( DeployGroupEntity.class, groupId ) ) );
    }

    @Override
    public DeployKey getKey ( final String keyId )
    {
        return doWithTransaction ( ( em ) -> convert ( em.find ( DeployKeyEntity.class, keyId ) ) );
    }

    static DeployGroupEntity getGroupChecked ( final EntityManager em, final String groupId )
    {
        final DeployGroupEntity dg = em.find ( DeployGroupEntity.class, groupId );
        if ( dg == null )
        {
            throw new IllegalStateException ( String.format ( "Group '%s' could not be found", groupId ) );
        }
        return dg;
    }

    static DeployKeyEntity getKeyChecked ( final EntityManager em, final String keyId )
    {
        final DeployKeyEntity dk = em.find ( DeployKeyEntity.class, keyId );
        if ( dk == null )
        {
            throw new IllegalStateException ( String.format ( "Key '%s' could not be found", keyId ) );
        }
        return dk;
    }

    @Override
    public DeployKey createDeployKey ( final String groupId, final String name )
    {
        return doWithTransaction ( ( em ) -> {
            final DeployGroupEntity group = getGroupChecked ( em, groupId );
            final DeployKeyEntity dk = new DeployKeyEntity ();
            dk.setGroup ( group );
            dk.setName ( name );
            dk.setKeyData ( Tokens.createToken ( getDeployKeySize () ) );
            dk.setCreationTimestamp ( new Date () );

            em.persist ( dk );

            return convert ( dk );
        } );
    }

    private int getDeployKeySize ()
    {
        return 32;
    }

}
