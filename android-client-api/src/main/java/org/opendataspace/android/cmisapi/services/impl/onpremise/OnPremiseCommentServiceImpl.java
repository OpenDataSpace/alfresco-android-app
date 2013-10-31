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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.opendataspace.android.cmisapi.constants.OnPremiseConstant;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.Comment;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.model.PagingResult;
import org.opendataspace.android.cmisapi.model.impl.CommentImpl;
import org.opendataspace.android.cmisapi.model.impl.PagingResultImpl;
import org.opendataspace.android.cmisapi.services.impl.AbstractCommentService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.RepositorySession;
import org.opendataspace.android.cmisapi.session.impl.RepositorySessionImpl;
import org.opendataspace.android.cmisapi.utils.JsonUtils;
import org.opendataspace.android.cmisapi.utils.OnPremiseUrlRegistry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Specific implementation of CommentService for OnPremise REST API.
 * 
 * @author Jean Marie Pascal
 */
public class OnPremiseCommentServiceImpl extends AbstractCommentService
{
    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public OnPremiseCommentServiceImpl(RepositorySession repositorySession)
    {
        super(repositorySession);
    }

    /** {@inheritDoc} */
    protected UrlBuilder getCommentsUrl(Node node, ListingContext listingContext, boolean isReadOperation)
    {
        String link = OnPremiseUrlRegistry.getCommentsUrl(session, node.getIdentifier());
        UrlBuilder url = new UrlBuilder(link);

        if (listingContext != null)
        {
            url.addParameter(OnPremiseConstant.PARAM_REVERSE, listingContext.isSortAscending());
            url.addParameter(OnPremiseConstant.PARAM_STARTINDEX, listingContext.getSkipCount());
            url.addParameter(OnPremiseConstant.PARAM_PAGESIZE, listingContext.getMaxItems());
        }
        else if (isReadOperation)
        {
            url.addParameter(OnPremiseConstant.PARAM_REVERSE, true);
        }
        return url;
    }

    /** {@inheritDoc} */
    protected UrlBuilder getCommentUrl(Node node, Comment comment)
    {
        return new UrlBuilder(OnPremiseUrlRegistry.getCommentUrl(session, comment.getIdentifier()));
    }

    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    protected Comment parseData(Map<String, Object> json)
    {
        return CommentImpl.parseJson((Map<String, Object>) json.get(OnPremiseConstant.ITEM_VALUE));
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / INTERNAL
    // ////////////////////////////////////////////////////////////////////////////////////
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    protected PagingResult<Comment> computeComment(UrlBuilder url)
    {
        try
        {
            // read and parse
            Response resp = read(url, ErrorCodeRegistry.COMMENT_GENERIC);
            Map<String, Object> json = JsonUtils.parseObject(resp.getStream(), resp.getCharset());

            List<Object> jo = (List<Object>) json.get(OnPremiseConstant.ITEMS_VALUE);
            List<Comment> result = new ArrayList<Comment>(jo.size());

            for (Object obj : jo)
            {
                result.add(CommentImpl.parseJson((Map<String, Object>) obj));
            }

            int pageSize = (JSONConverter.getString(json, OnPremiseConstant.PARAM_PAGESIZE) != null) ? Integer
                    .parseInt(JSONConverter.getString(json, OnPremiseConstant.PARAM_PAGESIZE)) : 0;
            int startIndex = (JSONConverter.getString(json, OnPremiseConstant.PARAM_STARTINDEX) != null) ? Integer
                    .parseInt(JSONConverter.getString(json, OnPremiseConstant.PARAM_STARTINDEX)) : 0;
            int total = (JSONConverter.getString(json, OnPremiseConstant.TOTAL_VALUE) != null) ? Integer
                    .parseInt(JSONConverter.getString(json, OnPremiseConstant.TOTAL_VALUE)) : 0;

            boolean hasMoreItem = ((startIndex + pageSize) < total);
            return new PagingResultImpl<Comment>(result, hasMoreItem, total);
        }
        catch (Exception e)
        {
            convertException(e);
        }

        return null;
    }
    
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<OnPremiseCommentServiceImpl> CREATOR = new Parcelable.Creator<OnPremiseCommentServiceImpl>()
    {
        public OnPremiseCommentServiceImpl createFromParcel(Parcel in)
        {
            return new OnPremiseCommentServiceImpl(in);
        }

        public OnPremiseCommentServiceImpl[] newArray(int size)
        {
            return new OnPremiseCommentServiceImpl[size];
        }
    };

    public OnPremiseCommentServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(RepositorySessionImpl.class.getClassLoader()));
    }

}
