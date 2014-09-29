package org.opendataspace.android.app.links;

import java.util.List;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.opendataspace.android.app.R;

import android.content.Context;
import android.view.View;

public class OdsLinksAdapter extends BaseListAdapter<OdsLink, GenericViewHolder>
{
    public OdsLinksAdapter(Context context, AlfrescoSession alfSession, int textViewResourceId, List<OdsLink> objects)
    {
        super(context, textViewResourceId, objects);
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, OdsLink item)
    {
        vh.bottomText.setText(item.getUrl());
        vh.choose.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, OdsLink item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_all_sites_light));
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, OdsLink item)
    {
        vh.topText.setText(item.getName());
    }
}
