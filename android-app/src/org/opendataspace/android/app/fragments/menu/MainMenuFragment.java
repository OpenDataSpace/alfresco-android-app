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
package org.opendataspace.android.app.fragments.menu;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.accounts.AccountManager;
import org.opendataspace.android.app.accounts.AccountSchema;
import org.opendataspace.android.app.accounts.fragment.AccountCursorAdapter;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.activity.MainActivity;
import org.opendataspace.android.app.fragments.DisplayUtils;
import org.opendataspace.android.app.fragments.about.AboutFragment;
import org.opendataspace.android.app.intent.IntentIntegrator;
import org.opendataspace.android.app.preferences.AccountsPreferences;
import org.opendataspace.android.app.preferences.GeneralPreferences;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.app.utils.UIUtils;
import org.opendataspace.android.app.utils.thirdparty.LocalBroadcastManager;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;

public class MainMenuFragment extends Fragment implements LoaderCallbacks<Cursor>, OnItemSelectedListener
{
    private AccountCursorAdapter cursorAdapter;

    private Spinner spinnerAccount;

    private Cursor accountCursor;

    private int accountIndex;

    private MainMenuReceiver receiver;

    //private Button menuFavorites;

    //private Button menuSlidingFavorites;

    private Button menuShared;

    private Button menuGlobal;

    public static final String TAG = "MainMenuFragment";

