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
package de.dentrassi.pm.sec.service.jpa;

import static de.dentrassi.pm.common.utils.Tokens.createToken;
import static de.dentrassi.pm.sec.service.jpa.Users.convert;
import static de.dentrassi.pm.sec.service.jpa.Users.hashIt;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.scada.utils.ExceptionHelper;
import org.eclipse.scada.utils.str.StringHelper;
import org.eclipse.scada.utils.str.StringReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

import de.dentrassi.pm.core.CoreService;
import de.dentrassi.pm.mail.service.MailService;
import de.dentrassi.pm.sec.CreateUser;
import de.dentrassi.pm.sec.DatabaseDetails;
import de.dentrassi.pm.sec.DatabaseUserInformation;
import de.dentrassi.pm.sec.UserInformation;
import de.dentrassi.pm.sec.UserStorage;
import de.dentrassi.pm.sec.jpa.UserEntity;
import de.dentrassi.pm.sec.jpa.UserEntity_;
import de.dentrassi.pm.sec.service.LoginException;
import de.dentrassi.pm.sec.service.UserService;
import de.dentrassi.pm.sec.service.password.BadPasswordException;
import de.dentrassi.pm.sec.service.password.PasswordChecker;
import de.dentrassi.pm.system.SystemService;

public class DatabaseUserService extends AbstractDatabaseUserService implements UserService, UserStorage
{
    private final static Logger logger = LoggerFactory.getLogger ( DatabaseUserService.class );

    private static final long MIN_EMAIL_DELAY = TimeUnit.MINUTES.toMillis ( 5 );

    private MailService mailService;

    private CoreService coreService;

    private SystemService systemService;

    private PasswordChecker passwordChecker;

    public void setPasswordChecker ( final PasswordChecker passwordChecker )
    {
        this.passwordChecker = passwordChecker;
    }

    public void setSystemService ( final SystemService systemService )
    {
        this.systemService = systemService;
    }

    public void setCoreService ( final CoreService coreService )
    {
        this.coreService = coreService;
    }

    public void setMailService ( final MailService mailService )
    {
        this.mailService = mailService;
    }

    public void unsetMailService ( final MailService mailService )
    {
        this.mailService = null;
    }

    @Override
    public UserInformation refresh ( final UserInformation user )
    {
        if ( ! ( user instanceof DatabaseUserInformation ) )
        {
            return null;
        }

        try
        {
            return doWithTransaction ( ( em ) -> processRefresh ( em, (DatabaseUserInformation)user ) );
        }
        catch ( final Exception e )
        {
            return null;
        }
    }

    private UserInformation processRefresh ( final EntityManager em, final DatabaseUserInformation user )
    {
        return convert ( em.find ( UserEntity.class, user.getId () ) );
    }

    @Override
    public UserInformation checkCredentials ( final String username, final String credentials, final boolean rememberMe ) throws LoginException
    {
        return doWithTransaction ( ( em ) -> processLogin ( em, username, credentials, rememberMe ) );

    }

    private UserInformation processLogin ( final EntityManager em, final String email, final String credentials, final boolean rememberMe ) throws LoginException
    {
        final UserEntity user = findByEmail ( em, email );

        if ( user == null )
        {
            // let the next one try
            return null;
        }

        if ( user.getPasswordSalt () == null || user.getPasswordHash () == null )
        {
            // no password set
            return null;
        }

        final String credHash = hashIt ( user.getPasswordSalt (), credentials );
        if ( !credHash.equals ( user.getPasswordHash () ) )
        {
            // wrong password - let the next one try, or fail with a "not found" error
            return null;
        }

        // only fail _after_ the password has been checked, so we should be sure it is the user

        validateUserAfterLogin ( user );

        // handle remember me

        String rememberMeToken;

        if ( rememberMe )
        {
            rememberMeToken = createToken ( 128 );
            final String tokenSalt = createToken ( 32 );

            final String tokenHash = hashIt ( tokenSalt, rememberMeToken );

            user.setRememberMeTokenHash ( tokenHash );
            user.setRememberMeTokenSalt ( tokenSalt );

            em.persist ( user );
        }
        else
        {
            rememberMeToken = null;
        }

        // we made it
        return convert ( user, rememberMeToken );
    }

    @Override
    public DatabaseUserInformation getUserDetails ( final String userId )
    {
        return doWithTransaction ( ( em ) -> convert ( em.find ( UserEntity.class, userId ) ) );
    }

    @Override
    public DatabaseUserInformation getUserDetailsByEmail ( final String email )
    {
        return doWithTransaction ( ( em ) -> convert ( findByEmail ( em, email ) ) );
    }

