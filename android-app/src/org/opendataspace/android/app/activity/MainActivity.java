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
package org.opendataspace.android.app.activity;

import java.io.File;
import java.util.Stack;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.accounts.AccountManager;
import org.opendataspace.android.app.accounts.fragment.AccountDetailsFragment;
import org.opendataspace.android.app.accounts.fragment.AccountEditFragment;
import org.opendataspace.android.app.accounts.fragment.AccountOAuthFragment;
import org.opendataspace.android.app.accounts.fragment.AccountsFragment;
import org.opendataspace.android.app.accounts.networks.CloudNetworksFragment;
import org.opendataspace.android.app.accounts.oauth.OAuthRefreshTokenCallback;
import org.opendataspace.android.app.accounts.oauth.OAuthRefreshTokenLoader;
import org.opendataspace.android.app.accounts.signup.CloudSignupDialogFragment;
import org.opendataspace.android.app.fragments.DisplayUtils;
import org.opendataspace.android.app.fragments.FragmentDisplayer;
import org.opendataspace.android.app.fragments.ListingModeFragment;
import org.opendataspace.android.app.fragments.RefreshFragment;
import org.opendataspace.android.app.fragments.SimpleAlertDialogFragment;
import org.opendataspace.android.app.fragments.WaitingDialogFragment;
import org.opendataspace.android.app.fragments.about.AboutFragment;
import org.opendataspace.android.app.fragments.activities.ActivitiesFragment;
import org.opendataspace.android.app.fragments.browser.ChildrenBrowserFragment;
import org.opendataspace.android.app.fragments.comments.CommentsFragment;
import org.opendataspace.android.app.fragments.create.DocumentTypesDialogFragment;
import org.opendataspace.android.app.fragments.favorites.FavoritesSyncFragment;
import org.opendataspace.android.app.fragments.fileexplorer.FileExplorerFragment;
import org.opendataspace.android.app.fragments.fileexplorer.FileExplorerMenuFragment;
import org.opendataspace.android.app.fragments.fileexplorer.LibraryFragment;
import org.opendataspace.android.app.fragments.menu.MainMenuFragment;
import org.opendataspace.android.app.fragments.menu.MenuActionItem;
import org.opendataspace.android.app.fragments.properties.DetailsFragment;
import org.opendataspace.android.app.fragments.properties.MetadataFragment;
import org.opendataspace.android.app.fragments.search.KeywordSearch;
import org.opendataspace.android.app.fragments.sites.BrowserSitesFragment;
import org.opendataspace.android.app.fragments.upload.UploadFormFragment;
import org.opendataspace.android.app.fragments.versions.VersionFragment;
import org.opendataspace.android.app.intent.IntentIntegrator;
import org.opendataspace.android.app.intent.PublicIntent;
import org.opendataspace.android.app.manager.ActionManager;
import org.opendataspace.android.app.manager.StorageManager;
import org.opendataspace.android.app.operations.batch.capture.DeviceCapture;
import org.opendataspace.android.app.operations.batch.capture.DeviceCaptureHelper;
import org.opendataspace.android.app.operations.sync.SynchroManager;
import org.opendataspace.android.app.preferences.AccountsPreferences;
import org.opendataspace.android.app.preferences.GeneralPreferences;
import org.opendataspace.android.app.preferences.PasscodePreferences;
import org.opendataspace.android.app.security.DataProtectionManager;
import org.opendataspace.android.app.security.DataProtectionUserDialogFragment;
import org.opendataspace.android.app.security.PassCodeActivity;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.app.utils.ConnectivityUtils;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.app.utils.UIUtils;
import org.alfresco.mobile.android.api.constants.OnPremiseConstant;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;

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
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        activateCheckPasscode = false;

        super.onCreate(savedInstanceState);

        // Check intent
        if (getIntent().hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
        {
            final long accountId = getIntent().getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
            currentAccount = AccountManager.retrieveAccount(this, accountId);
        }

        // Loading progress
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.app_main);

        setProgressBarIndeterminateVisibility(false);

        if (savedInstanceState != null)
        {
            final MainActivityHelper helper = new MainActivityHelper(savedInstanceState.getBundle(MainActivityHelper.TAG));
            currentAccount = helper.getCurrentAccount();
            importParent = helper.getFolder();
            fragmentQueue = helper.getFragmentQueue();
            if (helper.getDeviceCapture() != null)
            {
                capture = helper.getDeviceCapture();
                capture.setActivity(this);
            }
            stackCentral = helper.getStackCentral();
        } else
        {
            displayMainMenu();
        }

        if (SessionUtils.getAccount(this) != null)
        {
            currentAccount = SessionUtils.getAccount(this);
            if (currentAccount.getIsPaidAccount()
                    && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
            {
                // Check if we've prompted the user for Data Protection yet.
                // This is needed on new account creation, as the Activity gets
                // re-created after the account is created.
                DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                        DataProtectionUserDialogFragment.TAG);

                prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();
            }
        }

        // REDIRECT To Accounts Fragment if signup process
        if (IntentIntegrator.ACTION_CHECK_SIGNUP.equals(getIntent().getAction()))
        {
            displayAccounts();
        }

        // Display or not Left/central panel for middle tablet.
        DisplayUtils.switchSingleOrTwo(this, false);

        // TODO REMOVE
        // SynchroSchema.reset(DatabaseManager.newInstance(this).getWriteDb());
    }

    @Override
    protected void onStart()
    {
        final IntentFilter filters = new IntentFilter();
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

        registerPrivateReceiver(new MainActivityReceiver(), filters);

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
        SynchroManager.updateLastActivity(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data)
    {
        if (requestCode == PublicIntent.REQUESTCODE_DECRYPTED)
        {
            final String filename = PreferenceManager.getDefaultSharedPreferences(this).getString(
                    GeneralPreferences.REQUIRES_ENCRYPT, "");
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
            } else
            {
                activateCheckPasscode = true;
            }
        }

        if (capture != null && requestCode == capture.getRequestCode())
        {
            capture.capturedCallback(requestCode, resultCode, data);
        }
    }

    // TODO remove All this fonction and replace by broadcast.
    @Override
    protected void onNewIntent(final Intent intent)
    {
        super.onNewIntent(intent);
        try
        {
            // Shortcut to display favorites panel.
            if (IntentIntegrator.ACTION_SYNCHRO_DISPLAY.equals(intent.getAction()))
            {
                if (!isVisible(FavoritesSyncFragment.TAG))
                {
                    final Fragment syncFrag = FavoritesSyncFragment.newInstance(ListingModeFragment.MODE_LISTING);
                    FragmentDisplayer.replaceFragment(this, syncFrag, DisplayUtils.getLeftFragmentId(this),
                            FavoritesSyncFragment.TAG, true);
                    clearCentralPane();
                }
                return;
            }

            // Intent for CLOUD SIGN UP
            if (IntentIntegrator.ACTION_CHECK_SIGNUP.equals(intent.getAction()))
            {
                FragmentDisplayer.removeFragment(this, CloudSignupDialogFragment.TAG);
                displayAccounts();
                return;
            }

            if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null
                    && intent.getData().getHost().equals("activate-cloud-account")
                    && getFragment(AccountDetailsFragment.TAG) != null)
            {

                ((AccountDetailsFragment) getFragment(AccountDetailsFragment.TAG)).displayOAuthFragment();
                return;
            }

            //
            if (Intent.ACTION_VIEW.equals(intent.getAction()) && PublicIntent.NODE_TYPE.equals(intent.getType()))
            {
                if (intent.getExtras().containsKey(PublicIntent.EXTRA_NODE))
                {
                    final BaseFragment frag = MetadataFragment.newInstance((Document) intent.getExtras().get(
                            PublicIntent.EXTRA_NODE));
                    frag.setSession(SessionUtils.getSession(this));
                    FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, false);
                }
                return;
            }

            // Intent for display Sign up Dialog
            if (Intent.ACTION_VIEW.equals(intent.getAction())
                    && IntentIntegrator.ALFRESCO_SCHEME_SHORT.equals(intent.getData().getScheme())
                    && IntentIntegrator.CLOUD_SIGNUP_I.equals(intent.getData().getHost()))
            {
                getFragmentManager().popBackStack(AccountEditFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                final CloudSignupDialogFragment newFragment = new CloudSignupDialogFragment();
                FragmentDisplayer.replaceFragment(this, newFragment, DisplayUtils.getFragmentPlace(this),
                        CloudSignupDialogFragment.TAG, true);
            }
        }
        catch (final Exception e)
        {
            Log.w(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putBundle(MainActivityHelper.TAG, MainActivityHelper.createBundle(outState, stackCentral,
                currentAccount, capture, fragmentQueue, importParent));
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
        final View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        } else
        {
            final MainMenuFragment slidefragment = (MainMenuFragment) getFragment(MainMenuFragment.SLIDING_TAG);
            if (slidefragment != null)
            {
                slidefragment.refreshData();
            }
            showSlideMenu();
        }
    }

    private void hideSlideMenu()
    {
        final View slideMenu = findViewById(R.id.slide_pane);
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
        } else
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
        final View slideMenu = findViewById(R.id.slide_pane);
        slideMenu.setVisibility(View.VISIBLE);
        slideMenu.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rbm_in_from_left));
        getActionBar().setDisplayHomeAsUpEnabled(false);
        invalidateOptionsMenu();
    }

    private void doMainMenuAction(final int id)
    {
        BaseFragment frag = null;

        final View slideMenu = findViewById(R.id.slide_pane);
        if (slideMenu.getVisibility() == View.VISIBLE)
        {
            hideSlideMenu();
        }

        if (DisplayUtils.hasCentralPane(this))
        {
            clearCentralPane();
        }

        switch (id)
        {
        /*
            case R.id.menu_browse_my_sites:
                if (!checkSession(R.id.menu_browse_my_sites)) { return; }
                frag = BrowserSitesFragment.newInstance();
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        BrowserSitesFragment.TAG, true);
                break;
         */
        case R.id.menu_browse_shared:
        {
            if (!checkSession(R.id.menu_browse_shared))
            {
                return;
            }
            final AlfrescoSession ses = getCurrentSession();

            if (ses instanceof OdsRepositorySession)
            {
                frag = ChildrenBrowserFragment.newInstance(((OdsRepositorySession) ses).getShared().getRootFolder());
                frag.setSession(SessionUtils.getSession(this));
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        ChildrenBrowserFragment.TAG, true);
            }
        }
        break;
        case R.id.menu_browse_global:
        {
            if (!checkSession(R.id.menu_browse_global))
            {
                return;
            }
            final AlfrescoSession ses = getCurrentSession();

            if (ses instanceof OdsRepositorySession)
            {
                frag = ChildrenBrowserFragment.newInstance(((OdsRepositorySession) ses).getGlobal().getRootFolder());
                frag.setSession(SessionUtils.getSession(this));
                FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                        ChildrenBrowserFragment.TAG, true);
            }
        }
        break;
        case R.id.menu_browse_root:
            if (!checkSession(R.id.menu_browse_root))
            {
                return;
            }
            frag = ChildrenBrowserFragment.newInstance(getCurrentSession().getRootFolder());
            frag.setSession(SessionUtils.getSession(this));
            FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                    ChildrenBrowserFragment.TAG, true);
            break;
            /*
        case R.id.menu_browse_activities:
            if (!checkSession(R.id.menu_browse_activities)) { return; }
            frag = ActivitiesFragment.newInstance();
            FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                    ActivitiesFragment.TAG, true);
            break;
        case R.id.menu_search:
            if (!checkSession(R.id.menu_search)) { return; }
            frag = KeywordSearch.newInstance();
            FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), KeywordSearch.TAG,
                    true);
            break;
        case R.id.menu_favorites:
                Fragment syncFrag = FavoritesSyncFragment.newInstance(ListingModeFragment.MODE_LISTING);
                FragmentDisplayer.replaceFragment(this, syncFrag, DisplayUtils.getLeftFragmentId(this),
                        FavoritesSyncFragment.TAG, true);
                break;
             */
        case R.id.menu_downloads:
            if (currentAccount == null)
            {
                MessengerManager.showLongToast(this, getString(R.string.loginfirst));
            } else
            {
                addLocalFileNavigationFragment();
            }
            break;
        case R.id.menu_notifications:
            if (currentAccount == null)
            {
                MessengerManager.showLongToast(this, getString(R.string.loginfirst));
            } else
            {
                startActivity(new Intent(IntentIntegrator.ACTION_DISPLAY_OPERATIONS).putExtra(
                        IntentIntegrator.EXTRA_ACCOUNT_ID, currentAccount.getId()));
            }
            break;
            /*
        case R.id.menu_prefs:
            displayPreferences();
            break;
             */
        case R.id.menu_about:
            displayAbout();
            break;
            /*
            case R.id.menu_help:
                UIUtils.displayHelp(this);
                break;
             */
        default:
            break;
        }
    }

    public void showMainMenuFragment(final View v)
    {
        DisplayUtils.hideLeftTitlePane(this);
        doMainMenuAction(v.getId());
    }

    // ///////////////////////////////////////////////////////////////////////////
    // SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void setSessionState(final int state)
    {
        sessionState = state;
    }

    public void setSessionErrorMessageId(final int messageId)
    {
        sessionState = SESSION_ERROR;
        sessionStateErrorMessageId = messageId;
    }

    private void checkSession()
    {
        if (accountManager == null || accountManager.isEmpty() && accountManager.hasData())
        {
            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();
            return;
        }
        else if (getCurrentAccount() == null && getCurrentSession() == null)
        {
            ActionManager.loadAccount(this, accountManager.getDefaultAccount());
        }
        invalidateOptionsMenu();
    }

    private boolean checkSession(final int actionMainMenuId)
    {
        switch (sessionState)
        {
        case SESSION_ERROR:
            final Bundle b = new Bundle();
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
            } else if (getCurrentAccount() != null && getCurrentAccount().getActivation() != null)
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
    @Override
    public void addNavigationFragment(final Folder f)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(f);
    }

    public void addNavigationFragment(final String path)
    {
        clearScreen();
        clearCentralPane();
        super.addBrowserFragment(path);
    }

    @Override
    public void addNavigationFragment(final Site s)
    {
        clearScreen();
        clearCentralPane();
        super.addNavigationFragment(s);
    }

    public void addLocalFileNavigationFragment()
    {
        final BaseFragment frag = FileExplorerMenuFragment.newInstance();
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                FileExplorerMenuFragment.TAG, true);
    }

    public void addLocalFileNavigationFragment(final File file)
    {
        final BaseFragment frag = FileExplorerFragment.newInstance(file);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), FileExplorerFragment.TAG,
                true);
    }

    public void addLocalFileNavigationFragment(final int mediaType)
    {
        final LibraryFragment frag = LibraryFragment.newInstance(mediaType);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this), LibraryFragment.TAG, true);
    }

    public void addPropertiesFragment(final Node n, final Folder parentFolder, final boolean forceBackStack)
    {
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        final BaseFragment frag = DetailsFragment.newInstance(n, parentFolder);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, forceBackStack);

    }

    public void addPropertiesFragment(final String nodeIdentifier)
    {
        final Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        clearCentralPane();
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(DetailsFragment.TAG);
        }
        final BaseFragment frag = DetailsFragment.newInstance(nodeIdentifier);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(), DetailsFragment.TAG, b);
    }

    public void addPropertiesFragment(final Node n)
    {
        final Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        addPropertiesFragment(n, getImportParent(), b);
    }

    public void addComments(final Node n)
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.push(CommentsFragment.TAG);
        }
        final BaseFragment frag = CommentsFragment.newInstance(n);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(true), CommentsFragment.TAG, true);
        ((View) findViewById(getFragmentPlace(true)).getParent()).setVisibility(View.VISIBLE);
    }

    public void addVersions(final Document d)
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.push(VersionFragment.TAG);
        }
        final BaseFragment frag = VersionFragment.newInstance(d);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, getFragmentPlace(true), VersionFragment.TAG, true);
        ((View) findViewById(getFragmentPlace(true)).getParent()).setVisibility(View.VISIBLE);
    }

    public void addAccountDetails(final long id)
    {
        final Boolean b = DisplayUtils.hasCentralPane(this) ? false : true;
        if (DisplayUtils.hasCentralPane(this))
        {
            stackCentral.clear();
            stackCentral.push(AccountsFragment.TAG);
        }
        final BaseFragment frag = AccountDetailsFragment.newInstance(id);
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getMainPaneId(this), AccountDetailsFragment.TAG, b);
    }

    public void displayAbout()
    {

        if (DisplayUtils.hasCentralPane(this))
        {
            final DialogFragment f = new AboutFragment();
            f.show(getFragmentManager(), AboutFragment.TAG);
        }
        else
        {
            final Fragment f = new AboutFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), AboutFragment.TAG, true);
        }
    }

    public void displayPreferences()
    {
        if (DisplayUtils.hasCentralPane(this))
        {
            final Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_SETTINGS);
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
            startActivity(i);
        }
        else
        {
            final Fragment f = new GeneralPreferences();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getMainPaneId(this), GeneralPreferences.TAG, true);
        }
    }

    public void displayMainMenu()
    {
        final Fragment f = new MainMenuFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), MainMenuFragment.TAG, false);
        hideSlideMenu();
    }

    public void displayAccounts()
    {
        final Fragment f = new AccountsFragment();
        FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), AccountsFragment.TAG, true);
    }

    public void displayNetworks()
    {
        if (getCurrentSession() instanceof CloudSession)
        {
            final Fragment f = new CloudNetworksFragment();
            FragmentDisplayer.replaceFragment(this, f, DisplayUtils.getLeftFragmentId(this), CloudNetworksFragment.TAG,
                    true);
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
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        if (isSlideMenuVisible() && !DisplayUtils.hasCentralPane(this))
        {
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

        if (isVisible(AccountsFragment.TAG) /*&& !isVisible(AccountTypesFragment.TAG)*/
                && !isVisible(AccountEditFragment.TAG) && !isVisible(AccountOAuthFragment.TAG))
        {
            AccountsFragment.getMenu(menu);
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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
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
            FragmentDisplayer.replaceFragment(this, new KeywordSearch(), getFragmentPlace(), KeywordSearch.TAG,
                    true);
            return true;

        case MenuActionItem.MENU_CREATE_FOLDER:
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                ((ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG)).createFolder();
            } else if (getFragment(FileExplorerFragment.TAG) != null)
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
            final DocumentTypesDialogFragment dialogft = DocumentTypesDialogFragment.newInstance(currentAccount,
                    fragmentTag);
            dialogft.show(getFragmentManager(), DocumentTypesDialogFragment.TAG);
            return true;

        case MenuActionItem.MENU_UPLOAD:
            if (getFragment(ChildrenBrowserFragment.TAG) != null)
            {
                final Intent i = new Intent(IntentIntegrator.ACTION_PICK_FILE, null, this, PublicDispatcherActivity.class);
                i.putExtra(PublicIntent.EXTRA_FOLDER,
                        StorageManager.getDownloadFolder(this, getCurrentAccount()));
                i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
                getFragment(ChildrenBrowserFragment.TAG).startActivityForResult(i,
                        PublicIntent.REQUESTCODE_FILEPICKER);
            }
            return true;
        case MenuActionItem.MENU_REFRESH:
            ((RefreshFragment) getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this)))
            .refresh();
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
                final Intent i = new Intent(IntentIntegrator.ACTION_PICK_FILE, null, this, PublicDispatcherActivity.class);
                i.putExtra(PublicIntent.EXTRA_FOLDER,
                        StorageManager.getDownloadFolder(this, getCurrentAccount()));
                i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, getCurrentAccount().getId());
                getFragment(DetailsFragment.TAG).startActivityForResult(i, PublicIntent.REQUESTCODE_FILEPICKER);
            }
            return true;
        case MenuActionItem.MENU_EDIT:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).edit();
            return true;
            /* case MenuActionItem.MENU_COMMENT:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).comment();
                return true;*/
        case MenuActionItem.MENU_VERSION_HISTORY:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).versions();
            return true;
            /* case MenuActionItem.MENU_TAGS:
                ((DetailsFragment) getFragment(DetailsFragment.TAG)).tags();
                return true;*/
        case MenuActionItem.MENU_DELETE:
            ((DetailsFragment) getFragment(DetailsFragment.TAG)).delete();
            return true;
        case MenuActionItem.MENU_SITE_LIST_REQUEST:
            ((BrowserSitesFragment) getFragment(BrowserSitesFragment.TAG)).displayJoinSiteRequests();
            return true;
        case MenuActionItem.ABOUT_ID:
            displayAbout();
            DisplayUtils.switchSingleOrTwo(this, true);
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
        } else if (getResources().getBoolean(R.bool.tablet_middle))
        {
            // Sepcific case where we want to display one or two panes.
            if (getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)) == null)
            {
                super.onBackPressed();
            } else
            {
                DisplayUtils.getLeftPane(this).setVisibility(View.VISIBLE);
                DisplayUtils.getCentralPane(this).setVisibility(View.GONE);

                final Fragment fr = getFragmentManager().findFragmentById(DisplayUtils.getLeftFragmentId(this));

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

                if (fr instanceof KeywordSearch)
                {
                    ((KeywordSearch) fr).unselect();
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

                // Special case : if Activities Fragment
                if (backStack)
                {
                    getFragmentManager().popBackStack();
                } else
                {
                    FragmentDisplayer.remove(this,
                            getFragmentManager().findFragmentById(DisplayUtils.getCentralFragmentId(this)), false);
                }
            }
        } else
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

    public void setCurrentNode(final Node currentNode)
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

        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            Log.d(TAG, intent.getAction());

            final Activity activity = MainActivity.this;

            if (IntentIntegrator.ACTION_DECRYPT_ALL_COMPLETED.equals(intent.getAction())
                    || IntentIntegrator.ACTION_ENCRYPT_ALL_COMPLETED.equals(intent.getAction()))
            {
                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT.equals(intent.getAction())
                    || IntentIntegrator.ACTION_RELOAD_ACCOUNT.equals(intent.getAction()))
            {
                // Change activity state to loading.
                setSessionState(SESSION_LOADING);

                if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    return;
                }

                // Assign the account
                currentAccount = AccountManager.retrieveAccount(context,
                        intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));

                if (getFragment(MainMenuFragment.TAG) != null)
                {
                    //((MainMenuFragment)getFragment(MainMenuFragment.TAG)).displayFavoriteStatut();
                    ((MainMenuFragment)getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
                {
                    //((MainMenuFragment)getFragment(MainMenuFragment.SLIDING_TAG)).displayFavoriteStatut();
                    ((MainMenuFragment)getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                // Return to root screen
                if(getFragment(UploadFormFragment.TAG) == null)
                {
                    try
                    {
                        activity.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }catch(final Exception e){
                        Log.e("", "ERROR "+e.getMessage());
                    }
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
                    final Account acc = AccountManager.retrieveAccount(context,
                            intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                    MessengerManager.showLongToast(activity, acc.getDescription());
                }
                return;
            }

            if (IntentIntegrator.ACTION_LOAD_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent))
                {
                    return;
                }

                setSessionState(SESSION_ACTIVE);
                setProgressBarIndeterminateVisibility(false);

                if (getCurrentSession() instanceof RepositorySession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, false);
                } else if (getCurrentSession() instanceof CloudSession)
                {
                    DisplayUtils.switchSingleOrTwo(activity, true);
                }

                // Remove OAuthFragment if one
                if (getFragment(AccountOAuthFragment.TAG) != null)
                {
                    getFragmentManager().popBackStack(AccountOAuthFragment.TAG,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                if (getFragment(WaitingDialogFragment.TAG) != null)
                {
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                }

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
                } else
                {
                    OAuthRefreshTokenCallback.removeLastCloudLoadingTime(activity);
                }

                // NB : temporary code ?
                // Check to see if we have an old account that needs its paid
                // network flag setting.
                if (!currentAccount.getIsPaidAccount())
                {
                    boolean paidNetwork = false;
                    if (getCurrentSession() instanceof CloudSession)
                    {
                        paidNetwork = ((CloudSession) getCurrentSession()).getNetwork().isPaidNetwork();
                    } else
                    {
                        paidNetwork = getCurrentSession().getRepositoryInfo().getEdition()
                                .equals(OnPremiseConstant.ALFRESCO_EDITION_ENTERPRISE);
                    }

                    if (paidNetwork)
                    {
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                        prefs.edit().putBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, true).commit();

                        DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                                DataProtectionUserDialogFragment.TAG);

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
                    //((MainMenuFragment)getFragment(MainMenuFragment.TAG)).displayFavoriteStatut();
                    ((MainMenuFragment)getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                if (getFragment(MainMenuFragment.SLIDING_TAG) != null)
                {
                    //((MainMenuFragment)getFragment(MainMenuFragment.SLIDING_TAG)).displayFavoriteStatut();
                    ((MainMenuFragment)getFragment(MainMenuFragment.TAG)).updateFolderAccess();
                }

                return;
            }

            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()))
            {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                final Account tmpAccount = AccountManager.retrieveAccount(activity, intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                if (tmpAccount.getIsPaidAccount()
                        && !prefs.getBoolean(GeneralPreferences.HAS_ACCESSED_PAID_SERVICES, false))
                {
                    // Check if we've prompted the user for Data Protection yet.
                    // This is needed on new account creation, as the Activity gets
                    // re-created after the account is created.
                    DataProtectionUserDialogFragment.newInstance(true).show(getFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);

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
                    currentAccount = AccountManager.retrieveAccount(context,
                            intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                    applicationManager.removeAccount(currentAccount.getId());
                }

                // Stop progress indication
                activity.setProgressBarIndeterminateVisibility(false);
                return;
            }

            if (IntentIntegrator.ACTION_ACCOUNT_INACTIVE.equals(intent.getAction()))
            {
                if (!isCurrentAccountToLoad(intent))
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
                final Long accountId = intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
                final Account acc = AccountManager.retrieveAccount(activity, accountId);

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH)
                        && getFragment(AccountOAuthFragment.TAG) == null
                        || getFragment(AccountOAuthFragment.TAG) != null && getFragment(AccountOAuthFragment.TAG)
                        .isAdded())
                {
                    final AccountOAuthFragment newFragment = AccountOAuthFragment.newInstance(acc);
                    FragmentDisplayer.replaceFragment(activity, newFragment, DisplayUtils.getMainPaneId(activity),
                            AccountOAuthFragment.TAG, true);
                    DisplayUtils.switchSingleOrTwo(activity, true);
                    return;
                }

                if (intent.getCategories().contains(IntentIntegrator.CATEGORY_OAUTH_REFRESH))
                {
                    getLoaderManager().restartLoader(OAuthRefreshTokenLoader.ID, null,
                            new OAuthRefreshTokenCallback(activity, acc, (CloudSession) getCurrentSession()));
                    return;
                }
                return;
            }

            return;
        }
    }

    // Due to dropdown the account loaded might not be the last one to load.
    private boolean isCurrentAccountToLoad(final Intent intent)
    {
        if (currentAccount == null)
        {
            return false;
        }
        if (!intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
        {
            return false;
        }
        return currentAccount.getId() == intent.getExtras().getLong(IntentIntegrator.EXTRA_ACCOUNT_ID);
    }
}
