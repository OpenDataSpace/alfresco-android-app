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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendataspace.android.cmisapi.constants.CloudConstant;
import org.opendataspace.android.cmisapi.exceptions.ErrorCodeRegistry;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.model.PagingResult;
import org.opendataspace.android.cmisapi.model.Tag;
import org.opendataspace.android.cmisapi.model.impl.PagingResultImpl;
import org.opendataspace.android.cmisapi.model.impl.TagImpl;
import org.opendataspace.android.cmisapi.services.ServiceRegistry;
import org.opendataspace.android.cmisapi.services.TaggingService;
import org.opendataspace.android.cmisapi.services.impl.AlfrescoService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.cmisapi.session.CloudSession;
import org.opendataspace.android.cmisapi.session.impl.CloudSessionImpl;
import org.opendataspace.android.cmisapi.utils.CloudUrlRegistry;
import org.opendataspace.android.cmisapi.utils.JsonDataWriter;
import org.opendataspace.android.cmisapi.utils.PublicAPIResponse;
import org.opendataspace.android.cmisapi.utils.messages.Messagesl18n;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Jean Marie Pascal
 */
public class CloudTaggingServiceImpl extends AlfrescoService implements TaggingService
{

    /**
     * Default constructor for service. </br> Used by the
     * {@link ServiceRegistry}.
     * 
     * @param repositorySession
     */
    public CloudTaggingServiceImpl(CloudSession repositorySession)
    {
        super(repositorySession);
    }

    /** {@inheritDoc} */
    public List<Tag> getAllTags() 
    {
        return getAllTags(null).getList();
    }

    /** {@inheritDoc} */
    public PagingResult<Tag> getAllTags(ListingContext listingContext)
    {
        try
        {
            String link = CloudUrlRegistry.getTagsUrl((CloudSession) session);
            UrlBuilder url = new UrlBuilder(link);
            if (listingContext != null)
            {
                url.addParameter(CloudConstant.MAX_ITEMS_VALUE, listingContext.getMaxItems());
                url.addParameter(CloudConstant.SKIP_COUNT_VALUE, listingContext.getSkipCount());
            }
            return computeTag(url);
        }
        catch (Exception e)
        {
            convertException(e);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<Tag> getTags(Node node)
    {
        return getTags(node, null).getList();
    }

    /** {@inheritDoc} */
    public PagingResult<Tag> getTags(Node node, ListingContext listingContext)
    {
        if (isObjectNull(node)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "node")); }
        try
        {
            String link = CloudUrlRegistry.getTagsUrl((CloudSession) session, node.getIdentifier());
            UrlBuilder url = new UrlBuilder(link);
            if (listingContext != null)
            {
                url.addParameter(CloudConstant.MAX_ITEMS_VALUE, listingContext.getMaxItems());
                url.addParameter(CloudConstant.SKIP_COUNT_VALUE, listingContext.getSkipCount());
            }
            return computeTag(url);
        }
        catch (Exception e)
        {
            convertException(e);
        }
        return null;
    }

    /** {@inheritDoc} */
    public void addTags(Node node, List<String> tags)
    {
        if (isObjectNull(node)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "node")); }

        if (isListNull(tags)) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "tags")); }
        try
        {
            String link = CloudUrlRegistry.getTagsUrl((CloudSession) session, node.getIdentifier());
            UrlBuilder url = new UrlBuilder(link);

            // prepare json data
            JSONArray ja = new JSONArray();
            JSONObject jo = null;
            for (String tag : tags)
            {
                jo = new JSONObject();
                jo.put(CloudConstant.TAG_VALUE, tag);
                ja.put(jo);
            }
            final JsonDataWriter formData = new JsonDataWriter(ja);

            // send
            post(url, formData.getContentType(), new Output()
            {
                public void write(OutputStream out) throws IOException
                {
                    formData.write(out);
                }
            }, ErrorCodeRegistry.TAGGING_GENERIC);
        }
        catch (Exception e)
        {
            convertException(e);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / INTERNAL
    // ////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    private PagingResult<Tag> computeTag(UrlBuilder url)
    {
        Response resp = read(url, ErrorCodeRegistry.TAGGING_GENERIC);
        PublicAPIResponse response = new PublicAPIResponse(resp);

        List<Tag> result = new ArrayList<Tag>();
        Map<String, Object> data = null;
        for (Object entry : response.getEntries())
        {
            data = (Map<String, Object>) ((Map<String, Object>) entry).get(CloudConstant.ENTRY_VALUE);
            result.add(TagImpl.parsePublicAPIJson(data));
        }

        return new PagingResultImpl<Tag>(result, response.getHasMoreItems(), response.getSize());
    }
    
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    public static final Parcelable.Creator<CloudTaggingServiceImpl> CREATOR = new Parcelable.Creator<CloudTaggingServiceImpl>()
    {
        public CloudTaggingServiceImpl createFromParcel(Parcel in)
        {
            return new CloudTaggingServiceImpl(in);
        }

        public CloudTaggingServiceImpl[] newArray(int size)
        {
            return new CloudTaggingServiceImpl[size];
        }
    };

    public CloudTaggingServiceImpl(Parcel o)
    {
        super((AlfrescoSession) o.readParcelable(CloudSessionImpl.class.getClassLoader()));
    }
    
}