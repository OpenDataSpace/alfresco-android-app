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
package org.opendataspace.android.commonui.site;

import java.util.List;

import org.opendataspace.android.cmisapi.model.Site;
import org.opendataspace.android.commonui.fragments.BaseListAdapter;
import org.opendataspace.android.commonui.utils.GenericViewHolder;
import org.opendataspace.android.commonui.R;

import android.app.Activity;

/**
 * Provides access to sites and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 *
 */
public class SiteAdapter extends BaseListAdapter<Site, GenericViewHolder>
{

    public SiteAdapter(Activity context, int textViewResourceId, List<Site> listItems)
    {
        super(context, textViewResourceId, listItems);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, Site item)
    {
        vh.topText.setText(item.getTitle());
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, Site item)
    {
        vh.bottomText.setText(item.getDescription());
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, Site item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mime_site));
    }
}