    public static final String SLIDING_TAG = "SlidingMenuFragment";

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.app_main_menu, container, false);

        spinnerAccount = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(this);
        /*
        menuFavorites = (Button) rootView.findViewById(R.id.menu_favorites);

        if (SLIDING_TAG.equals(getTag()))
        {
            menuSlidingFavorites = (Button) rootView.findViewById(R.id.menu_favorites);
        }
         */

        menuShared = (Button) rootView.findViewById(R.id.menu_browse_shared);
        menuGlobal = (Button) rootView.findViewById(R.id.menu_browse_global);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (accountCursor != null)
        {
            accountCursor.close();
            accountCursor = null;
        }
        cursorAdapter = new AccountCursorAdapter(getActivity(), null, R.layout.app_account_list_row, null);
        spinnerAccount.setAdapter(cursorAdapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        DisplayUtils.hideLeftTitlePane(getActivity());

        if (isAdded() && TAG.equals(getTag())
                && getActivity().getFragmentManager().findFragmentByTag(GeneralPreferences.TAG) == null
                && getActivity().getFragmentManager().findFragmentByTag(AboutFragment.TAG) == null)
        {
            ((MainActivity) getActivity()).clearScreen();
        }

        UIUtils.displayTitle(getActivity(), R.string.app_name);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        if (receiver == null)
        {
            IntentFilter intentFilter = new IntentFilter(IntentIntegrator.ACTION_SYNCHRO_COMPLETED);
            receiver = new MainMenuReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, intentFilter);
        }

        //displayFavoriteStatut();
        updateFolderAccess();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (!isVisible() && TAG.equals(getTag()))
        {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (receiver != null)
        {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DPUBLIC
    // ///////////////////////////////////////////////////////////////////////////
    public void refreshData()
    {
        refresh();
        //displayFavoriteStatut();
        updateFolderAccess();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // DROPDOWN EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
    {
        Cursor cursor = (Cursor) parentView.getItemAtPosition(position);
        int accountId = cursor.getInt(AccountSchema.COLUMN_ID_ID);

        switch (accountId)
        {
        case AccountCursorAdapter.NETWORK_ITEM:
            ((MainActivity) getActivity()).displayNetworks();
            hideSlidingMenu(false);
            break;
        case AccountCursorAdapter.MANAGE_ITEM:
            ((MainActivity) getActivity()).displayAccounts();
            hideSlidingMenu(false);
            break;

        default:
            Account currentAccount = SessionUtils.getAccount(getActivity());
            if (currentAccount != null && cursor.getCount() > 1
                    && currentAccount.getId() != cursor.getLong(AccountSchema.COLUMN_ID_ID))
            {
                hideSlidingMenu(true);

                // Request session loading for the selected account.
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT).putExtra(
                                IntentIntegrator.EXTRA_ACCOUNT_ID, cursor.getLong(AccountSchema.COLUMN_ID_ID)));

                // Update dropdown menu (eventual new items to display)
                cursorAdapter.swapCursor(AccountCursorAdapter.createMergeCursor(getActivity(), accountCursor));
            }
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
    }

    // ///////////////////////////////////////////////////////////////////////////
    // CALLBACKS
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(getActivity(), AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        accountCursor = cursor;
        cursorAdapter.changeCursor(AccountCursorAdapter.createMergeCursor(getActivity(), accountCursor));
        if (cursor.getCount() > 0)
        {
            refresh();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        cursorAdapter.changeCursor(null);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private void refresh()
    {
        if (accountCursor == null) { return; }
        if (accountCursor.isClosed()) { return; }

        Account currentAccount = SessionUtils.getAccount(getActivity());
        if (currentAccount == null)
        {
            currentAccount = AccountsPreferences.getDefaultAccount(getActivity());
        }

        if (currentAccount == null) { return; }

        for (int i = 0; i < accountCursor.getCount(); i++)
        {
            accountCursor.moveToPosition(i);
            if (accountCursor.getLong(AccountSchema.COLUMN_ID_ID) == currentAccount.getId())
            {
                accountIndex = accountCursor.getPosition();
                break;
            }
        }

        spinnerAccount.setSelection(accountIndex);
    }

    private void hideSlidingMenu(boolean goHome)
    {
        if (SLIDING_TAG.equals(getTag()))
        {
            ((MainActivity) getActivity()).toggleSlideMenu();
            if (goHome)
            {
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }
    /*
    public void displayFavoriteStatut()
    {
        Cursor statutCursor = null;
        Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_favorite);
        Drawable statut = null;

        try
        {
            Account acc = SessionUtils.getAccount(getActivity());
            Boolean hasSynchroActive = GeneralPreferences.hasActivateSync(getActivity(), acc);

            if (hasSynchroActive && acc != null)
            {
                statutCursor = getActivity().getContentResolver().query(
                        SynchroProvider.CONTENT_URI,
                        SynchroSchema.COLUMN_ALL,
                        SynchroProvider.getAccountFilter(acc) + " AND " + SynchroSchema.COLUMN_STATUS + " == "
                                + SyncOperation.STATUS_REQUEST_USER, null, null);
                if (statutCursor.getCount() > 0)
                {
                    statut = getActivity().getResources().getDrawable(R.drawable.ic_warning_light);
                }
                statutCursor.close();

                if (menuSlidingFavorites != null)
                {
                    menuSlidingFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
                }
                menuFavorites.setCompoundDrawablesWithIntrinsicBounds(icon, null, statut, null);
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            if (statutCursor != null)
            {
                statutCursor.close();
            }
        }
    }
     */
    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class MainMenuReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Log.d(TAG, intent.getAction());
            if (intent.getAction() == null) { return; }
            //displayFavoriteStatut();
            updateFolderAccess();
        }
    }

    public void updateFolderAccess()
    {
        Activity acc = getActivity();
        AlfrescoSession ses = acc instanceof BaseActivity ? ((BaseActivity) acc).getCurrentSession() : null;
        OdsRepositorySession ods = ses instanceof OdsRepositorySession ? (OdsRepositorySession) ses : null;
        boolean hasShared = ods != null && ods.getShared() != null;
        boolean hasGlobal = ods != null && ods.getGlobal() != null;

        menuShared.setVisibility(hasShared ? View.VISIBLE : View.GONE);
        menuGlobal.setVisibility(hasGlobal ? View.VISIBLE : View.GONE);
    }
}
