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
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.opendataspace.android.cmisapi.constants.OnPremiseConstant;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.services.impl.AbstractRatingsService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.RepositorySession;
import org.opendataspace.android.cmisapi.session.impl.RepositorySessionImpl;
import org.opendataspace.android.cmisapi.utils.JsonUtils;
import org.opendataspace.android.cmisapi.utils.OnPremiseUrlRegistry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The RatingsService can be used to manage like (as ratings) on any content
 * node in the repository.<br>
 * Like can be applied or removed.
 * 
 * @author Jean Marie Pascal
 */
public class OnPremiseRatingsServiceImpl extends AbstractRatingsService
{
    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public OnPremiseRatingsServiceImpl(RepositorySession repositorySession)
    {
        super(repositorySession);
    }

    /** {@inheritDoc} */
    protected UrlBuilder getRatingsUrl(Node node)
    {
        return new UrlBuilder(OnPremiseUrlRegistry.getRatingsUrl(session, node.getIdentifier()));
    }

    /** {@inheritDoc} */
    protected JSONObject getRatingsObject()
    {
        JSONObject jo = new JSONObject();
        jo.put(OnPremiseConstant.RATING_VALUE, "1");
        jo.put(OnPremiseConstant.RATINGSCHEME_VALUE, OnPremiseConstant.LIKERATINGSSCHEME_VALUE);
        return jo;
    }

    /** {@inheritDoc} */
    protected UrlBuilder getUnlikeUrl(Node node)
    {
        return new UrlBuilder(OnPremiseUrlRegistry.getUnlikeUrl(session, node.getIdentifier()));
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / INTERNAL
    // ////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    protected int computeRatingsCount(UrlBuilder url)
    {
        // read and parse
        Response resp = read(url, ErrorCodeRegistry.RATING_GENERIC);
        Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());

        Map<String, Object> j = (Map<String, Object>) json.get(OnPremiseConstant.DATA_VALUE);
        if (j.size() == 0 && j.get(OnPremiseConstant.NODESTATISTICS_VALUE) == null) { return -1; }

        Map<String, Object> js = (Map<String, Object>) j.get(OnPremiseConstant.NODESTATISTICS_VALUE);
        if (js.size() == 0 && js.get(OnPremiseConstant.LIKERATINGSSCHEME_VALUE) == null) { return -1; }

        Map<String, Object> jso = (Map<String, Object>) js.get(OnPremiseConstant.LIKERATINGSSCHEME_VALUE);
        if (jso.size() != 0 && jso.get(OnPremiseConstant.RATINGSCOUNT_VALUE) != null) { return Integer
                .parseInt(JSONConverter.getString(jso, OnPremiseConstant.RATINGSCOUNT_VALUE)); }

        return -1;
    }

    @SuppressWarnings("unchecked")
    protected boolean computeIsRated(UrlBuilder url)
    {
        // read and parse
        Response resp = read(url, ErrorCodeRegistry.RATING_GENERIC);
        Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());

        Map<String, Object> j = (Map<String, Object>) json.get(OnPremiseConstant.DATA_VALUE);
        if (j.size() == 0 && j.get(OnPremiseConstant.RATINGS_VALUE) == null) { return false; }

        Map<String, Object> js = (Map<String, Object>) j.get(OnPremiseConstant.RATINGS_VALUE);
        if (js.size() == 0 && js.get(OnPremiseConstant.LIKERATINGSSCHEME_VALUE) == null) { return false; }

        Map<String, Object> jso = (Map<String, Object>) js.get(OnPremiseConstant.LIKERATINGSSCHEME_VALUE);
        if (jso.size() != 0 && jso.get(OnPremiseConstant.APPLIEDBY_VALUE) != null) { return session
                .getPersonIdentifier().equals(JSONConverter.getString(jso, OnPremiseConstant.APPLIEDBY_VALUE)); }

        return false;
    }
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<OnPremiseRatingsServiceImpl> CREATOR = new Parcelable.Creator<OnPremiseRatingsServiceImpl>()
    {
        public OnPremiseRatingsServiceImpl createFromParcel(Parcel in)
        {
            return new OnPremiseRatingsServiceImpl(in);
        }

        public OnPremiseRatingsServiceImpl[] newArray(int size)
        {
            return new OnPremiseRatingsServiceImpl[size];
        }
    };

    public OnPremiseRatingsServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(RepositorySessionImpl.class.getClassLoader()));
    }

}
