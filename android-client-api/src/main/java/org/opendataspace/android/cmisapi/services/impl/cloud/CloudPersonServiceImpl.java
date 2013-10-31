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
package org.opendataspace.android.cmisapi.services.impl.cloud;

import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.http.HttpStatus;
import org.opendataspace.android.cmisapi.constants.CloudConstant;
import org.opendataspace.android.cmisapi.exceptions.AlfrescoServiceException;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.ContentStream;
import org.opendataspace.android.cmisapi.model.Person;
import org.opendataspace.android.cmisapi.model.impl.PersonImpl;
import org.opendataspace.android.cmisapi.services.impl.AbstractDocumentFolderServiceImpl;
import org.opendataspace.android.cmisapi.services.impl.AbstractPersonService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.CloudSession;
import org.opendataspace.android.cmisapi.session.impl.CloudSessionImpl;
import org.opendataspace.android.cmisapi.utils.CloudUrlRegistry;
import org.opendataspace.android.cmisapi.utils.JsonUtils;
import org.opendataspace.android.cmisapi.utils.messages.Messagesl18n;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The PersonService can be used to get informations about people.
 * 
 * @author Jean Marie Pascal
 */
public class CloudPersonServiceImpl extends AbstractPersonService
{

    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public CloudPersonServiceImpl(AlfrescoSession repositorySession)
    {
        super(repositorySession);
    }

    /** {@inheritDoc} */
    protected UrlBuilder getPersonDetailssUrl(String personIdentifier)
    {
        return new UrlBuilder(CloudUrlRegistry.getPersonDetailssUrl((CloudSession) session, personIdentifier));
    }

    /** {@inheritDoc} */
    public ContentStream getAvatarStream(String personIdentifier)
    {
        if (isStringNull(personIdentifier)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "personIdentifier")); }
        try
        {
            Person person = getPerson(personIdentifier);
            if (person.getAvatarIdentifier() == null){
                return null;
            }
            ContentStream st = ((AbstractDocumentFolderServiceImpl) session.getServiceRegistry()
                    .getDocumentFolderService()).downloadContentStream(person.getAvatarIdentifier());
            return st;
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
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    protected Person computePerson(UrlBuilder url)
    {
        Response resp = getHttpInvoker().invokeGET(url, getSessionHttp());

        // check response code
        if (resp.getResponseCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR || resp.getResponseCode() == HttpStatus.SC_NOT_FOUND)
        {
            throw new AlfrescoServiceException(ErrorCodeRegistry.PERSON_NOT_FOUND, resp.getErrorContent());
        }
        else if (resp.getResponseCode() != HttpStatus.SC_OK)
        {
            convertStatusCode(resp, ErrorCodeRegistry.PERSON_GENERIC);
        }

        Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());
        Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) json).get(CloudConstant.ENTRY_VALUE);
        return PersonImpl.parsePublicAPIJson(data);
    }
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<CloudPersonServiceImpl> CREATOR = new Parcelable.Creator<CloudPersonServiceImpl>()
    {
        public CloudPersonServiceImpl createFromParcel(Parcel in)
        {
            return new CloudPersonServiceImpl(in);
        }

        public CloudPersonServiceImpl[] newArray(int size)
        {
            return new CloudPersonServiceImpl[size];
        }
    };

    public CloudPersonServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(CloudSessionImpl.class.getClassLoader()));
    }

}
