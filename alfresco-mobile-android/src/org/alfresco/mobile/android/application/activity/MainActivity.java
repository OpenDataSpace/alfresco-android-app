/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;

import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Process;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.model.Task;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.accounts.fragment.AccountDetailsFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountEditFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountOAuthFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountTypesFragment;
import org.alfresco.mobile.android.application.accounts.fragment.AccountsFragment;
import org.alfresco.mobile.android.application.accounts.networks.CloudNetworksFragment;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenCallback;
import org.alfresco.mobile.android.application.accounts.oauth.OAuthRefreshTokenLoader;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.configuration.ConfigurationManager;
import org.alfresco.mobile.android.application.fragments.DisplayUtils;
import org.alfresco.mobile.android.application.fragments.FragmentDisplayer;
import org.alfresco.mobile.android.application.fragments.ListingModeFragment;
import org.alfresco.mobile.android.application.fragments.RefreshFragment;
import org.alfresco.mobile.android.application.fragments.about.AboutFragment;
import org.alfresco.mobile.android.application.fragments.activities.ActivitiesFragment;
import org.alfresco.mobile.android.application.fragments.browser.ChildrenBrowserFragment;
import org.alfresco.mobile.android.application.fragments.create.DocumentTypesDialogFragment;
import org.alfresco.mobile.android.application.fragments.favorites.FavoritesSyncFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerMenuFragment;
import org.alfresco.mobile.android.application.fragments.fileexplorer.LibraryFragment;
import org.alfresco.mobile.android.application.fragments.menu.MainMenuFragment;
import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.fragments.person.PersonProfileFragment;
import org.alfresco.mobile.android.application.fragments.properties.DetailsFragment;
import org.alfresco.mobile.android.application.fragments.properties.PreviewGallery;
import org.alfresco.mobile.android.application.fragments.search.SearchFragment;
import org.alfresco.mobile.android.application.fragments.sites.BrowserSitesFragment;
import org.alfresco.mobile.android.application.fragments.sites.SiteMembersFragment;
import org.alfresco.mobile.android.application.fragments.workflow.process.ProcessesFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TaskDetailsFragment;
import org.alfresco.mobile.android.application.fragments.workflow.task.TasksFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.intent.PublicIntent;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.batch.capture.DeviceCapture;
import org.alfresco.mobile.android.application.operations.batch.capture.DeviceCaptureHelper;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.preferences.PasscodePreferences;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.alfresco.mobile.android.application.security.DataProtectionUserDialogFragment;
import org.alfresco.mobile.android.application.security.PassCodeActivity;
import org.alfresco.mobile.android.application.utils.ConnectivityUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.config.OdsConfigManager;
import org.opendataspace.android.app.fragments.OdsLinksFragment;
import org.opendataspace.android.app.session.OdsRepoType;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.ui.logging.OdsLog;

import java.io.File;
import java.io.Serializable;
import java.util.Stack;

/**
 * Main activity of the application.
 *
 * @author Jean Marie Pascal
 */
public class MainActivity extends BaseActivity
{
    private static final String TAG = MainActivity.class.getName();

    // SESSION FLAG
    private static final int SESSION_LOADING = 1;

    private static final int SESSION_ACTIVE = 2;

    private static final int SESSION_INACTIVE = 4;

    private static final int SESSION_ERROR = 8;

    private int sessionState = 0;

    private int sessionStateErrorMessageId;

    // MANAGE FRAGMENT STACK CENTRAL
    private Stack<String> stackCentral = new Stack<String>();

    private Folder importParent;

    private Node currentNode;

    // Device capture
    private DeviceCapture capture;

    private int fragmentQueue = -1;

    private boolean activateCheckPasscode = false;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        activateCheckPasscode = false;

        super.onCreate(savedInstanceState);

        // Check intent
        if (getIntent().hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
        {
            long accountId = getIntent().getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
            currentAccount = AccountManager.retrieveAccount(this, accountId);
        }

        // Loading progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_main);

        setProgressBarIndeterminateVisibility(false);

        if (savedInstanceState != null)
        {
            MainActivityHelper helper = new MainActivityHelper(savedInstanceState.getBundle(MainActivityHelper.TAG));
            currentAccount = helper.getCurrentAccount();
            importParent = helper.getFolder();
            fragmentQueue = helper.getFragmentQueue();
            if (helper.getDeviceCapture() != null)
            {
                capture = helper.getDeviceCapture();
                capture.setActivity(this);
            }
            stackCentral = helper.getStackCentral();
        }
        else
        {
            displayMainMenu();
        }

        if (SessionUtils.getAccount(this) != null)
        {
            currentAccount = SessionUtils.getAccount(this);
            if (currentAccount.getIsPaidAccount() &&
                    !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                // Check if we've prompted the user for Data Protection yet.
                // This is needed on new account creation, as the Activity gets
                // re-created after the account is created.
                DataProtectionUserDialogFragment.newInstance(true)
                        .show(getFragmentManager(), DataProtectionUserDialogFragment.TAG);

                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
            }
        }

        checkForUpdates();

        // Display or not Left/central panel for middle tablet.
        DisplayUtils.switchSingleOrTwo(this, false);

        if (IntentIntegrator.ACTION_DISLPAY_ACCOUNTS.equals(getIntent().getAction()))
        {
            displayAccounts();
        }
    }

