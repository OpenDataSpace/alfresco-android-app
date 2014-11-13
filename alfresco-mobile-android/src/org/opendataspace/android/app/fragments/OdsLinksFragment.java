package org.opendataspace.android.app.fragments;

import java.util.ArrayList;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.exception.CloudExceptionUtils;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.fragments.BaseListFragment;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.links.OdsLinksAdapter;
import org.opendataspace.android.app.links.OdsLinksLoader;
import org.opendataspace.android.app.operations.OdsUpdateLinkContext;
import org.opendataspace.android.ui.logging.OdsLog;

import com.j256.ormlite.dao.CloseableIterator;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class OdsLinksFragment extends BaseListFragment implements
        LoaderCallbacks<LoaderResult<CloseableIterator<OdsLink>>>, RefreshFragment
{
    public static final String TAG = "OdsLinksFragment";
    public static final String ARGUMENT_NODE = "node";

    private class LinksReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            OdsLog.d(TAG, intent.getAction());

            if (adapter == null)
            {
                return;
            }

            if (intent.getExtras() != null)
            {
                Bundle b = intent.getExtras().getParcelable(IntentIntegrator.EXTRA_DATA);

                if (b != null
                        && b.getSerializable(IntentIntegrator.EXTRA_CONFIGURATION) instanceof OdsUpdateLinkContext)
                {
                    refresh();
                }
            }
        }
    }

    private Node node;
    private LinksReceiver receiver;

    public static Bundle createBundleArgs(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    public static BaseFragment newInstance(Node n)
    {
        OdsLinksFragment bf = new OdsLinksFragment();
        Bundle b = createBundleArgs(n);
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

        if (getArguments() != null && getArguments().containsKey(ARGUMENT_NODE))
        {
            node = bundle.getParcelable(ARGUMENT_NODE);
        }
    }

    @Override
    public void onResume()
    {
        if (!DisplayUtils.hasCentralPane(getActivity()))
        {
            UIUtils.displayTitle(getActivity(), getString(R.string.document_links_header));
        }

        super.onResume();

        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_UPDATE_LINK_COMPLETED);
            receiver = new LinksReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setRetainInstance(false);
        View v = inflater.inflate(R.layout.ods_links, container, false);

        init(v, R.string.empty_links);

        lv.setDivider(null);
        lv.setSelector(android.R.color.transparent);
        lv.setCacheColorHint(android.R.color.transparent);
        return v;
    }

    public static void editLink(Node node, OdsLink val, FragmentManager fm)
    {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(OdsLinkDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        if (node != null)
        {
            val.setNodeId(node.getIdentifier());
        }

        OdsLinkDialogFragment.newInstance(val).show(ft, OdsLinkDialogFragment.TAG);
    }

    @Override
    public void onLoaderException(Exception e)
    {
        setListShown(true);
        CloudExceptionUtils.handleCloudException(getActivity(), e, false);
    }

    @Override
    public Loader<LoaderResult<CloseableIterator<OdsLink>>> onCreateLoader(int id, Bundle ba)
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
    public void onLoadFinished(Loader<LoaderResult<CloseableIterator<OdsLink>>> loader,
            LoaderResult<CloseableIterator<OdsLink>> results)
    {
        if (checkException(results))
        {
            onLoaderException(results.getException());
        }
        else
        {
            adapter = new OdsLinksAdapter(this, R.layout.sdk_list_row, new ArrayList<OdsLink>());

            while (results.getData().hasNext())
            {
                ((OdsLinksAdapter) adapter).add(results.getData().next());
            }

            lv.setAdapter(adapter);
            results.getData().closeQuietly();
            setListShown(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<CloseableIterator<OdsLink>>> loader)
    {
        // nothing
    }

    public void getMenu(Menu menu)
    {
        MenuItem mi;

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_CREATE_LINK, Menu.FIRST + MenuActionItem.MENU_CREATE_LINK,
                R.string.links_add);
        mi.setIcon(R.drawable.ic_add_link);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        mi = menu.add(Menu.NONE, MenuActionItem.MENU_REFRESH, Menu.FIRST + MenuActionItem.MENU_REFRESH,
                R.string.refresh);
        mi.setIcon(R.drawable.ic_refresh);
        mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public void refresh()
    {
        refresh(loaderId, callback);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        //editLink(node, (OdsLink) l.getItemAtPosition(position), getFragmentManager());
    }

    public Node getNode()
    {
        return node;
    }

    @Override
    public void onPause()
    {
        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
            receiver = null;
        }

        super.onPause();
    }
}