    @Override
    public List<DatabaseUserInformation> list ( final int position, final int size )
    {
        return doWithTransaction ( ( em ) -> {

            final CriteriaBuilder cb = em.getCriteriaBuilder ();
            final CriteriaQuery<UserEntity> cq = cb.createQuery ( UserEntity.class );

            final Root<UserEntity> root = cq.from ( UserEntity.class );

            // order by

            cq.orderBy ( cb.asc ( root.get ( UserEntity_.email ) ) );

            // convert

            final TypedQuery<UserEntity> q = em.createQuery ( cq );

            // limit

            q.setFirstResult ( position );
            q.setMaxResults ( size );

            // execute

            final List<UserEntity> rl = q.getResultList ();

            final List<DatabaseUserInformation> result = new ArrayList<> ( rl.size () );
            for ( final UserEntity ue : rl )
            {
                result.add ( convert ( ue ) );
            }

            return result;
        } );

    }

    @Override
    public DatabaseUserInformation createUser ( final CreateUser data, final boolean emailVerified )
    {
        if ( !emailVerified )
        {
            // check only for external signup
            checkPassword ( data.getPassword () );
        }

        return doWithTransaction ( ( em ) -> {

            final Date now = new Date ();

            final UserEntity user = new UserEntity ();

            user.setEmail ( data.getEmail () );
            user.setName ( data.getName () );
            user.setRegistrationDate ( now );
            user.setDeleted ( false );
            user.setLocked ( false );

            applyPassword ( user, data.getPassword () );

            final String token;
            if ( emailVerified )
            {
                token = null;
                user.setEmailVerified ( true );
            }
            else
            {
                user.setEmailVerified ( false );
                token = applyNewEmailToken ( now, user );
            }

            em.persist ( user );
            em.flush ();

            if ( token != null )
            {
                sendVerifyEmail ( data.getEmail (), user.getId (), token );
            }

            return convert ( user );
        } );
    }

    private void applyPassword ( final UserEntity user, final String password )
    {
        if ( password == null || password.isEmpty () )
        {
            return;
        }

        final String salt = createToken ( 32 );
        final String passwordHash = hashIt ( salt, password );

        user.setPasswordSalt ( salt );
        user.setPasswordHash ( passwordHash );
    }

    protected String applyNewEmailToken ( final Date now, final UserEntity user )
    {
        final String token = createToken ( 32 );

        final String tokenSalt = createToken ( 32 );
        final String tokenHash = hashIt ( tokenSalt, token );

        user.setEmailToken ( tokenHash );
        user.setEmailTokenSalt ( tokenSalt );
        user.setEmailTokenDate ( now );
        return token;
    }

    protected void sendVerifyEmail ( final String email, final String userId, final String token )
    {
        final String link = String.format ( "%s/signup/verifyEmail?userId=%s&token=%s", getSitePrefix (), userId, token );

        final Map<String, String> model = new HashMap<> ();
        model.put ( "token", token );
        model.put ( "link", link );
        sendEmail ( email, "Verify your account", "verify", model );
    }

    protected void sendResetEmail ( final String email, final String resetToken )
    {
        String link;
        try
        {
            link = String.format ( "%s/signup/newPassword?email=%s&token=%s", getSitePrefix (), URLEncoder.encode ( email, "UTF-8" ), resetToken );
        }
        catch ( final UnsupportedEncodingException e )
        {
            throw new RuntimeException ( e );
        }

        final Map<String, String> model = new HashMap<> ();
        model.put ( "token", resetToken );
        model.put ( "link", link );
        sendEmail ( email, "Password reset request", "passwordReset", model );
    }

    private String getSitePrefix ()
    {
        final String prefix = this.coreService.getCoreProperty ( "site-prefix", this.systemService.getDefaultSitePrefix () );
        if ( prefix != null )
        {
            return prefix;
        }
        return "http://localhost:8080";
    }

    @Override
    public String reRequestEmail ( final String userId )
    {
        return doWithTransaction ( ( em ) -> processReRequestEmail ( em, userId ) );
    }

    private String processReRequestEmail ( final EntityManager em, final String email )
    {
        final UserEntity user = findByEmail ( em, email );

        if ( user == null )
        {
            return "User not found";
        }

        if ( user.isDeleted () )
        {
            return "User is deleted";
        }

        if ( user.isLocked () )
        {
            return "User is locked";
        }

        if ( user.getEmailToken () == null || user.isEmailVerified () )
        {
            // we are already verified
            return "E-Mail is already verified";
        }

        if ( isTooSoon ( user.getEmailTokenDate () ) )
        {
            return MessageFormat.format ( "An e-mail verification was requested at {0,time}. Please wait until {1,time} before requesting the next one!", user.getEmailTokenDate (), nextMailSlot ( user.getEmailTokenDate () ) );
        }

        final String token = applyNewEmailToken ( new Date (), user );
        sendVerifyEmail ( user.getEmail (), user.getId (), token );
        return null;
    }

