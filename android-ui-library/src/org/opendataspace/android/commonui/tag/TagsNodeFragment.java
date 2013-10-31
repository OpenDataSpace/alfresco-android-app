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
package org.opendataspace.android.commonui.tag;

import java.util.ArrayList;

import org.opendataspace.android.asynchronous.LoaderResult;
import org.opendataspace.android.asynchronous.TagsLoader;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.model.PagingResult;
import org.opendataspace.android.cmisapi.model.Tag;
import org.opendataspace.android.commonui.fragments.BaseListFragment;
import org.opendataspace.android.commonui.R;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;

public abstract class TagsNodeFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult<Tag>>>
{
    public static final String TAG = "TagsFragment";

    protected Node node;

    public static final String ARGUMENT_NODE = "commentedNode";

    public TagsNodeFragment()
    {
        loaderId = TagsLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_tag;
    }

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    @Override
    public Loader<LoaderResult<PagingResult<Tag>>> onCreateLoader(int id, Bundle ba)
    {
        setListShown(false);
        bundle = (ba == null) ? getArguments() : ba;
        ListingContext lc = null, lcorigin = null;
        if (bundle != null)
        {
            node = (Node) bundle.getSerializable(ARGUMENT_NODE);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }
        TagsLoader tg = new TagsLoader(getActivity(), alfSession, node);
        tg.setListingContext(lc);
        return tg;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<Tag>>> arg0,
            LoaderResult<PagingResult<Tag>> results)
    {
        if (adapter == null)
        {
            adapter = new TagsAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Tag>(0));
        }
        
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<Tag>>> arg0)
    {
        // TODO Auto-generated method stub
    }

}
