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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.opendataspace.android.cmisapi.constants.CloudConstant;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.Comment;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.model.PagingResult;
import org.opendataspace.android.cmisapi.model.impl.CommentImpl;
import org.opendataspace.android.cmisapi.model.impl.PagingResultImpl;
import org.opendataspace.android.cmisapi.services.impl.AbstractCommentService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.CloudSession;
import org.opendataspace.android.cmisapi.session.impl.CloudSessionImpl;
import org.opendataspace.android.cmisapi.utils.CloudUrlRegistry;
import org.opendataspace.android.cmisapi.utils.PublicAPIResponse;
import org.opendataspace.android.cmisapi.utils.messages.Messagesl18n;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Specific implementation of CommentService for Public Cloud API.
 * 
 * @author Jean Marie Pascal
 */
public class CloudCommentServiceImpl extends AbstractCommentService
{

    /**
     * Default Constructor. Only used inside ServiceRegistry.
     * 
     * @param repositorySession : Repository Session.
     */
    public CloudCommentServiceImpl(CloudSession repositorySession)
    {
        super(repositorySession);
    }

    /** {@inheritDoc} */
    protected UrlBuilder getCommentsUrl(Node node, ListingContext listingContext, boolean isReadOperation)
    {
        String link = CloudUrlRegistry.getCommentsUrl((CloudSession) session, node.getIdentifier());
        UrlBuilder url = new UrlBuilder(link);
        if (listingContext != null)
        {
            url.addParameter(CloudConstant.SKIP_COUNT_VALUE, listingContext.getSkipCount());
            url.addParameter(CloudConstant.MAX_ITEMS_VALUE, listingContext.getMaxItems());
        }
        return url;
    }

    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    protected Comment parseData(Map<String, Object> json)
    {
        return CommentImpl.parsePublicAPIJson((Map<String, Object>) json.get(CloudConstant.ENTRY_VALUE));
    }

    /** {@inheritDoc} */
    protected UrlBuilder getCommentUrl(Node node, Comment comment)
    {
        if (isObjectNull(node)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "node")); }
        return new UrlBuilder(CloudUrlRegistry.getCommentUrl((CloudSession) session, node.getIdentifier(),
                comment.getIdentifier()));
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / INTERNAL
    // ////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    /** {@inheritDoc} */
    protected PagingResult<Comment> computeComment(UrlBuilder url)
    {
        // read and parse
        Response resp = read(url, ErrorCodeRegistry.COMMENT_GENERIC);
        PublicAPIResponse response = new PublicAPIResponse(resp);

        List<Comment> result = new ArrayList<Comment>();
        Map<String, Object> data = null;
        for (Object entry : response.getEntries())
        {
            data = (Map<String, Object>) ((Map<String, Object>) entry).get(CloudConstant.ENTRY_VALUE);
            result.add(CommentImpl.parsePublicAPIJson(data));
        }

        return new PagingResultImpl<Comment>(result, response.getHasMoreItems(), response.getSize());
    }
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<CloudCommentServiceImpl> CREATOR = new Parcelable.Creator<CloudCommentServiceImpl>()
    {
        public CloudCommentServiceImpl createFromParcel(Parcel in)
        {
            return new CloudCommentServiceImpl(in);
        }

        public CloudCommentServiceImpl[] newArray(int size)
        {
            return new CloudCommentServiceImpl[size];
        }
    };

    public CloudCommentServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(CloudSessionImpl.class.getClassLoader()));
    }
}