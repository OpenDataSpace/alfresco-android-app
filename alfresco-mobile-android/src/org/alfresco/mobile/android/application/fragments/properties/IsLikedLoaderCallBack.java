/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.fragments.properties;

import org.alfresco.mobile.android.api.asynchronous.IsLikedLoader;
import org.alfresco.mobile.android.api.asynchronous.LikeLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.opendataspace.android.app.R;
import org.opendataspace.android.ui.logging.OdsLog;
import org.alfresco.mobile.android.application.manager.AccessibilityHelper;
import org.alfresco.mobile.android.ui.fragments.BaseLoaderCallback;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class IsLikedLoaderCallBack extends BaseLoaderCallback implements LoaderCallbacks<LoaderResult<Boolean>>
{
    private static final String TAG = "IsLikedLoaderCallBack";

    private Node node;

    private ImageView likeButton;

    private View progressView;

    public IsLikedLoaderCallBack(AlfrescoSession session, Activity context, Node node)
    {
        super();
        this.session = session;
        this.context = context;
        this.node = node;
    }

    @Override
    public Loader<LoaderResult<Boolean>> onCreateLoader(int id, Bundle args)
    {
        boolean isCreate = false;
        if (args != null)
        {
            isCreate = args.getBoolean(IS_CREATE);
        }

        if (!isCreate)
        {
            return new IsLikedLoader(context, session, node);
        }
        else
        {
            return new LikeLoader(context, session, node);
        }
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Boolean>> arg0, LoaderResult<Boolean> isLiked)
    {
        if (progressView != null)
        {
            progressView.setVisibility(View.GONE);
        }
        if (isLiked.getData() == null)
        {
            OdsLog.ex(TAG, isLiked.getException());
            MessengerManager.showToast(context, R.string.error_retrieve_likes);
        }
        else if (isLiked.getData())
        {
            likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like));
            AccessibilityHelper.addContentDescription(likeButton, R.string.unlike);
        }
        else
        {
            likeButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_unlike));
            AccessibilityHelper.addContentDescription(likeButton, R.string.like);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Boolean>> arg0)
    {

    }

    public void setImageButton(ImageView mi)
    {
        this.likeButton = mi;
    }

    public void setProgressView(View v)
    {
        this.progressView = v;
    }

    private static final String IS_CREATE = "isCreate";

    public void execute(boolean isCreate)
    {
        int id = (isCreate) ? LikeLoader.ID : IsLikedLoader.ID;

        if (progressView != null)
        {
            progressView.setVisibility(View.VISIBLE);
        }

        Bundle b = new Bundle();
        b.putBoolean(IS_CREATE, isCreate);

        if (getLoaderManager().getLoader(id) == null)
        {
            getLoaderManager().initLoader(id, b, this);
        }
        else
        {
            getLoaderManager().restartLoader(id, b, this);
        }
        getLoaderManager().getLoader(id).forceLoad();

    }
}
