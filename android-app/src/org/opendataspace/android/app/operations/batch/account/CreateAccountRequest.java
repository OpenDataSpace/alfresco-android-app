/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 *  This file is part of Alfresco Mobile for Android.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.app.operations.batch.account;


import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.accounts.Account.ProtocolType;
import org.opendataspace.android.app.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;

public class CreateAccountRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 105;

    public static final String SESSION_MIME = "AlfrescoSession";

    protected String baseUrl;

    protected String username;

    protected String password;

    protected String description;

    protected OAuthData data;

    protected Account.ProtocolType proto;

    public CreateAccountRequest()
    {
        super();
        requestTypeId = TYPE_ID;
        proto = Account.ProtocolType.ATOM;

        setMimeType(SESSION_MIME);
    }

    public CreateAccountRequest(String url, String username, String password, String description, Account.ProtocolType proto)
    {
        this();
        this.baseUrl = url;
        this.username = username;
        this.password = password;
        this.description = description;
        this.proto = proto;
        setNotificationTitle(description);
    }

    public CreateAccountRequest(OAuthData data)
    {
        this();
        this.data = data;
        setNotificationTitle("Cloud");
    }

    @Override
    public String getRequestIdentifier()
    {
        return baseUrl + "@" + username;
    }


    public String getBaseUrl()
    {
        return baseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDescription()
    {
        return description;
    }

    public OAuthData getData()
    {
        return data;
    }

    public ProtocolType getProto()
    {
        return proto;
    }

}
