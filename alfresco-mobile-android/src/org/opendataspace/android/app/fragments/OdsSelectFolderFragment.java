package org.opendataspace.android.app.fragments;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Spinner;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.AccountSchema;
import org.alfresco.mobile.android.application.accounts.fragment.AccountCursorAdapter;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.upload.UploadFolderAdapter;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.session.OdsRepositorySession;

import java.util.ArrayList;
import java.util.List;

public class OdsSelectFolderFragment extends Fragment implements LoaderCallbacks<Cursor>
{
    public static final String TAG = "SelectFolderFragment";

    private View rootView;

    private Spinner spinnerAccount;

    private Cursor selectedAccountCursor;

    protected CursorAdapter cursorAdapter;

    private Integer folderImportId;

    private int importFolderIndex;

    public static OdsSelectFolderFragment newInstance(Bundle b)
    {
        OdsSelectFolderFragment fr = new OdsSelectFolderFragment();
        fr.setArguments(b);
        return fr;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        UIUtils.displayTitle(getActivity(), R.string.import_document_title);

        rootView = inflater.inflate(R.layout.app_import, container, false);
        if (rootView.findViewById(R.id.listView) != null)
        {
            rootView.findViewById(R.id.listView).setVisibility(View.GONE);
        }
        else
        {
            rootView.findViewById(R.id.import_documents_spinner).setVisibility(View.GONE);
        }

        rootView.findViewById(R.id.import_documents_header).setVisibility(View.GONE);

        spinnerAccount = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                selectedAccountCursor = (Cursor) parent.getItemAtPosition(pos);
                refreshImportFolder();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // Do nothing
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        cursorAdapter = new AccountCursorAdapter(getActivity(), null, R.layout.sdk_list_row, null);
        spinnerAccount.setAdapter(cursorAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Button b = UIUtils.initCancel(rootView, R.string.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        b = UIUtils.initValidation(rootView, R.string.next);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        refreshImportFolder();
    }

    // ///////////////////////////////////////////
    // ACCOUNT CURSOR
    // ///////////////////////////////////////////

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
    {
        return new CursorLoader(getActivity(), AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL,
                AccountSchema.COLUMN_ACTIVATION + " IS NULL OR " + AccountSchema.COLUMN_ACTIVATION + "= ''", null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor)
    {
        if (cursor.getCount() == 0)
        {
            startActivityForResult(new Intent(getActivity(), HomeScreenActivity.class), 1);
            return;
        }

        cursorAdapter.changeCursor(cursor);

        Account acc = ApplicationManager.getInstance(getActivity()).getCurrentAccount();

        if (acc == null)
        {
            return;
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            if (cursor.getLong(AccountSchema.COLUMN_ID_ID) == acc.getId())
            {
                int accountIndex = cursor.getPosition();
                spinnerAccount.setSelection(accountIndex);
                break;
            }
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

    private void refreshImportFolder()
    {
        AlfrescoSession ses;
        int type = 0;

        if (selectedAccountCursor != null)
        {
            long accountId = selectedAccountCursor.getLong(AccountSchema.COLUMN_ID_ID);
            ses = ApplicationManager.getInstance(getActivity()).getSession(accountId);
            type = selectedAccountCursor.getInt(AccountSchema.COLUMN_REPOSITORY_TYPE_ID);
        }
        else
        {
            ses = ApplicationManager.getInstance(getActivity()).getCurrentSession();
        }

        boolean isOds = (ses != null && ses instanceof OdsRepositorySession) || type == Account.TYPE_ODS_CMIS;

        Spinner spinner = (Spinner) rootView.findViewById(R.id.import_folder_spinner);
        UploadFolderAdapter upLoadadapter = new UploadFolderAdapter(getActivity(), R.layout.sdk_list_row,
                isOds ? ODS_IMPORT_FOLDER_LIST : IMPORT_FOLDER_LIST);
        spinner.setAdapter(upLoadadapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                folderImportId = (Integer) parent.getItemAtPosition(pos);
                importFolderIndex = pos;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                // DO Nothing
            }
        });
        if (folderImportId == null)
        {
            importFolderIndex = 0;
        }
        spinner.setSelection(importFolderIndex);
    }

    @SuppressWarnings("serial")
    private static final List<Integer> IMPORT_FOLDER_LIST = new ArrayList<Integer>(1)
    {
        {
            //add(R.string.menu_downloads);
            //add(R.string.menu_browse_sites);
            //add(R.string.menu_favorites_folder);
            add(R.string.menu_browse_root);
        }
    };

    @SuppressWarnings("serial")
    private static final List<Integer> ODS_IMPORT_FOLDER_LIST = new ArrayList<Integer>(3)
    {
        {
            add(R.string.menu_browse_root);
            //add(R.string.menu_browse_shared);
            //add(R.string.menu_browse_global);
        }
    };

    private void next()
    {
        long accountId = selectedAccountCursor.getLong(AccountSchema.COLUMN_ID_ID);
        Account tmpAccount = AccountManager.retrieveAccount(getActivity(), accountId);

        switch (folderImportId)
        {
        case R.string.menu_browse_sites:
        case R.string.menu_browse_root:
        case R.string.menu_favorites_folder:
        case R.string.menu_browse_shared:
        case R.string.menu_browse_global:
            if (getActivity() instanceof PublicDispatcherActivity)
            {
                ((PublicDispatcherActivity) getActivity()).setUploadFolder(folderImportId);
            }

            AlfrescoSession session =
                    tmpAccount != null ? ApplicationManager.getInstance(getActivity()).getSession(tmpAccount.getId()) :
                            null;

            // Try to use Session used by the application
            if (session != null)
            {
                ((BaseActivity) getActivity()).setCurrentAccount(tmpAccount);
                ((BaseActivity) getActivity()).setRenditionManager(null);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED)
                                .putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, tmpAccount.getId()));
                return;
            }

            // Session is not used by the application so create one.
            ActionManager.loadAccount(getActivity(), tmpAccount);
            break;
        default:
            break;
        }
    }

}