    @Override
    protected void onStart()
    {
        IntentFilter filters = new IntentFilter();
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_ACCOUNT_INACTIVE);
        filters.addAction(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        filters.addCategory(IntentIntegrator.CATEGORY_OAUTH);
        filters.addCategory(IntentIntegrator.CATEGORY_OAUTH_REFRESH);
        filters.addAction(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR);
        filters.addAction(IntentIntegrator.ACTION_DECRYPT_ALL_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_ENCRYPT_ALL_COMPLETED);
        filters.addAction(IntentIntegrator.ACTION_DIALOG_EXTRA);

        registerPrivateReceiver(new MainActivityReceiver(), filters);
        registerPublicReceiver(new NetworkReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        super.onStart();
        OAuthRefreshTokenCallback.requestRefreshToken(getCurrentSession(), this);
        PassCodeActivity.requestUserPasscode(this);
        activateCheckPasscode = PasscodePreferences.hasPasscodeEnable(this);
        SynchroManager.getInstance(this).cronSync(currentAccount);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkForCrashes();
        checkSession();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (!activateCheckPasscode)
        {
            PasscodePreferences.updateLastActivity(this);
        }
        SynchroManager.saveSyncPrepareTimestamp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            String filename = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(GeneralPreferences.REQUIRES_ENCRYPT, "");
            if (!StorageManager.isSyncFile(this, new File(filename)))
            {
                DataProtectionManager.getInstance(this).checkEncrypt(getCurrentAccount(), new File(filename));
            }
        }

        if (requestCode == PassCodeActivity.REQUEST_CODE_PASSCODE)
        {
            if (resultCode == RESULT_CANCELED)
            {
                finish();
            }
            else
            {
                activateCheckPasscode = true;
                setSessionState(SESSION_ACTIVE);
                setProgressBarIndeterminateVisibility(false);
            }
        }

        if (capture != null && requestCode == capture.getRequestCode())
        {
            capture.capturedCallback(requestCode, resultCode, data);
        }
    }

