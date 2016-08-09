/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * <p/>
 * This file is part of Alfresco Mobile for Android.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.activity;

import android.accounts.AccountAuthenticatorResponse;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.fragment.AccountEditFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountOAuthFragment;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesSyncFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.operations.OperationsFragment;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.upload.UploadFormFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.fragments.OdsSelectFolderFragment;
import org.opendataspace.android.app.session.OdsRepoType;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.ui.logging.OdsLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible to manage public intent from 3rd party application. This activity is "open" to public Intent.
 *
 * @author Jean Marie Pascal
 */
public class PublicDispatcherActivity extends BaseActivity
{
    private static final String TAG = PublicDispatcherActivity.class.getName();
    private static final String ARG_FILES = "ods.files";

    /**
     * Define the type of importFolder.
     */
    private int uploadFolder;

    /**
     * Define the local file to upload
     */
    private List<File> uploadFiles;

    private boolean activateCheckPasscode = false;

    private PublicDispatcherActivityReceiver receiver;

    protected long requestedAccountId = -1;

    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;

    private Bundle mResultBundle = null;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        activateCheckPasscode = false;

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        android.view.WindowManager.LayoutParams params = getWindow().getAttributes();

        int[] values = UIUtils.getScreenDimension(this);
        int height = values[1];
        int width = values[0];

        params.height = (int) Math.round(height * 0.9);
        params.width = (int) Math.round(width * 0.8);

        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes(params);

        setContentView(R.layout.app_left_panel);

        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(android.accounts.AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null)
        {
            mAccountAuthenticatorResponse.onRequestContinued();
        }

