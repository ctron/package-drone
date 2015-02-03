package de.dentrassi.pm.sec.service.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.dentrassi.pm.common.service.AbstractJpaServiceImpl;
import de.dentrassi.pm.sec.jpa.UserEntity;
import de.dentrassi.pm.sec.jpa.UserEntity_;
import de.dentrassi.pm.sec.service.LoginException;

public class AbstractDatabaseUserService extends AbstractJpaServiceImpl
{

    protected UserEntity findByEmail ( final EntityManager em, final String email )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder ();
        final CriteriaQuery<UserEntity> cq = cb.createQuery ( UserEntity.class );

        final Root<UserEntity> root = cq.from ( UserEntity.class );

        cq.where ( cb.equal ( root.get ( UserEntity_.email ), email ) );

        final TypedQuery<UserEntity> q = em.createQuery ( cq );

        // limit

        q.setMaxResults ( 1 );

        // execute

        final List<UserEntity> rl = q.getResultList ();

        if ( rl.isEmpty () )
        {
            return null;
        }

        return rl.get ( 0 );
    }

    protected void validateUserAfterLogin ( final UserEntity user ) throws LoginException
    {
        if ( user.isDeleted () )
        {
            throw new LoginException ( "User is deleted" );
        }
    
        if ( user.isLocked () )
        {
            throw new LoginException ( "User is locked" );
        }
    
        if ( !user.isEmailVerified () )
        {
            throw new LoginException ( "E-mail not verified" );
        }
    }

}