    // TODO remove All this fonction and replace by broadcast.
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        try
        {
            // Shortcut to display favorites panel.
            if (IntentIntegrator.ACTION_SYNCHRO_DISPLAY.equals(intent.getAction()))
            {
                if (!isVisible(FavoritesSyncFragment.TAG))
                {
                    Fragment syncFrag = FavoritesSyncFragment.newInstance(ListingModeFragment.MODE_LISTING);
                    FragmentDisplayer.replaceFragment(this, syncFrag, DisplayUtils.getLeftFragmentId(this),
                            FavoritesSyncFragment.TAG, true);
                    clearCentralPane();
                }
                return;
            }

            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null &&
                    intent.getData().getHost().equals("activate-cloud-account") &&
                    getFragment(AccountDetailsFragment.TAG) != null)
            {

                ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).displayOAuthFragment();
                return;
            }

            //
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && IntentIntegrator.NODE_TYPE.equals(intent.getType()))
            {
                if (intent.getExtras().containsKey(IntentIntegrator.EXTRA_NODE))
                {
                    BaseFragment frag =
                            DetailsFragment.newInstance((Document) intent.getExtras().get(IntentIntegrator.EXTRA_NODE));
                    frag.setSession(SessionUtils.getSession(this));
                    FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, false);
                }
                return;
            }

            if (IntentIntegrator.ACTION_DISLPAY_ACCOUNTS.equals(intent.getAction()))
            {
                displayAccounts();
            }
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putBundle(MainActivityHelper.TAG, MainActivityHelper
                .createBundle(outState, stackCentral, currentAccount, capture, fragmentQueue, importParent));
    }

    // ///////////////////////////////////////////////////////////////////////////
    // HockeyApp Integration
    // ///////////////////////////////////////////////////////////////////////////
    private void checkForCrashes()
    {
        //ReportManager.checkForCrashes(this);
    }

    private void checkForUpdates()
    {
        //ReportManager.checkForUpdates(this);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SLIDE MENU
    // ///////////////////////////////////////////////////////////////////////////
    public void toggleSlideMenu()
    {
        if (getFragment(MainMenuFragment.TAG) != null && getFragment(MainMenuFragment.TAG).isAdded())
        {
            return;
        }
        View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        }
        else
        {
            MainMenuFragment slidefragment = (MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG);
            if (slidefragment != null)
            {
                slidefragment.refreshData();
            }
            showSlideMenu();
        }
    }

    private void hideSlideMenu()
    {
        View slideMenu = findViewById(R.id.slide_pane);
        slideMenu.setVisibility(View.GONE);
        slideMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rbm_out_to_left));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // We await the end of sliding menu animation to display the bottom bar
        if (!DisplayUtils.hasCentralPane(this))
        {
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    invalidateOptionsMenu();
                }
            }, 250);
        }
        else
        {
            invalidateOptionsMenu();
        }
    }

    private boolean isSlideMenuVisible()
    {
        return findViewById(R.id.slide_pane).getVisibility() == View.VISIBLE;
    }

    private void showSlideMenu()
    {
        View slideMenu = findViewById(R.id.slide_pane);
        slideMenu.setVisibility(View.VISIBLE);
        slideMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rbm_in_from_left));
        getActionBar().setDisplayHomeAsUpEnabled(false);
        invalidateOptionsMenu();
    }

    private void doMainMenuAction(int id)
    {
        View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            clearCentralPane();
        }

        fragmentQueue = -1;
        switch (id)
        {
        case R.id.menu_browse_root:
        case R.id.menu_browse_shared:
        case R.id.menu_browse_global:
        case R.id.menu_browse_ext1:
            browseRepo(id);
            break;
        case R.id.menu_downloads:
            if (currentAccount == null)
            {
                MessengerManager.showLongToast(this, getString(R.string.loginfirst));
            }
            else
            {
                addLocalFileNavigationFragment();
            }
            break;
        case R.id.menu_notifications:
            if (currentAccount == null)
            {
                MessengerManager.showLongToast(this, getString(R.string.loginfirst));
            }
            else
            {
                startActivity(new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS)
                        .putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, currentAccount.getId()));
            }
            break;
        default:
            break;
        }
    }

    @SuppressWarnings("unused")
    public void showMainMenuFragment(View v)
    {
        doMainMenuAction(v.getId());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void setSessionState(int state)
    {
        sessionState = state;
    }

    public void setSessionErrorMessageId(int messageId)
    {
        sessionState = SESSION_ERROR;
        sessionStateErrorMessageId = messageId;
    }

    private void checkSession()
    {
        if (accountManager == null || (accountManager.isEmpty() && accountManager.hasData()))
        {
            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();
            return;
        }
        else if (getCurrentAccount() == null && getCurrentSession() == null)
        {
            ActionManager.loadAccount(this, accountManager.getDefaultAccount());
        }
        else if (sessionState == SESSION_ERROR && getCurrentSession() == null &&
                ConnectivityUtils.hasInternetAvailable(this))
        {
            ActionManager.loadAccount(this, getCurrentAccount());
        }
        invalidateOptionsMenu();
    }

    private boolean checkSession(int actionMainMenuId)
    {
        switch (sessionState)
        {
        case SESSION_ERROR:
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_alfresco_logo);
            b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_session_creation_message);
            b.putInt(SimpleAlertDialogFragment.PARAM_MESSAGE, sessionStateErrorMessageId);
            b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            ActionManager.actionDisplayDialog(this, b);
            return false;
        case SESSION_LOADING:
            displayWaitingDialog();
            fragmentQueue = actionMainMenuId;
            return false;
        default:
            if (!ConnectivityUtils.hasNetwork(this))
            {
                return false;
            }
            else if (getCurrentAccount() != null && getCurrentAccount().getActivation() != null)
            {
                MessengerManager.showToast(this, R.string.account_not_activated);
                return false;
            }
            break;
        }
        return true;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FRAGMENTS
    // ///////////////////////////////////////////////////////////////////////////
    public void addNavigationFragment(Folder f)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(f);
    }

    public void addNavigationFragment(Folder f, boolean isShortCut)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(f, isShortCut);
    }

    public void addNavigationFragment(String path)
    {
        clearScreen();
        clearCentralPane();
        super.addBrowserFragment(path);
    }

    public void addNavigationFragmentById(String folderIdentifier)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(folderIdentifier);
    }

    public void addNavigationFragment(Site s)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(s);
    }

    public void addLocalFileNavigationFragment()
    {
        BaseFragment frag = FileExplorerMenuFragment.newInstance();
        FragmentDisplayer
                .replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), FileExplorerMenuFragment.TAG, true);
    }

    public void addLocalFileNavigationFragment(File file)
    {
        BaseFragment frag = FileExplorerFragment.newInstance(file);
        FragmentDisplayer
                .replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), FileExplorerFragment.TAG, true);
    }

    public void addLocalFileNavigationFragment(int mediaType)
    {
        LibraryFragment frag = LibraryFragment.newInstance(mediaType);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), LibraryFragment.TAG, true);
    }

    public void addPropertiesFragment(Node n, Folder parentFolder, boolean forceBackStack)
    {
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = DetailsFragment.newInstance(n, parentFolder);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, forceBackStack);
    }

    public void addPropertiesFragment(String nodeIdentifier)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = DetailsFragment.newInstance(nodeIdentifier);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, b);
    }

    public void addPropertiesFragment(boolean isFavorite, String nodeIdentifier)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = DetailsFragment.newInstance(nodeIdentifier, isFavorite);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, b);
    }

    public void addPropertiesFragment(String nodeIdentifier, boolean backstack)
    {
        BaseFragment frag = DetailsFragment.newInstance(nodeIdentifier);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, backstack);
    }

    public void addPersonProfileFragment(String userIdentifier)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = PersonProfileFragment.newInstance(userIdentifier);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), PersonProfileFragment.TAG, b);
    }

    public void addMembersFragment(Site site)
    {
        BaseFragment frag = SiteMembersFragment.newInstance(site);
        FragmentDisplayer
                .replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), SiteMembersFragment.TAG, true);
    }

    public void addTaskDetailsFragment(Task task, boolean backStack)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        TaskDetailsFragment frag = TaskDetailsFragment.newInstance(task);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), TaskDetailsFragment.TAG,
                (b != backStack) ? backStack : b);
    }

    public void addProcessDetailsFragment(Process process, boolean backStack)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        TaskDetailsFragment frag = TaskDetailsFragment.newInstance(process);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), TaskDetailsFragment.TAG,
                (b != backStack) ? backStack : b);
    }

    public void addPropertiesFragment(Node n)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        addPropertiesFragment(n, getImportParent(), b);
    }

    public void addGalleryFragment()
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        BaseFragment frag = PreviewGallery.newInstance();
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), PreviewGallery.TAG, b);
    }

