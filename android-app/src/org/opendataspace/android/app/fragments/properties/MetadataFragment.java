/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.opendataspace.android.app.fragments.properties;

import java.util.ArrayList;
import java.util.List;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.cmisapi.constants.ContentModel;
import org.opendataspace.android.cmisapi.model.Folder;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.commonui.fragments.BaseFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MetadataFragment extends BaseFragment
{
    public static final String TAG = "MetadataFragment";

    public static final String ARGUMENT_NODE = "node";

    public static final String ARGUMENT_NODE_PARENT = "nodeParent";

    protected Folder parentNode;

    protected Node node;

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static Bundle createBundleArgs(Node node, Folder parentNode)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        args.putSerializable(ARGUMENT_NODE_PARENT, parentNode);
        return args;
    }

    public static MetadataFragment newInstance(Node node, Folder parentNode)
    {
        MetadataFragment bf = new MetadataFragment();
        bf.setArguments(createBundleArgs(node, parentNode));
        return bf;
    }

    public static MetadataFragment newInstance(Node n)
    {
        MetadataFragment bf = new MetadataFragment();
        bf.setArguments(createBundleArgs(n));
        return bf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        if (container != null)
        {
            container.setVisibility(View.VISIBLE);
        }
        alfSession = SessionUtils.getSession(getActivity());

        node = (Node) getArguments().get(ARGUMENT_NODE);
        parentNode = (Folder) getArguments().get(ARGUMENT_NODE_PARENT);

        ScrollView sv = new ScrollView(getActivity());
        sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        sv.setPadding(5, 5, 5, 0);
        
        if (alfSession == null) { return sv; }

        LinearLayout v = new LinearLayout(getActivity());
        v.setOrientation(LinearLayout.VERTICAL);
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        v.setGravity(Gravity.CENTER);

        ViewGroup grouprootview = (ViewGroup) inflater.inflate(R.layout.app_properties, v);
        grouprootview = (ViewGroup) grouprootview.findViewById(R.id.metadata);

        // Description
        TextView tv = (TextView) v.findViewById(R.id.description);
        List<String> filter = new ArrayList<String>();
        if (node.getDescription() != null && node.getDescription().length() > 0)
        {
            v.findViewById(R.id.description_group).setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.description_title)).setText(R.string.metadata_general);
            tv.setText(node.getDescription());
            ((TextView) v.findViewById(R.id.prop_name_value)).setText(node.getName());
            filter.add(ContentModel.PROP_NAME);
        }
        else
        {
            v.findViewById(R.id.description_group).setVisibility(View.GONE);
        }

        sv.addView(v);

        return sv;
    }

    protected void addPathProperty(ViewGroup generalGroup, LayoutInflater inflater)
    {
        // Add Path
        if (parentNode != null || node.isFolder())
        {
            Node tmpNode = (parentNode != null) ? parentNode : node;
            View vr = inflater.inflate(R.layout.sdk_property_row, null);
            TextView tv = (TextView) vr.findViewById(R.id.propertyName);
            tv.setText(R.string.metadata_prop_path);
            tv = (TextView) vr.findViewById(R.id.propertyValue);
            tv.setText((String) tmpNode.getPropertyValue(PropertyIds.PATH));
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv.setTag(tmpNode);
            tv.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (getActivity() instanceof BaseActivity)
                    {
                        ((BaseActivity) getActivity()).addBrowserFragment((String) ((Folder) v.getTag())
                                .getPropertyValue(PropertyIds.PATH));
                    }
                }
            });
            generalGroup.addView(vr);
        }
    }

    public Folder getParentNode()
    {
        return parentNode;
    }
}
