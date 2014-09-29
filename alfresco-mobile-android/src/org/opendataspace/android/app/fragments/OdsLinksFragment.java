package org.opendataspace.android.app.fragments;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.links.OdsLinksAdapter;
import org.opendataspace.android.app.links.OdsLinksLoader;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class OdsLinksFragment extends BaseListFragment implements LoaderCallbacks<LoaderResult<PagingResult<OdsLink>>>,
        RefreshFragment
{
    public static final String TAG = "OdsLinksFragment";
    private static final String ARGUMENT_FOLDER = "parentFolderNode";
    public static final String ARGUMENT_NODE = "node";

    private Node node;
    private ImageView bAdd;

    public static Bundle createBundleArgs(Node node, Folder parentNode)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        args.putParcelable(ARGUMENT_FOLDER, (Parcelable) parentNode);
        return args;
    }

    public static BaseFragment newInstance(Node n, Folder parentNode)
    {
        OdsLinksFragment bf = new OdsLinksFragment();
        Bundle b = createBundleArgs(n, parentNode);
        bf.setArguments(b);
        return bf;
    }

    public OdsLinksFragment()
    {
        loaderId = OdsLinksLoader.ID;
        emptyListMessageId = R.string.empty_links;
        callback = this;
    }

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
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.document_links_header));
        }

        if (!alfSession.getServiceRegistry().getDocumentFolderService().getPermissions(node).canEdit())
        {
            bAdd.setVisibility(View.GONE);
        }

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        View v = inflater.inflate(R.layout.ods_links, container, false);

        init(v, R.string.empty_links);

        bAdd = (ImageView) v.findViewById(R.id.action_addlink);

        bAdd.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addLink();
            }
        });

        lv.setDivider(null);
        lv.setSelector(android.R.color.transparent);
        lv.setCacheColorHint(android.R.color.transparent);
        return v;
    }

    private void addLink()
    {
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    @Override
    public Loader<LoaderResult<PagingResult<OdsLink>>> onCreateLoader(int id, Bundle ba)
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
            node = bundle.getParcelable(ARGUMENT_NODE);
            lcorigin = (ListingContext) bundle.getSerializable(ARGUMENT_LISTING);
            lc = copyListing(lcorigin);
            loadState = bundle.getInt(LOAD_STATE);
        }

        calculateSkipCount(lc);
        OdsLinksLoader loader = new OdsLinksLoader(getActivity(), alfSession, node);
        loader.setListingContext(lc);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<PagingResult<OdsLink>>> arg0,
            LoaderResult<PagingResult<OdsLink>> results)
    {
        if (adapter == null)
        {
            adapter = new OdsLinksAdapter(getActivity(), alfSession, R.layout.sdk_list_row,
                    new ArrayList<OdsLink>(0));
        }
        if (checkException(results))
        {
            onLoaderException(results.getException());
        } else
        {
            displayPagingData(results.getData(), loaderId, callback);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<PagingResult<OdsLink>>> arg0)
    {
        // nothing
    }

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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        // OdsLink item = (OdsLink) l.getItemAtPosition(position);
    }
}
