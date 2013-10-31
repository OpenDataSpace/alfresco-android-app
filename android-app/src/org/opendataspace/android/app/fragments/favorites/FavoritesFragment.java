/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.app.fragments.favorites;

import java.util.ArrayList;
import java.util.List;








import org.opendataspace.android.app.R;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.activity.MainActivity;
import org.opendataspace.android.app.fragments.DisplayUtils;
import org.opendataspace.android.app.fragments.FragmentDisplayer;
import org.opendataspace.android.app.fragments.ListingModeFragment;
import org.opendataspace.android.app.fragments.RefreshFragment;
import org.opendataspace.android.app.fragments.browser.NodeAdapter;
import org.opendataspace.android.app.fragments.menu.MenuActionItem;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.app.utils.UIUtils;
import org.opendataspace.android.asynchronous.FavoritesLoader;
import org.opendataspace.android.asynchronous.LoaderResult;
import org.opendataspace.android.cmisapi.model.Folder;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.model.PagingResult;
import org.opendataspace.android.cmisapi.model.impl.cloud.CloudFolderImpl;
import org.opendataspace.android.commonui.fragments.BaseListFragment;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

@SuppressWarnings("rawtypes")
public class FavoritesFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<PagingResult>>, RefreshFragment
{
    public static final String TAG = FavoritesFragment.class.getName();

    public static final int MODE_DOCUMENTS = FavoritesLoader.MODE_DOCUMENTS;

    public static final int MODE_FOLDERS = FavoritesLoader.MODE_FOLDERS;

    public static final int MODE_BOTH = FavoritesLoader.MODE_BOTH;
    
    private static final String PARAM_MODE = "FavoriteMode";

    private List<Node> selectedItems = new ArrayList<Node>(1);

    private int mode = MODE_DOCUMENTS; 

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public FavoritesFragment()
    {
        loaderId = FavoritesLoader.ID;
        callback = this;
        emptyListMessageId = R.string.empty_favorites;
    }

    public static FavoritesFragment newInstance(int mode)
    {
        FavoritesFragment bf = new FavoritesFragment();
        ListingContext lc = new ListingContext();
        lc.setMaxItems(50);
        Bundle b = createBundleArgs(lc, LOAD_MANUAL);
        b.putInt(PARAM_MODE, mode);
        bf.setArguments(b);
        return bf;
    }

    
    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        UIUtils.displayTitle(getActivity(), R.string.menu_favorites);
    }
    // ///////////////////////////////////////////////////////////////////////////
    // LOADER
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<LoaderResult<PagingResult>> onCreateLoader(int id, Bundle ba)
    {
        if (!hasmore)
        {
            setListShown(false);
        }

        // Case Init & case Reload
        bundle = (ba == null) ? getArguments() : ba;

        ListingContext lc = null, lcorigin = null;

        if (bundle != null)
        {
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
            mode  =  bundle.getInt(PARAM_MODE);
        }
        calculateSkipCount(lc);
        FavoritesLoader loader = new FavoritesLoader(getActivity(), alfSession, mode);
        loader.setListingContext(lc);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult>> arg0,
            LoaderResult<PagingResult> results)
    {
        if (adapter == null)
        {
            adapter = new NodeAdapter(getActivity(), R.layout.sdk_list_row, new ArrayList<Node>(0), selectedItems,
                    ListingModeFragment.MODE_LISTING);
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
        ((NodeAdapter) adapter).setActivateThumbnail(true);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult>> arg0)
    {
        // DO Nothing
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIST ACTIONS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        Node item = (Node) l.getItemAtPosition(position);

        Boolean hideDetails = false;
        if (!selectedItems.isEmpty())
        {
            hideDetails = selectedItems.get(0).equals(item);
            selectedItems.clear();
        }
        l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        l.setItemChecked(position, true);
        v.setSelected(true);

        if (DisplayUtils.hasCentralPane(getActivity()))
        {
            selectedItems.add(item);
        }

        if (hideDetails)
        {
            if (DisplayUtils.hasCentralPane(getActivity()))
            {
                FragmentDisplayer.removeFragment(getActivity(), DisplayUtils.getCentralFragmentId(getActivity()));
            }
            selectedItems.clear();
        }
        else
        {
            if (item.isFolder())
            {
                if (item instanceof CloudFolderImpl){
                    ((BaseActivity) getActivity()).addNavigationFragment((Folder)item);
                } else {
                    ((BaseActivity) getActivity()).addBrowserFragment((String) ((Folder) item).getPropertyValue(PropertyIds.PATH));
                }
            }
            else
            {
                // Show properties
                ((MainActivity) getActivity()).addPropertiesFragment(item.getIdentifier());
                DisplayUtils.switchSingleOrTwo(getActivity(), true);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////

    public static void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public void refresh()
    {
        refresh(loaderId, callback);
    }
}
