/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * <p>
 * This file is part of Alfresco Mobile for Android.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.exception;

import android.content.Context;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.opendataspace.android.app.R;

import javax.net.ssl.SSLHandshakeException;
import java.net.UnknownHostException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

/**
 * Helper class to find the right user message to display when an exception has
 * occured.
 *
 * @author Jean Marie Pascal
 */
public final class SessionExceptionHelper
{

    private SessionExceptionHelper()
    {
    }

    /**
     * Return user friendly message Id for a specific exception.
     *
     * @param context :
     * @param e       : exception occured
     * @return message Id
     */
    public static int getMessageId(Context context, Exception e)
    {
        final int messageId;
        Throwable cause = e;

        while (cause != null && !(cause instanceof CmisBaseException))
        {
            cause = cause.getCause();
        }

        if (cause == null)
        {
            cause = e;
        }

        // Case where the user has no right (server configuration or wrong
        // username/password)
        if (checkCause(cause, 0, CmisUnauthorizedException.class, null))
        {
            messageId = R.string.error_session_unauthorized;
        }
        // Case where the ALL url seems to be wrong.
        else if (checkCause(cause, 0, CmisObjectNotFoundException.class, null))
        {
            messageId = R.string.error_session_service_url;
        }
        // Case where the port seems to be wrong.
        else if (checkCause(cause, 0, CmisRuntimeException.class, "Service Temporarily Unavailable"))
        {
            messageId = R.string.error_session_service_unavailable;
        }
        // Case where the port seems to be wrong.
        else if (checkCause(cause, 0, CmisRuntimeException.class, "Found"))
        {
            messageId = R.string.error_session_port;
        }
        // Case where the hostname is wrong or no data connection.
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, UnknownHostException.class, null))
        {
            if (ConnectivityUtils.hasInternetAvailable(context))
            {
                messageId = R.string.error_session_hostname;
            }
            else
            {
                messageId = R.string.error_session_nodata;
            }
        }
        // Case where missing certificate / untrusted certificate
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, SSLHandshakeException.class, null) &&
                (checkCause(cause, 2, CertPathValidatorException.class,
                        "Trust anchor for certification path not found.") ||
                        checkCause(cause, 2, CertificateException.class,
                                "Trust anchor for certification path not found.")))
        {
            messageId = R.string.error_session_certificate;
        }
        // Case where the certificate has expired or is not yet valid.
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, SSLHandshakeException.class, null) &&
                checkCause(cause, 2, CertificateException.class, "Could not validate certificate: current time:"))
        {
            messageId = R.string.error_session_certificate_expired;
        }
        // Case where the certificate has expired or is not yet valid.
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, SSLHandshakeException.class, null) &&
                (checkCause(cause, 2, CertificateExpiredException.class, null) ||
                        checkCause(cause, 2, CertificateNotYetValidException.class, null)))
        {
            messageId = R.string.error_session_certificate_expired;
        }
        // Generic Certificate error
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, SSLHandshakeException.class, null) &&
                checkCause(cause, 2, CertificateException.class, null))
        {
            messageId = R.string.error_session_certificate;
        }
        // Generic SSL error
        else if (checkCause(cause, 0, CmisConnectionException.class, null) &&
                checkCause(cause, 1, SSLHandshakeException.class, null))
        {
            messageId = R.string.error_session_ssl;
        }
        // Case where the service url seems to be wrong.
        else if (checkCause(cause, 0, CmisConnectionException.class, "Cannot access"))
        {
            messageId = R.string.error_session_notfound;
        }
        else if (checkCause(e, 0, AlfrescoServiceException.class, "API plan limit exceeded"))
        {
            messageId = R.string.error_general;
        }
        else
        // Default case. We don't know what's wrong...
        {
            messageId = R.string.error_session_creation;
        }

        return messageId;
    }

    private static boolean checkCause(Throwable cause, int level, Class<?> cls, String message)
    {
        while (cause != null && level > 0)
        {
            cause = cause.getCause();
            --level;
        }

        return cause != null && cls.isInstance(cause) && level == 0 &&
                (message == null || cause.getMessage().contains(message));

    }
}
