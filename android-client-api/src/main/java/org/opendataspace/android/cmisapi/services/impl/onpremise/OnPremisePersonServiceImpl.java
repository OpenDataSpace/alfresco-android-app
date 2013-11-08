/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.cmisapi.services.impl.onpremise;

import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.http.HttpStatus;
import org.opendataspace.android.cmisapi.constants.OnPremiseConstant;
import org.opendataspace.android.cmisapi.exceptions.AlfrescoServiceException;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.ContentStream;
import org.opendataspace.android.cmisapi.model.Person;
import org.opendataspace.android.cmisapi.model.impl.ContentStreamImpl;
import org.opendataspace.android.cmisapi.model.impl.PersonImpl;
import org.opendataspace.android.cmisapi.services.impl.AbstractPersonService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.RepositorySession;
import org.opendataspace.android.cmisapi.session.impl.RepositorySessionImpl;
import org.opendataspace.android.cmisapi.utils.JsonUtils;
import org.opendataspace.android.cmisapi.utils.OnPremiseUrlRegistry;
import org.opendataspace.android.cmisapi.utils.messages.Messagesl18n;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The PersonService can be used to get informations about people.
 * 
 * @author Jean Marie Pascal
 */
public class OnPremisePersonServiceImpl extends AbstractPersonService
{
    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public OnPremisePersonServiceImpl(RepositorySession repositorySession)
    {
        super(repositorySession);
    }
    
    /** {@inheritDoc} */
    protected UrlBuilder getPersonDetailssUrl(String personIdentifier)
    {
        return new UrlBuilder(OnPremiseUrlRegistry.getPersonDetailssUrl(session, personIdentifier));
    }

    /** {@inheritDoc} */
    public ContentStream getAvatarStream(String personIdentifier)
    {
        if (isStringNull(personIdentifier)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "personIdentifier")); }

        try
        {
            ContentStream cf = null;

            String url = getAvatarURL(personIdentifier);

            // Alfresco Version before V4
            if (session.getRepositoryInfo().getMajorVersion() < OnPremiseConstant.ALFRESCO_VERSION_4)
            {
                Person person = getPerson(personIdentifier);
                url = OnPremiseUrlRegistry.getThumbnailsUrl(session, person.getAvatarIdentifier(),
                        OnPremiseConstant.AVATAR_VALUE);
            }

            UrlBuilder builder = new UrlBuilder(url);
            Response resp = read(builder, ErrorCodeRegistry.PERSON_GENERIC);

            cf = new ContentStreamImpl(resp.getStream(), resp.getContentTypeHeader() + ";" + resp.getCharset(), resp
                    .getContentLength().longValue());

            return cf;
        }
        catch (Exception e)
        {
            convertException(e);
        }
        return null;
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / INTERNAL
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * @param username
     * @return Returns avatar url for the specified username
     */
    private String getAvatarURL(String username)
    {
        return OnPremiseUrlRegistry.getAvatarUrl(session, username);
    }

    /** {@inheritDoc} */
    protected Person computePerson(UrlBuilder url)
    {
        Response resp = getHttpInvoker().invokeGET(url, getSessionHttp());

        // check response code
        if (resp.getResponseCode() == HttpStatus.SC_NOT_FOUND)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.PERSON_NOT_FOUND, resp.getErrorContent());
        }
        else if (resp.getResponseCode() != HttpStatus.SC_OK)
        {
            convertStatusCode(resp, ErrorCodeRegistry.PERSON_GENERIC);
        }

        Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());

        return PersonImpl.parseJson(json);
    }
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<OnPremisePersonServiceImpl> CREATOR = new Parcelable.Creator<OnPremisePersonServiceImpl>()
    {
        public OnPremisePersonServiceImpl createFromParcel(Parcel in)
        {
            return new OnPremisePersonServiceImpl(in);
        }

        public OnPremisePersonServiceImpl[] newArray(int size)
        {
            return new OnPremisePersonServiceImpl[size];
        }
    };

    public OnPremisePersonServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(RepositorySessionImpl.class.getClassLoader()));
    }

}