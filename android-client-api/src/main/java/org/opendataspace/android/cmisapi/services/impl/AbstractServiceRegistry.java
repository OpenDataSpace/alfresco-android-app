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
package org.opendataspace.android.cmisapi.services.impl;

import org.opendataspace.android.cmisapi.services.ActivityStreamService;
import org.opendataspace.android.cmisapi.services.CommentService;
import org.opendataspace.android.cmisapi.services.DocumentFolderService;
import org.opendataspace.android.cmisapi.services.PersonService;
import org.opendataspace.android.cmisapi.services.RatingService;
import org.opendataspace.android.cmisapi.services.SearchService;
import org.opendataspace.android.cmisapi.services.ServiceRegistry;
import org.opendataspace.android.cmisapi.services.SiteService;
import org.opendataspace.android.cmisapi.services.TaggingService;
import org.opendataspace.android.cmisapi.services.VersionService;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;

import android.os.Parcel;

/**
 * Abstract class implementation of ServiceRegistry. Responsible of sharing
 * common methods between child class (OnPremise and Cloud)
 * 
 * @author Jean Marie Pascal
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry
{
    protected DocumentFolderService documentFolderService;

    protected SearchService searchService;

    protected VersionService versionService;

    protected SiteService siteService;

    protected CommentService commentService;

    protected TaggingService taggingService;

    protected ActivityStreamService activityStreamService;

    protected RatingService ratingsService;

    protected final AlfrescoSession session;

    protected PersonService personService;

    public AbstractServiceRegistry(AlfrescoSession session)
    {
        this.session = session;
        this.versionService = new VersionServiceImpl(session);
        this.searchService = new SearchServiceImpl(session);
    }

    // ////////////////////////////////////////////////////////////////////////////////////
    // / Available anytime
    // ////////////////////////////////////////////////////////////////////////////////////
    public DocumentFolderService getDocumentFolderService()
    {
        return documentFolderService;
    }

    public SearchService getSearchService()
    {
        return searchService;
    }

    public VersionService getVersionService()
    {
        return versionService;
    }
    
    // ////////////////////////////////////////////////////
    // Save State - serialization / deserialization
    // ////////////////////////////////////////////////////
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int arg1)
    {
        dest.writeParcelable(session, PARCELABLE_WRITE_RETURN_VALUE);
    }
}