        String action = getIntent().getAction();
        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) &&
                getFragment(UploadFormFragment.TAG) == null)
        {
            Fragment f = new UploadFormFragment();
            FragmentDisplayer
                    .replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), UploadFormFragment.TAG, false,
                            false);
            return;
        }

        if (IntentIntegrator.ACTION_SYNCHRO_DISPLAY.equals(action))
        {
            Fragment f = FavoritesSyncFragment.newInstance(FavoritesSyncFragment.MODE_PROGRESS);
            FragmentDisplayer
                    .replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), OperationsFragment.TAG, false,
                            false);
            return;
        }

        if (IntentIntegrator.ACTION_PICK_FILE.equals(action))
        {
            if (getIntent().hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
            {
                currentAccount = AccountManager
                        .retrieveAccount(this, getIntent().getLongExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, 1));
            }

            File f = Environment.getExternalStorageDirectory();
            if (getIntent().hasExtra(IntentIntegrator.EXTRA_FOLDER))
            {
                f = (File) getIntent().getExtras().getSerializable(IntentIntegrator.EXTRA_FOLDER);
                Fragment fragment = FileExplorerFragment.newInstance(f, ListingModeFragment.MODE_PICK, true, 1);
                FragmentDisplayer
                        .replaceFragment(this, fragment, DisplayUtils.getLeftFragmentId(this), FileExplorerFragment.TAG,
                                false, false);
            }
        }

        if (IntentIntegrator.ACTION_PICK_FOLDER.equals(action))
        {
            Fragment f = new OdsSelectFolderFragment();
            FragmentDisplayer
                    .replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), OdsSelectFolderFragment.TAG, false,
                            false);
            return;
        }

        if (IntentIntegrator.ACTION_CREATE_ACCOUNT.equals(action))
        {
            Fragment f = new AccountEditFragment();
            FragmentDisplayer
                    .replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), AccountEditFragment.TAG, false,
                            false);
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE)
        {
            if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
            else
            {
                activateCheckPasscode = true;
            }
        }
    }

    @Override
    protected void onStart()
    {
        getActionBar().setDisplayHomeAsUpEnabled(mAccountAuthenticatorResponse == null);
        if (receiver == null)
        {
            receiver = new PublicDispatcherActivityReceiver();
            IntentFilter filters = new IntentFilter(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
            filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT);
            filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
            broadcastManager.registerReceiver(receiver, filters);
        }

        super.onStart();
        PassCodeActivity.requestUserPasscode(this);
        activateCheckPasscode = PasscodePreferences.hasPasscodeEnable(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!activateCheckPasscode)
        {
            PasscodePreferences.updateLastActivityDisplay(this);
        }
    }

    @Override
    protected void onStop()
    {
        if (receiver != null)
        {
            broadcastManager.unregisterReceiver(receiver);
            receiver = null;
        }
        super.onStop();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UI Public Method
    // ///////////////////////////////////////////////////////////////////////////
    public void doCancel(View v)
    {
        finish();
    }

    public void validateAction(View v)
    {
        if (IntentIntegrator.ACTION_PICK_FOLDER.equals(getIntent().getAction()))
        {
            ChildrenBrowserFragment f = (ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG);
            Intent intent = new Intent(IntentIntegrator.ACTION_PICK_FOLDER);
            intent.putExtra(IntentIntegrator.EXTRA_FOLDER_ID, f.getImportFolder().getIdentifier());
            intent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, SessionUtils.getAccount(this).getId());
            setResult(PublicIntent.REQUESTCODE_FOLDERPICKER, intent);
            finish();
            return;
        }

        ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFiles(uploadFiles);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MENU
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if (isVisible(ChildrenBrowserFragment.TAG))
        {
            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getMenu(menu);
            return true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case MenuActionItem.MENU_CREATE_FOLDER:
            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFolder();
            return true;
        case android.R.id.home:
            if (getIntent() != null && IntentIntegrator.ACTION_PICK_FILE.equals(getIntent().getAction()))
            {
                finish();
            }
            else
            {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public void setUploadFolder(int uploadFolderType)
    {
        this.uploadFolder = uploadFolderType;
    }

    public void setUploadFile(List<File> localFile)
    {
        this.uploadFiles = localFile;
    }

    // ////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////
    private class PublicDispatcherActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            OdsLog.d(TAG, intent.getAction());

            Activity activity = PublicDispatcherActivity.this;

            // During the session creation, display a waiting dialog.
            if (IntentIntegrator.ACTION_LOAD_ACCOUNT.equals(intent.getAction()))
            {
                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    return;
                }
                requestedAccountId = intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
                displayWaitingDialog();
                return;
            }

            // If the sesison is available, display the view associated (repository, sites, downloads, favorites).
            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    return;
                }
                long accountId = intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
                if (requestedAccountId != -1 && requestedAccountId != accountId)
                {
                    return;
                }
                requestedAccountId = -1;

                setProgressBarIndeterminateVisibility(false);

                if (getCurrentSession() instanceof RepositorySession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, false);
                }
                else if (getCurrentSession() instanceof CloudSession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, true);
                }

                // Remove OAuthFragment if one
                if (getFragment(AccountOAuthFragment.TAG) != null)
                {
                    getFragmentManager()
                            .popBackStack(AccountOAuthFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                removeWaitingDialog();

                // Upload process : Display the view where the user wants to upload files.
                BaseFragment frag = null;
                if (getCurrentSession() != null && uploadFolder == R.string.menu_browse_sites)
                {
                    frag = BrowserSitesFragment.newInstance();
                    FragmentDisplayer.replaceFragment(activity, frag, DisplayUtils.getLeftFragmentId(activity),
                            BrowserSitesFragment.TAG, true);
                }
                else if (getCurrentSession() != null && uploadFolder == R.string.menu_favorites_folder)
                {
                    frag = FavoritesFragment.newInstance(FavoritesFragment.MODE_FOLDERS);
                    FragmentDisplayer.replaceFragment(activity, frag, DisplayUtils.getLeftFragmentId(activity),
                            FavoritesFragment.TAG, true);
                }
                else if (uploadFolder == R.string.menu_browse_shared || uploadFolder == R.string.menu_browse_global ||
                        uploadFolder == R.string.menu_browse_root)
                {
                    navigateOdsRepo();
                }
            }
        }
    }

    private void navigateOdsRepo()
    {
        AlfrescoSession ses = getCurrentSession();

        if (ses == null)
        {
            return;
        }

        if (!(ses instanceof OdsRepositorySession))
        {
            addNavigationFragment(ses.getRootFolder());
            return;
        }

        OdsRepositorySession ods = (OdsRepositorySession) ses;

        if (uploadFolder == R.string.menu_browse_shared)
        {
            addNavigationFragment(ods.getByType(OdsRepoType.SHARED).getRootFolder());
        }
        else if (uploadFolder == R.string.menu_browse_global)
        {
            addNavigationFragment(ods.getByType(OdsRepoType.GLOBAL).getRootFolder());
        }
        else
        {
            addNavigationFragment(ods.getByType(OdsRepoType.DEFAULT).getRootFolder());
        }
    }

    public final void setAccountAuthenticatorResult(Bundle result)
    {
        mResultBundle = result;
    }

    public void finish()
    {
        if (mAccountAuthenticatorResponse != null)
        {
            if (mResultBundle != null)
            {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            }
            else
            {
                mAccountAuthenticatorResponse.onError(android.accounts.AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }

        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        if (uploadFiles != null && !uploadFiles.isEmpty())
        {
            final ArrayList<String> files = new ArrayList<String>();

            for (File cur : uploadFiles)
            {
                files.add(cur.getAbsolutePath());
            }

            outState.putStringArrayList(ARG_FILES, files);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null)
        {
            return;
        }

        final ArrayList<String> files = savedInstanceState.getStringArrayList(ARG_FILES);

        if (files != null && !files.isEmpty())
        {
            uploadFiles = new ArrayList<File>();

            for (String cur : files)
            {
                if (!TextUtils.isEmpty(cur))
                {
                    uploadFiles.add(new File(cur));
                }
            }
        }
    }
}