//    public void addComments(Node n)
//    {
//        if (DisplayUtils.hasCentralPane(this))
//        {
//            stackCentral.push(CommentsFragment.TAG);
//        }
//        BaseFragment frag = CommentsFragment.newInstance(n);
//        frag.setSession(SessionUtils.getSession(this));
//        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(true), CommentsFragment.TAG, true);
//        ((View) findViewById(getFragmentPlace(true)).getParent()).setVisibility(View.VISIBLE);
//    }

    public void addAccountDetails(long id)
    {
        Boolean b = !DisplayUtils.hasCentralPane(this);
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(AccountsFragment.TAG);
        }
        BaseFragment frag = AccountDetailsFragment.newInstance(id);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getMainPaneId(this), AccountDetailsFragment.TAG, b);
    }

    public void displayAbout()
    {

        if (DisplayUtils.hasCentralPane(this))
        {
            DialogFragment f = new AboutFragment();
            f.show(getFragmentManager(), AboutFragment.TAG);
        }
        else
        {
            Fragment f = new AboutFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), AboutFragment.TAG, true);
        }
    }

    public void displayPreferences()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_SETTINGS);
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
            startActivity(i);
        }
        else
        {
            Fragment f = new GeneralPreferences();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), GeneralPreferences.TAG, true);
        }
    }

    public void displayMainMenu()
    {
        Fragment f = new MainMenuFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), MainMenuFragment.TAG, false);
        hideSlideMenu();
    }

    public void displayAccounts()
    {
        Fragment f = new AccountsFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), AccountsFragment.TAG, true);
    }

    public void displayNetworks()
    {
        if (getCurrentSession() instanceof CloudSession)
        {
            Fragment f = new CloudNetworksFragment();
            FragmentDisplayer
                    .replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), CloudNetworksFragment.TAG, true);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS FRAGMENTS
    // ///////////////////////////////////////////////////////////////////////////
    public void clearScreen()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            findViewById(DisplayUtils.getCentralFragmentId(this)).setBackgroundColor(Color.TRANSPARENT);
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            FragmentDisplayer.removeFragment(this, DisplayUtils.getCentralFragmentId(this));
        }
        if (DisplayUtils.hasLeftPane(this))
        {
            DisplayUtils.show(DisplayUtils.getLeftPane(this));
        }
    }

    private void clearCentralPane()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            findViewById(DisplayUtils.getCentralFragmentId(this)).setBackgroundColor(Color.TRANSPARENT);
        }
        FragmentDisplayer.removeFragment(this, stackCentral);
        stackCentral.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if (sessionState == SESSION_ERROR && getCurrentSession() == null)
        {
            MenuItem mi =
                    menu.add(Menu.NONE, MenuActionItem.ACCOUNT_RELOAD, Menu.FIRST, R.string.retry_account_loading);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        if (isSlideMenuVisible() || isVisible(MainMenuFragment.TAG))
        {
            MainMenuFragment.getMenu(menu);
            return true;
        }

        if (isVisible(TaskDetailsFragment.TAG))
        {
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(ProcessesFragment.TAG))
        {
            ProcessesFragment.getMenu(menu);
            return true;
        }

        if (isVisible(SearchFragment.TAG))
        {
            SearchFragment.getMenu(menu);
            return true;
        }

        if (isVisible(TasksFragment.TAG))
        {
            TasksFragment.getMenu(menu);
            return true;
        }

        if (isVisible(FileExplorerFragment.TAG))
        {
            ((FileExplorerFragment) getFragment(FileExplorerFragment.TAG)).getMenu(menu);
        }

        if (isVisible(ActivitiesFragment.TAG))
        {
            ((ActivitiesFragment) getFragment(ActivitiesFragment.TAG)).getMenu(menu);
        }

        if (isVisible(AccountDetailsFragment.TAG))
        {
            ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(AccountsFragment.TAG) && !isVisible(AccountTypesFragment.TAG) &&
                !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountsFragment.getMenu(this, menu);
            return true;
        }

        if (isVisible(BrowserSitesFragment.TAG))
        {
            BrowserSitesFragment.getMenu(menu);
            return true;
        }

        if (isVisible(DetailsFragment.TAG))
        {
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(ChildrenBrowserFragment.TAG))
        {
            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(FavoritesSyncFragment.TAG))
        {
            ((FavoritesSyncFragment) getFragment(FavoritesSyncFragment.TAG)).getMenu(menu);
            return true;
        }

        if (isVisible(OdsLinksFragment.TAG))
        {
            ((OdsLinksFragment) getFragment(OdsLinksFragment.TAG)).getMenu(menu);
            return true;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case MenuActionItem.ACCOUNT_RELOAD:
            ActionManager.loadAccount(this, getCurrentAccount());
            return true;
        case MenuActionItem.MENU_PROFILE:
            PersonProfileFragment frag = PersonProfileFragment.newInstance(getCurrentAccount().getUsername());
            frag.show(getFragmentManager(), PersonProfileFragment.TAG);
            return true;
        case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_PHOTO:
        case MenuActionItem.MENU_DEVICE_CAPTURE_CAMERA_VIDEO:
        case MenuActionItem.MENU_DEVICE_CAPTURE_MIC_AUDIO:
            capture = DeviceCaptureHelper.createDeviceCapture(this, item.getItemId());
            return true;
        case MenuActionItem.MENU_ACCOUNT_ADD:
            ((AccountsFragment) getFragment(AccountsFragment.TAG)).add();
            return true;

        case MenuActionItem.MENU_ACCOUNT_EDIT:
            ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).edit();
            return true;

        case MenuActionItem.MENU_ACCOUNT_DELETE:
            ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).delete();
            return true;

        case MenuActionItem.MENU_SEARCH_FOLDER:
            ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).search(getFragmentPlace(false));
            return true;

        case MenuActionItem.MENU_SEARCH:
            FragmentDisplayer
                    .replaceFragment(this, SearchFragment.newInstance(), getFragmentPlace(), SearchFragment.TAG, true);
            return true;

        case MenuActionItem.MENU_CREATE_FOLDER:
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFolder();
            }
            else if (getFragment(FileExplorerFragment.TAG) != null)
            {
                ((FileExplorerFragment) getFragment(FileExplorerFragment.TAG)).createFolder();
            }
            return true;

        case MenuActionItem.MENU_CREATE_DOCUMENT:
            String fragmentTag = FileExplorerFragment.TAG;
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                fragmentTag = ChildrenBrowserFragment.TAG;
            }
            DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment.newInstance(currentAccount, fragmentTag);
            dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
            return true;

        case MenuActionItem.MENU_UPLOAD:
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                Intent i = new Intent(IntentIntegrator.ACTION_PICK_FILE, null, this, PublicDispatcherActivity.class);
                i.putExtra(IntentIntegrator.EXTRA_FOLDER, StorageManager.getDownloadFolder(this, getCurrentAccount()));
                i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
                getFragment(ChildrenBrowserFragment.TAG).startActivityForResult(i, PublicIntent.REQUESTCODE_FILEPICKER);
            }
            return true;
        case MenuActionItem.MENU_REFRESH:
            if (getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)) instanceof RefreshFragment)
            {
                ((RefreshFragment) getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)))
                        .refresh();
            }
            return true;

        case MenuActionItem.MENU_PASTE:
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).pasteFileList();
            }
            return true;

        case MenuActionItem.MENU_SHARE:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).share();
            return true;
        case MenuActionItem.MENU_OPEN_IN:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).openin();
            return true;
        case MenuActionItem.MENU_DOWNLOAD:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).download();
            return true;
        case MenuActionItem.MENU_UPDATE:
            if (getFragment(DetailsFragment.TAG) != null)
            {
                Intent i = new Intent(IntentIntegrator.ACTION_PICK_FILE, null, this, PublicDispatcherActivity.class);
                i.putExtra(IntentIntegrator.EXTRA_FOLDER, StorageManager.getDownloadFolder(this, getCurrentAccount()));
                i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
                getFragment(DetailsFragment.TAG).startActivityForResult(i, PublicIntent.REQUESTCODE_FILEPICKER);
            }
            return true;
        case MenuActionItem.MENU_EDIT:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).edit();
            return true;
        case MenuActionItem.MENU_COMMENT:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).comment();
            return true;
        case MenuActionItem.MENU_VERSION_HISTORY:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).versions();
            return true;
        case MenuActionItem.MENU_TAGS:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).tags();
            return true;
        case MenuActionItem.MENU_LINKS:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).links();
            return true;
        case MenuActionItem.MENU_CREATE_LINK:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).createLink();
            return true;
        case MenuActionItem.MENU_DELETE:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).delete();
            return true;
        case MenuActionItem.MENU_DISPLAY_GALLERY:
            addGalleryFragment();
            return true;
        case MenuActionItem.MENU_SITE_LIST_REQUEST:
            ((BrowserSitesFragment) getFragment(BrowserSitesFragment.TAG)).displayJoinSiteRequests();
            return true;
        case MenuActionItem.MENU_WORKFLOW_ADD:
            Intent i = new Intent(IntentIntegrator.ACTION_START_PROCESS, null, this, PrivateDialogActivity.class);
            if (getFragment(DetailsFragment.TAG) != null)
            {
                Document doc = (Document) ((DetailsFragment) getFragment(DetailsFragment.TAG)).getCurrentNode();
                i.putExtra(IntentIntegrator.EXTRA_DOCUMENT, (Serializable) doc);
            }
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
            startActivity(i);
            return true;
        case MenuActionItem.MENU_TASK_REASSIGN:
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).reassign();
            return true;
        case MenuActionItem.MENU_TASK_CLAIM:
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).claim();
            return true;
        case MenuActionItem.MENU_PROCESS_HISTORY:
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).displayHistory();
            return true;
        case MenuActionItem.MENU_TASK_UNCLAIM:
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).unclaim();
            return true;
        case MenuActionItem.MENU_PROCESS_DETAILS:
            ((TaskDetailsFragment) getFragment(TaskDetailsFragment.TAG)).showProcessDiagram();
            return true;
        case MenuActionItem.MENU_SYNC_WARNING:
            ((FavoritesSyncFragment) getFragment(FavoritesSyncFragment.TAG)).displayWarning();
            return true;
        case MenuActionItem.MENU_SETTINGS_ID:
            displayPreferences();
            hideSlideMenu();
            return true;
        case MenuActionItem.MENU_HELP_ID:
            UIUtils.displayHelp(this);
            hideSlideMenu();
            return true;
        case MenuActionItem.MENU_ABOUT_ID:
            displayAbout();
            DisplayUtils.switchSingleOrTwo(this, true);
            hideSlideMenu();
            return true;
        case android.R.id.home:
            // app icon in action bar clicked; go home
            toggleSlideMenu();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (isSlideMenuVisible())
        {
            hideSlideMenu();
        }
        else if (getResources().getBoolean(R.bool.tablet_middle))
        {
            // Sepcific case where we want to display one or two panes.
            if (getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)) == null)
            {
                super.onBackPressed();
            }
            else
            {
                Fragment fr = getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this));

                boolean backStack = true;

                if (fr instanceof AccountsFragment)
                {
                    ((AccountsFragment) fr).unselect();
                    backStack = false;
                }

                if (fr instanceof ChildrenBrowserFragment)
                {
                    ((ChildrenBrowserFragment) fr).unselect();
                    backStack = false;
                }

                if (fr instanceof SearchFragment)
                {
                    backStack = false;
                }

                if (fr instanceof ActivitiesFragment)
                {
                    backStack = false;
                }

                if (fr instanceof FavoritesSyncFragment)
                {
                    backStack = false;
                }

                if (fr instanceof TasksFragment)
                {
                    backStack = false;
                }

                // Special case : if Activities Fragment
                if (backStack)
                {
                    getFragmentManager().popBackStack();
                }
                else
                {
                    FragmentDisplayer.remove(this,
                            getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)), false);
                }
            }
        }
        else
        {
            super.onBackPressed();
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            invalidateOptionsMenu();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Node getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode)
    {
        this.currentNode = currentNode;
    }

    // For Creating file in childrenbrowser
    public Folder getImportParent()
    {
        if (getFragment(ChildrenBrowserFragment.TAG) != null)
        {
            importParent = ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getImportFolder();
            if (importParent == null)
            {
                importParent = ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).getParent();
            }
        }
        return importParent;
    }

    public boolean hasActivateCheckPasscode()
    {
        return activateCheckPasscode;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class MainActivityReceiver extends BroadcastReceiver
    {

        @SuppressLint("CommitPrefEdits")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            OdsLog.d(TAG, intent.getAction());

            Activity activity = MainActivity.this;

            if (IntentIntegrator.ACTION_DECRYPT_ALL_COMPLETED.equals(intent.getAction()) ||
                    IntentIntegrator.ACTION_ENCRYPT_ALL_COMPLETED.equals(intent.getAction()))
            {
                removeWaitingDialog();
                if (getFragment(GeneralPreferences.TAG) != null)
                {
                    ((GeneralPreferences) getFragment(GeneralPreferences.TAG)).refreshDataProtection();
                }

                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT.equals(intent.getAction()) ||
                    IntentIntegrator.ACTION_RELOAD_ACCOUNT.equals(intent.getAction()))
            {
                // Change activity state to loading.
                setSessionState(SESSION_LOADING);

                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    return;
                }

                // Assign the account
                currentAccount = AccountManager
                        .retrieveAccount(context, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));

                if (getFragment(MainMenuFragment.TAG) != null)
                {
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).displayFavoriteStatut();
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).hideWorkflowMenu(currentAccount);
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
                {
                    ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).displayFavoriteStatut();
                    ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG)).hideWorkflowMenu(currentAccount);
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                // Return to root screen
                try
                {
                    activity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                catch (Exception ex)
                {
                    OdsLog.ex(TAG, ex);
                }

                // Display progress
                activity.setProgressBarIndeterminateVisibility(true);

                // Add accountName in actionBar
                UIUtils.displayTitle(activity, getString(R.string.app_name));
                activity.getActionBar().setDisplayHomeAsUpEnabled(false);

                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_STARTED.equals(intent.getAction()))
            {
                setSessionState(SESSION_LOADING);
                activity.setProgressBarIndeterminateVisibility(true);
                if (intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    Account acc = AccountManager
                            .retrieveAccount(context, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));

                    if (acc != null)
                    {
                        MessengerManager.showLongToast(activity, acc.getDescription());
                    }
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                if (getCurrentSession() instanceof RepositorySession)
                {
                    if (getCurrentSession() instanceof OdsRepositorySession)
                    {
                        OdsConfigManager.getInstance(context).retrieveConfiguration(activity, currentAccount);
                    }
                    else
                    {
                        ConfigurationManager.getInstance(activity).retrieveConfiguration(activity, currentAccount);
                    }
                    if (getFragment(MainMenuFragment.TAG) != null)
                    {
                        ((MainMenuFragment) getFragment(MainMenuFragment.TAG))
                                .displayFolderShortcut(getCurrentSession());
                    }

                    if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
                    {
                        ((MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG))
                                .displayFolderShortcut(getCurrentSession());
                    }
                }

                if (!isCurrentAccountToLoad(intent))
                {
                    return;
                }

                setSessionState(SESSION_ACTIVE);
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

                // Used for launching last pressed action button from main menu
                if (fragmentQueue != -1)
                {
                    doMainMenuAction(fragmentQueue);
                }
                fragmentQueue = -1;

                // Save latest position as default future one
                AccountsPreferences.setDefaultAccount(activity, currentAccount.getId());

                // Check Last cloud session creation ==> prevent oauth token
                // expiration
                if (getCurrentSession() instanceof CloudSession)
                {
                    OAuthRefreshTokenCallback.saveLastCloudLoadingTime(activity);
                }
                else
                {
                    OAuthRefreshTokenCallback.removeLastCloudLoadingTime(activity);
                }

                // NB : temporary code ?
                // Check to see if we have an old account that needs its paid
                // network flag setting.
                if (!currentAccount.getIsPaidAccount())
                {
                    boolean paidNetwork;
                    if (getCurrentSession() instanceof CloudSession)
                    {
                        paidNetwork = ((CloudSession) getCurrentSession()).getNetwork().isPaidNetwork();
                    }
                    else
                    {
                        paidNetwork = getCurrentSession().getRepositoryInfo().getEdition()
                                .equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE);
                    }

                    if (paidNetwork)
                    {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                        prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();

                        DataProtectionUserDialogFragment.newInstance(true)
                                .show(getFragmentManager(), DataProtectionUserDialogFragment.TAG);

                        currentAccount = accountManager.update(currentAccount.getId(), currentAccount.getDescription(),
                                currentAccount.getUrl(), currentAccount.getUsername(), currentAccount.getPassword(),
                                currentAccount.getRepositoryId(), currentAccount.getTypeId(),
                                currentAccount.getActivation(), currentAccount.getAccessToken(),
                                currentAccount.getRefreshToken(), 1, currentAccount.getProtocolType());
                    }
                }

                // Start Sync if active
                if (GeneralPreferences.hasDisplayedActivateSync(activity))
                {
                    SynchroManager.getInstance(activity).sync(currentAccount);
                }

                if (getFragment(MainMenuFragment.TAG) != null)
                {
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
                {
                    ((MainMenuFragment) getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                rebrand();
                return;
            }

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                Account tmpAccount = AccountManager
                        .retrieveAccount(activity, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                if (tmpAccount != null && tmpAccount.getIsPaidAccount() &&
                        !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
                {
                    // Check if we've prompted the user for Data Protection yet.
                    // This is needed on new account creation, as the Activity
                    // gets
                    // re-created after the account is created.
                    DataProtectionUserDialogFragment.newInstance(true)
                            .show(getFragmentManager(), DataProtectionUserDialogFragment.TAG);

                    prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_ERROR.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent))
                {
                    return;
                }

                // Display error dialog message
                ActionManager.actionDisplayDialog(context, intent.getExtras());

                // Change status
                setSessionErrorMessageId(intent.getExtras().getInt(SimpleAlertDialogFragment.PARAM_MESSAGE));

                // Reset currentAccount & references
                if (intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    currentAccount = AccountManager
                            .retrieveAccount(context, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));

                    if (currentAccount != null)
                    {
                        applicationManager.removeAccount(currentAccount.getId());
                    }
                }

                // Stop progress indication
                activity.setProgressBarIndeterminateVisibility(false);
                invalidateOptionsMenu();
                return;
            }

            if (IntentIntegrator.ACTION_ACCOUNT_INACTIVE.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent) && !intent.hasExtra(IntentIntegrator.ACTION_ACCOUNT_INACTIVE))
                {
                    return;
                }

                setSessionState(SESSION_INACTIVE);
                activity.setProgressBarIndeterminateVisibility(false);
                MessengerManager.showLongToast(activity, getString(R.string.account_not_activated));
                return;
            }

            if (IntentIntegrator.ACTION_USER_AUTHENTICATION.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent))
                {
                    return;
                }

                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    return;
                }
                Long accountId = intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
                Account acc = AccountManager.retrieveAccount(activity, accountId);

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH) &&
                        getFragment(AccountOAuthFragment.TAG) == null ||
                        (getFragment(AccountOAuthFragment.TAG) != null &&
                                getFragment(AccountOAuthFragment.TAG).isAdded()))
                {
                    AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(acc);
                    FragmentDisplayer.replaceFragment(activity, newFragment, DisplayUtils.getMainPaneId(activity),
                            AccountOAuthFragment.TAG, true);
                    DisplayUtils.switchSingleOrTwo(activity, true);
                    return;
                }

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH_REFRESH))
                {
                    getLoaderManager().restartLoader(OAuthRefreshTokenLoader.ID, null,
                            new OAuthRefreshTokenCallback(activity, acc, (CloudSession) getCurrentSession()));
                }
            }

            if (IntentIntegrator.ACTION_DIALOG_EXTRA.equals(intent.getAction()))
            {
                int id = intent.getIntExtra(IntentIntegrator.EXTRA_DIALOG_ACTION, 0);

                switch (id)
                {
                case R.string.account_change_password:
                    if (currentAccount != null)
                    {
                        addAccountDetails(currentAccount.getId());
                    }
                    break;

                default:
                }
            }
        }
    }

    public class NetworkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                if (ConnectivityUtils.hasInternetAvailable(context))
                {
                    if (sessionState == SESSION_ERROR && getCurrentAccount() == null && getCurrentSession() == null)
                    {
                        ActionManager.loadAccount(MainActivity.this, accountManager.getDefaultAccount());
                    }
                    else if (sessionState == SESSION_ERROR && getCurrentSession() == null)
                    {
                        ActionManager.loadAccount(MainActivity.this, getCurrentAccount());
                    }
                    invalidateOptionsMenu();
                }
            }
            catch (Exception e)
            {
                // Nothing special
            }
        }
    }

    // Due to dropdown the account loaded might not be the last one to load.
    private boolean isCurrentAccountToLoad(Intent intent)
    {
        return currentAccount != null && intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID) &&
                (currentAccount.getId() == intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
    }

    private void browseRepo(int menuId)
    {
        if (!checkSession(menuId))
        {
            return;
        }

        AlfrescoSession ses = getCurrentSession();

        if (ses == null)
        {
            return;
        }

        OdsRepositorySession ods = null, current = null;

        if (ses instanceof OdsRepositorySession)
        {
            ods = (OdsRepositorySession) ses;
        }

        if (menuId != R.id.menu_browse_root && ods == null)
        {
            return;
        }

        switch (menuId)
        {
        case R.id.menu_browse_root:
            if (ods != null)
            {
                ses = current = ods.getByType(OdsRepoType.DEFAULT);
            }
            break;

        case R.id.menu_browse_global:
            ses = current = ods.getByType(OdsRepoType.GLOBAL);
            break;

        case R.id.menu_browse_shared:
            ses = current = ods.getByType(OdsRepoType.SHARED);
            break;

        case R.id.menu_browse_ext1:
            ses = current = ods.getByType(OdsRepoType.PROJECTS);
            break;

        default:
            return;
        }

        if (ods != null)
        {
            ods.setCurrent(current);
        }

        ChildrenBrowserFragment frag = ChildrenBrowserFragment.newInstance(ses.getRootFolder());
        frag.setSession(ses);
        FragmentDisplayer
                .replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), ChildrenBrowserFragment.TAG, true);
    }
}
