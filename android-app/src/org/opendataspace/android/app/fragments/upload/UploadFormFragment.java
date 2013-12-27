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
package org.opendataspace.android.app.fragments.upload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendataspace.android.app.ApplicationManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.accounts.AccountManager;
import org.opendataspace.android.app.accounts.AccountSchema;
import org.opendataspace.android.app.accounts.fragment.AccountCursorAdapter;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.activity.HomeScreenActivity;
import org.opendataspace.android.app.activity.PublicDispatcherActivity;
import org.opendataspace.android.app.exception.AlfrescoAppException;
import org.opendataspace.android.app.fragments.fileexplorer.FileExplorerAdapter;
import org.opendataspace.android.app.intent.IntentIntegrator;
import org.opendataspace.android.app.manager.ActionManager;
import org.opendataspace.android.app.manager.StorageManager;
import org.opendataspace.android.app.security.DataProtectionManager;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.app.utils.AndroidVersion;
import org.opendataspace.android.app.utils.UIUtils;
import org.opendataspace.android.app.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * Display the form to choose account and import folder.
 * 
 * @author Jean Marie Pascal
 */
public class UploadFormFragment extends Fragment implements LoaderCallbacks<Cursor>
{

    public static final String TAG = "ImportFormFragment";

    private Cursor selectedAccountCursor;

    private String fileName;

    private File file;

    private View rootView;

    private Integer folderImportId;

    private int importFolderIndex;

    /** Principal ListView of the fragment */
    protected ListView lv;

    protected ArrayAdapter<?> adapter;

    protected CursorAdapter cursorAdapter;

    protected int selectedPosition;

    protected List<File> files = new ArrayList<File>();

    private Spinner spinnerDoc;

    private Spinner spinnerAccount;

    public static UploadFormFragment newInstance(Bundle b)
    {
        UploadFormFragment fr = new UploadFormFragment();
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
            initDocumentList(rootView);
        }
        else
        {
            initiDocumentSpinner(rootView);
        }