    @Override
    public String verifyEmail ( final String userId, final String token )
    {
        return doWithTransaction ( ( em ) -> processVerifyEmail ( em, userId, token ) );
    }

    private String processVerifyEmail ( final EntityManager em, final String userId, final String token )
    {
        final UserEntity user = em.find ( UserEntity.class, userId );

        if ( user == null )
        {
            return "User not found";
        }

        if ( user.isDeleted () )
        {
            return "User is deleted";
        }

        if ( user.isLocked () )
        {
            return "User is locked";
        }

        if ( user.getEmailToken () == null || user.isEmailVerified () )
        {
            // we are already verified
            return null;
        }

        final String salt = user.getEmailTokenSalt ();
        final String hashedToken = hashIt ( salt, token );

        if ( hashedToken.equals ( user.getEmailToken () ) )
        {
            user.setEmailVerified ( true );
            user.setEmailToken ( null );
            user.setEmailTokenSalt ( null );
            user.setEmailTokenDate ( null );

            em.persist ( user );
            return null;
        }

        return "It may be that you clicked on a verification link which was either expired or superseeded by another e-mail request.";
    }

    @Override
    public DatabaseUserInformation updateUser ( final String userId, final DatabaseDetails data )
    {
        return doWithTransaction ( ( em ) -> {

            final UserEntity user = em.find ( UserEntity.class, userId );

            user.setEmail ( data.getEmail () );
            user.setName ( data.getName () );

            if ( data.getRoles () != null && !data.getRoles ().isEmpty () )
            {
                final TreeSet<String> sortedRoles = new TreeSet<> ( data.getRoles () );
                user.setRoles ( StringHelper.join ( sortedRoles, "," ) );
            }
            else
            {
                user.setRoles ( null );
            }

            em.persist ( user );

            return convert ( user );
        } );
    }

    @Override
    public String resetPassword ( final String email )
    {
        try
        {
            return doWithTransaction ( ( em ) -> processResetPassword ( em, email ) );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to reset password", e );
            return ExceptionHelper.getMessage ( e );
        }
    }

    private String processResetPassword ( final EntityManager em, final String email )
    {
        final UserEntity user = findByEmail ( em, email );

        if ( user == null )
        {
            return "No account for this e-mail address.";
        }

        if ( !user.isEmailVerified () )
        {
            return "The e-mail address for this account is not verified.";
        }

        if ( isTooSoon ( user.getEmailTokenDate () ) )
        {
            return MessageFormat.format ( "A password reset e-mail was requested at {0,time}. Please wait until {1,time} before requesting the next one!", user.getEmailTokenDate (), nextMailSlot ( user.getEmailTokenDate () ) );
        }

        if ( user.isLocked () )
        {
            // we silently fail, since this would give out information about the user's state
            sendEmail ( user.getEmail (), "Password reset request", "lockedUser", null );
            return null;
        }

        final String resetToken = createToken ( 64 );
        final String resetTokenSalt = createToken ( 32 );

        final String resetTokenHash = hashIt ( resetTokenSalt, resetToken );

        user.setEmailTokenSalt ( resetTokenSalt );
        user.setEmailTokenDate ( new Date () );
        user.setEmailToken ( resetTokenHash );

        em.persist ( user );

        // we don't touch the password for now, could be anybody

        sendResetEmail ( email, resetToken );

        return null;
    }

    private void sendEmail ( final String email, final String subject, final String resource, final Map<String, ?> model )
    {
        final MailService mailService = this.mailService;

        if ( mailService == null )
        {
            throw new IllegalStateException ( "Failed to send e-mail. Mail service not present!" );
        }

        final URL url = DatabaseUserService.class.getResource ( String.format ( "mails/%s.txt", resource ) );
        if ( url == null )
        {
            logger.info ( "Failed to load mail content" );
            throw new IllegalStateException ( String.format ( "Unable to find message content: %s", resource ) );
        }

        String data;
        try ( InputStream is = url.openStream (); Reader r = new InputStreamReader ( is, StandardCharsets.UTF_8 ) )
        {
            data = CharStreams.toString ( r );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to process mail content", e );
            throw new RuntimeException ( "Failed to load mail content: " + resource, e );
        }

        if ( model != null )
        {
            data = StringReplacer.replace ( data, StringReplacer.newExtendedSource ( model ), StringReplacer.DEFAULT_PATTERN, true );
        }

        try
        {
            mailService.sendMessage ( email, subject, data );
        }
        catch ( final Exception e )
        {
            logger.warn ( "Failed to send e-mail to: " + email, e );
        }
    }

