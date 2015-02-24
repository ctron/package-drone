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
package de.dentrassi.pm.mail.service;

public interface MailService
{
    public void sendMessage ( String to, String subject, String text ) throws Exception;

    /**
     * Send a message <br/>
     * The content of the message is read from the readable parameter. The
     * method will not close the readable.
     *
     * @param to
     *            the recipient address
     * @param subject
     *            the subject (without prefix)
     * @param readable
     *            the readable providing the content
     * @throws Exception
     *             if anything goes wrong
     */
    public void sendMessage ( String to, String subject, Readable readable ) throws Exception;
}