        spinnerAccount = (Spinner) rootView.findViewById(R.id.accounts_spinner);
        spinnerAccount.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
            {
                selectedAccountCursor = (Cursor) parent.getItemAtPosition(pos);
                updateImportListFolders();
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


    @SuppressLint("NewApi")
    @Override
    public void onStart()
    {
        super.onStart();

        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (files != null)
        {
            files.clear();
        }

        try
        {
            if (Intent.ACTION_SEND_MULTIPLE.equals(action))
            {
                ClipData clipdata = intent.getClipData();
                if (clipdata != null && clipdata.getItemCount() > 1)
                {
                    Item item = null;
                    for (int i = 0; i < clipdata.getItemCount(); i++)
                    {
                        item = clipdata.getItemAt(i);
                        Uri uri = item.getUri();
                        if (uri != null)
                        {
                            retrieveIntentInfo(uri);
                        }
                        else
                        {
                            String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss").format(new Date());
                            File localParentFolder = org.alfresco.mobile.android.ui.manager.StorageManager.getCacheDir(getActivity(), "AlfrescoMobile/import");
                            File f = createFile(localParentFolder, timeStamp + ".txt", item.getText().toString());
                            if (f.exists())
                            {
                                retrieveIntentInfo(Uri.fromFile(f));
                            }
                        }
                        if (!files.contains(file))
                        {
                            files.add(file);
                        }
                    }
                }
            }
            else
            {
                // Manage only one clip data. If multiple we ignore.
                if (AndroidVersion.isJBOrAbove() && (!Intent.ACTION_SEND.equals(action) || type == null))
                {
                    ClipData clipdata = intent.getClipData();
                    if (clipdata != null && clipdata.getItemCount() == 1 && clipdata.getItemAt(0) != null
                            && (clipdata.getItemAt(0).getText() != null || clipdata.getItemAt(0).getUri() != null))
                    {
                        Item item = clipdata.getItemAt(0);
                        Uri uri = item.getUri();
                        if (uri != null)
                        {
                            retrieveIntentInfo(uri);
                        }
                        else
                        {
                            String timeStamp = new SimpleDateFormat("yyyyddMM_HHmmss").format(new Date());
                            File localParentFolder = org.alfresco.mobile.android.ui.manager.StorageManager.getCacheDir(getActivity(), "AlfrescoMobile/import");
                            File f = createFile(localParentFolder, timeStamp + ".txt", item.getText().toString());
                            if (f.exists())
                            {
                                retrieveIntentInfo(Uri.fromFile(f));
                            }
                        }
                    }
                }

                if (file == null && Intent.ACTION_SEND.equals(action) && type != null)
                {
                    Uri uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    retrieveIntentInfo(uri);
                }
                else if (action == null && intent.getData() != null)
                {
                    retrieveIntentInfo(intent.getData());
                }
                else if (file == null || fileName == null)
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.import_unsupported_intent));
                    getActivity().finish();
                    return;
                }
                if (!files.contains(file))
                {
                    files.add(file);
                }
            }
        }
        catch (AlfrescoAppException e)
        {
            org.opendataspace.android.app.manager.ActionManager.actionDisplayError(this, e);
            getActivity().finish();
            return;
        }

        if (adapter == null && files != null)
        {
            adapter = new FileExplorerAdapter(this, R.layout.app_list_progress_row, files);
            if (lv != null)
            {
                lv.setAdapter(adapter);
            }
            else if (spinnerDoc != null)
            {
                spinnerDoc.setAdapter(adapter);
            }
        }

        Button b = (Button) rootView.findViewById(R.id.cancel);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getActivity().finish();
            }
        });

        b = (Button) rootView.findViewById(R.id.ok);
        b.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                next();
            }
        });

        updateImportListFolders();
        //refreshImportFolder();

    }

    // ///////////////////////////////////////////
    // ACCOUNT CURSOR
    // ///////////////////////////////////////////
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        final CursorLoader c =  new CursorLoader(getActivity(), AccountManager.CONTENT_URI, AccountManager.COLUMN_ALL,
                AccountSchema.COLUMN_ACTIVATION + " IS NULL OR " + AccountSchema.COLUMN_ACTIVATION + "= ''", null, null);

        return c;
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

        final Cursor c = cursorAdapter.getCursor();
        if(c == null)
            return ;

        if(c.getCount() <= 1)
            return ;

        long _id = ApplicationManager.getInstance(BaseActivity.getCurrentContext()).getCurrentAccount().getId();
        for (int i = 0; i < c.getCount(); i++)
        {
            c.moveToPosition(i);
            if (c.getLong(AccountSchema.COLUMN_ID_ID) == _id)
            {
                int accountIndex = c.getPosition();
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
    private void initiDocumentSpinner(View v)
    {
        spinnerDoc = (Spinner) v.findViewById(R.id.import_documents_spinner);
        if (adapter != null)
        {
            spinnerDoc.setAdapter(adapter);
        }
    }

    private void initDocumentList(View v)
    {
        lv = (ListView) v.findViewById(R.id.listView);

        if (adapter != null)
        {
            lv.setAdapter(adapter);
            lv.setSelection(selectedPosition);
        }
    }

    private void retrieveIntentInfo(Uri uri)
    {
        if (uri == null) { throw new AlfrescoAppException(getString(R.string.import_unsupported_intent), true); }

        String tmpPath = org.alfresco.mobile.android.ui.manager.ActionManager.getPath(getActivity(), uri);
        if (tmpPath != null)
        {
            file = new File(tmpPath);

            if (file == null || !file.exists()) { throw new AlfrescoAppException(
                    getString(R.string.error_unknown_filepath), true); }
            fileName = file.getName();

            if (getActivity() instanceof PublicDispatcherActivity)
            {
                files.add(file);
                ((PublicDispatcherActivity) getActivity()).setUploadFile(files);
            }
        }
        else
        {
            // Error case : Unable to find the file path associated
            // to user pick.
            // Sample : Picasa image case
            throw new AlfrescoAppException(getString(R.string.error_unknown_filepath), true);
        }
    }

    private void refreshImportFolder(List<Integer> list)
    {
        Spinner spinner = (Spinner) rootView.findViewById(R.id.import_folder_spinner);
        UploadFolderAdapter upLoadadapter = new UploadFolderAdapter(getActivity(), R.layout.sdk_list_row,list);
        //IMPORT_FOLDER_LIST);
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
    private static final List<Integer> IMPORT_FOLDER_LIST = new ArrayList<Integer>(3)
    {
        {
            add(R.string.menu_browse_root);
            // add(R.string.menu_downloads);
            //add(R.string.menu_browse_sites);
            //add(R.string.menu_favorites_folder);
        }
    };

    @SuppressWarnings("serial")
    private static final List<Integer> IMPORT_ODS_FOLDER_LIST = new ArrayList<Integer>(3)
    {
        {
            add(R.string.menu_browse_root);
            add(R.string.menu_browse_shared);
            add(R.string.menu_browse_global);
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
        case R.string.menu_browse_global:
        case R.string.menu_browse_shared:

            if (getActivity() instanceof PublicDispatcherActivity)
            {
                ((PublicDispatcherActivity) getActivity()).setUploadFolder(folderImportId);
            }

            AlfrescoSession session = ApplicationManager.getInstance(getActivity()).getSession(tmpAccount.getId());

            // Try to use Session used by the application
            if (session != null)
            {
                ((BaseActivity) getActivity()).setCurrentAccount(tmpAccount);
                ((BaseActivity) getActivity()).setRenditionManager(null);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED).putExtra(
                                IntentIntegrator.EXTRA_ACCOUNT_ID, tmpAccount.getId()));
                return;
            }

            // Session is not used by the application so create one.
            ActionManager.loadAccount(getActivity(), tmpAccount);

            break;
        case R.string.menu_downloads:
            if (files.size() == 1)
            {
                UploadLocalDialogFragment fr = UploadLocalDialogFragment.newInstance(tmpAccount, file);
                fr.show(getActivity().getFragmentManager(), UploadLocalDialogFragment.TAG);
            }
            else
            {
                File folderStorage = StorageManager.getDownloadFolder(getActivity(), tmpAccount);
                DataProtectionManager.getInstance(getActivity()).copyAndEncrypt(tmpAccount, files, folderStorage);
                getActivity().finish();
            }
            break;
        default:
            break;
        }
    }

    // TODO Move to IOUtils
    private File createFile(File localParentFolder, String filename, String data)
    {
        File outputFile = null;
        Writer writer = null;
        try
        {
            if (!localParentFolder.isDirectory())
            {
                localParentFolder.mkdir();
            }
            outputFile = new File(localParentFolder, filename);
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(data);
            writer.close();
        }
        catch (IOException e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
        return outputFile;
    }

    private void updateImportListFolders(){

        Context c = BaseActivity.getCurrentContext();
        AlfrescoSession session = null;

        if(c == null){
            refreshImportFolder(IMPORT_FOLDER_LIST);
            return;
        }
        if(selectedAccountCursor != null){
            long accountId = selectedAccountCursor.getLong(AccountSchema.COLUMN_ID_ID);
            session = ApplicationManager.getInstance(c).getSession(accountId);
        }else{
            session = ApplicationManager.getInstance(c).getCurrentSession();
        }

        if (session instanceof OdsRepositorySession)
        {
            refreshImportFolder(IMPORT_ODS_FOLDER_LIST);
            return;
        }

        refreshImportFolder(IMPORT_FOLDER_LIST);
    }
}