    private Date nextMailSlot ( final Date date )
    {
        return new Date ( date.getTime () + MIN_EMAIL_DELAY );
    }

    private boolean isTooSoon ( final Date date )
    {
        if ( date == null )
        {
            return false;
        }

        return System.currentTimeMillis () - date.getTime () < MIN_EMAIL_DELAY;
    }

    @Override
    public void updatePassword ( final String userId, final String currentPassword, final String newPassword )
    {
        doWithTransactionVoid ( ( em ) -> processUpdatePassword ( em, userId, currentPassword, newPassword ) );
    }

    private void processUpdatePassword ( final EntityManager em, final String userId, final String currentPassword, final String newPassword )
    {
        logger.debug ( "Process password update - userId: {},  currentPassword: {},  newPassword: {}", userId, currentPassword != null ? "***" : null, newPassword != null ? "***" : null );
        final UserEntity user = getUserChecked ( em, userId );

        if ( user.isLocked () )
        {
            throw new RuntimeException ( "User is locked" );
        }

        final String currentSalt = user.getPasswordSalt ();
        final String currentHash = user.getPasswordHash ();

        checkPassword ( newPassword );

        if ( currentPassword != null && currentSalt != null && currentHash != null )
        {
            final String checkHash = hashIt ( currentSalt, currentPassword );
            if ( !currentHash.equals ( checkHash ) )
            {
                throw new RuntimeException ( "Current password is incorrect" );
            }
        }

        applyPassword ( user, newPassword );

        em.persist ( user );
    }

    @Override
    public void changePassword ( final String email, final String token, final String password )
    {
        doWithTransactionVoid ( ( em ) -> processPasswordChange ( em, email, token, password ) );
    }

    private void processPasswordChange ( final EntityManager em, final String email, final String token, final String password )
    {
        logger.debug ( "Process password change - email: {}, token: {}, password: {}", email, token, password != null ? "***" : null );

        final UserEntity user = findByEmail ( em, email );

        if ( user == null )
        {
            throw new RuntimeException ( "User not found" );
        }

        // validate token

        final String salt = user.getEmailTokenSalt ();

        if ( salt == null )
        {
            throw new RuntimeException ( "No token" );
        }

        final String hashedToken = hashIt ( salt, token );

        if ( !hashedToken.equals ( user.getEmailToken () ) )
        {
            throw new RuntimeException ( "Invalid token" );
        }

        // check for "locked" after the token was validated

        if ( user.isLocked () )
        {
            throw new RuntimeException ( "User is locked" );
        }

        checkPassword ( password );

        applyPassword ( user, password );

        user.setEmailToken ( null );
        user.setEmailTokenDate ( null );
        user.setEmailTokenSalt ( null );

        em.persist ( user );
    }

    private void checkPassword ( final String password )
    {
        try
        {
            this.passwordChecker.checkPassword ( password );
        }
        catch ( final BadPasswordException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private UserEntity getUserChecked ( final EntityManager em, final String userId )
    {
        final UserEntity user = em.find ( UserEntity.class, userId );
        if ( user == null )
        {
            throw new IllegalArgumentException ( String.format ( "User '%s' could not be found", userId ) );
        }

        return user;
    }

    @Override
    public void lockUser ( final String userId )
    {
        doWithTransactionVoid ( ( em ) -> {

            final UserEntity user = getUserChecked ( em, userId );

            if ( user.isDeleted () )
            {
                throw new IllegalArgumentException ( String.format ( "User '%s' is deleted", userId ) );
            }

            user.setLocked ( true );

            em.persist ( user );
        } );
    }

    @Override
    public void unlockUser ( final String userId )
    {
        doWithTransactionVoid ( ( em ) -> {
            final UserEntity user = getUserChecked ( em, userId );

            user.setLocked ( false );

            em.persist ( user );
        } );

    }

    @Override
    public void deleteUser ( final String userId )
    {
        doWithTransactionVoid ( ( em ) -> {
            final UserEntity user = getUserChecked ( em, userId );
            user.setDeleted ( true );
            user.setPasswordHash ( null );
            user.setPasswordSalt ( null );
            em.persist ( user );
        } );
    }
}
