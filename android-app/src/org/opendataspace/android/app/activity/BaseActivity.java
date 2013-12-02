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
package org.opendataspace.android.app.activity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Site;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.opendataspace.android.app.ApplicationManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.accounts.AccountManager;
import org.opendataspace.android.app.exception.AlfrescoAppException;
import org.opendataspace.android.app.exception.CloudExceptionUtils;
import org.opendataspace.android.app.fragments.DisplayUtils;
import org.opendataspace.android.app.fragments.FragmentDisplayer;
import org.opendataspace.android.app.fragments.SimpleAlertDialogFragment;
import org.opendataspace.android.app.fragments.WaitingDialogFragment;
import org.opendataspace.android.app.fragments.browser.ChildrenBrowserFragment;
import org.opendataspace.android.app.intent.IntentIntegrator;
import org.opendataspace.android.app.manager.NetworkHttpInvoker;
import org.opendataspace.android.app.manager.RenditionManager;
import org.opendataspace.android.app.preferences.GeneralPreferences;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.app.utils.thirdparty.LocalBroadcastManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

/**
 * Base class for all activities.
 * 
 * @author Jean Marie Pascal
 */
public abstract class BaseActivity extends Activity
{
    protected AccountManager accountManager;

    protected LocalBroadcastManager broadcastManager;

    protected ApplicationManager applicationManager;

    protected BroadcastReceiver receiver;

    protected BroadcastReceiver utilsReceiver;

    protected List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>(2);

    protected Account currentAccount;

    protected RenditionManager renditionManager;

    private static Context mContext = null;

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        broadcastManager = LocalBroadcastManager.getInstance(this);
        applicationManager = ApplicationManager.getInstance(this);
        accountManager = applicationManager.getAccountManager();

        final IntentFilter filters = new IntentFilter();
        filters.addAction(IntentIntegrator.ACTION_DISPLAY_DIALOG);
        filters.addAction(IntentIntegrator.ACTION_DISPLAY_ERROR);
        filters.addAction(IntentIntegrator.ACTION_DISPLAY_CERTIFICATE);

        utilsReceiver = new UtilsReceiver();
        broadcastManager.registerReceiver(utilsReceiver, filters);
    }

    @Override
    protected void onStart()
    {
        if (accountManager == null)
            accountManager = applicationManager.getAccountManager();
        if (applicationManager == null)
        {
            applicationManager = ApplicationManager.getInstance(this);
            applicationManager.setAccountManager(accountManager);
        }
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        for (final BroadcastReceiver bReceiver : receivers)
            broadcastManager.unregisterReceiver(bReceiver);
        receivers.clear();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // UTILS
    // ///////////////////////////////////////////////////////////////////////////
    public Fragment getFragment(final String tag)
    {
        return getFragmentManager().findFragmentByTag(tag);
    }

    protected int getFragmentPlace()
    {
        int id = R.id.left_pane_body;
        if (DisplayUtils.hasCentralPane(this))
            id = R.id.central_pane_body;
        return id;
    }

    protected int getFragmentPlace(final boolean forceRight)
    {
        int id = R.id.left_pane_body;
        if (forceRight && DisplayUtils.hasCentralPane(this))
            id = R.id.central_pane_body;
        return id;
    }

    protected boolean isVisible(final String tag)
    {
        return getFragmentManager().findFragmentByTag(tag) != null
                && getFragmentManager().findFragmentByTag(tag).isAdded();
    }

    public void displayWaitingDialog()
    {
        if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) == null)
            new WaitingDialogFragment().show(getFragmentManager(), WaitingDialogFragment.TAG);
    }

    public void removeWaitingDialog()
    {
        if (getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG) != null)
            ((WaitingDialogFragment) getFragmentManager().findFragmentByTag(WaitingDialogFragment.TAG)).dismiss();
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNTS / SESSION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public void setCurrentAccount(final Account account)
    {
        this.currentAccount = account;
    }

    public void setCurrentAccount(final long accountId)
    {
        this.currentAccount = AccountManager.retrieveAccount(this, accountId);
    }

    public Account getCurrentAccount()
    {
        return currentAccount;
    }

    public AlfrescoSession getCurrentSession()
    {
        if (currentAccount == null)
            currentAccount = applicationManager.getCurrentAccount();

        return currentAccount != null ? applicationManager.getSession(currentAccount.getId()) : null;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // MANAGERS
    // ///////////////////////////////////////////////////////////////////////////
    public RenditionManager getRenditionManager()
    {
        return renditionManager;
    }

    public void setRenditionManager(final RenditionManager renditionManager)
    {
        this.renditionManager = renditionManager;
    }

    public AccountManager getAccountManager()
    {
        return accountManager;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // FRAGMENT UTILITY
    // ///////////////////////////////////////////////////////////////////////////
    public void addBrowserFragment(final String path)
    {
        if (path == null)
            return;

        final ChildrenBrowserFragment mFragment = (ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG);
        if (mFragment != null && path.equals(mFragment.getParent().getPropertyValue(PropertyIds.PATH)))
            return;

        final BaseFragment frag = ChildrenBrowserFragment.newInstance(path);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(final Folder f)
    {
        if (f == null)
            return;

        final ChildrenBrowserFragment mFragment = (ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG);
        if (mFragment != null && f.getIdentifier().equals(mFragment.getParent().getIdentifier()))
            return;

        final BaseFragment frag = ChildrenBrowserFragment.newInstance(f);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(final Site site, final Folder f)
    {
        if (f == null)
            return;
        if (site == null)
        {
            addNavigationFragment(f);
            return;
        }

        final ChildrenBrowserFragment mFragment = (ChildrenBrowserFragment) getFragment(ChildrenBrowserFragment.TAG);
        if (mFragment != null && f.getIdentifier().equals(mFragment.getParent().getIdentifier()))
            return;

        final BaseFragment frag = ChildrenBrowserFragment.newInstance(site, f);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    public void addNavigationFragment(final Site s)
    {
        final BaseFragment frag = ChildrenBrowserFragment.newInstance(s);
        frag.setSession(SessionUtils.getSession(this));
        FragmentDisplayer.replaceFragment(this, frag, DisplayUtils.getLeftFragmentId(this),
                ChildrenBrowserFragment.TAG, true);
    }

    // ////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////
    /**
     * Register a broadcast receiver to this specific activity. If used this
     * methods is responsible to unregister the receiver during on stop().
     * 
     * @param receiver
     * @param filter
     */
    public void registerPrivateReceiver(final BroadcastReceiver receiver, final IntentFilter filter)
    {
        if (receiver != null && filter != null)
        {
            broadcastManager.registerReceiver(receiver, filter);
            receivers.add(receiver);
        }
    }

    /**
     * Utility BroadcastReceiver for displaying dialog after an error or to
     * display custom message. Use ACTION_DISPLAY_DIALOG or ACTION_DISPLAY_ERROR
     * Action inside an Intent and send it with localBroadcastManager instance.
     * 
     * @author Jean Marie Pascal
     */
    private class UtilsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            final Activity activity = BaseActivity.this;

            if (activity.isFinishing() || activity.isChangingConfigurations())
                return;

            Log.d("UtilsReceiver", intent.getAction());

            //
            if (IntentIntegrator.ACTION_DISPLAY_DIALOG.equals(intent.getAction()))
            {
                removeWaitingDialog();

                SimpleAlertDialogFragment.newInstance(intent.getExtras()).show(activity.getFragmentManager(),
                        SimpleAlertDialogFragment.TAG);
                return;
            }

            // Intent for Display Errors
            if (IntentIntegrator.ACTION_DISPLAY_ERROR.equals(intent.getAction()))
            {
                if (getFragment(WaitingDialogFragment.TAG) != null)
                    ((DialogFragment) getFragment(WaitingDialogFragment.TAG)).dismiss();
                final Exception e = (Exception) intent.getExtras().getSerializable(IntentIntegrator.EXTRA_ERROR_DATA);

                String errorMessage = getString(R.string.error_general);
                if (e instanceof AlfrescoAppException && ((AlfrescoAppException) e).isDisplayMessage())
                    errorMessage = e.getMessage();

                MessengerManager.showLongToast(activity, errorMessage);

                CloudExceptionUtils.handleCloudException(activity, e, false);

                return;
            }

            if(IntentIntegrator.ACTION_DISPLAY_CERTIFICATE.equals(intent.getAction())){

                final String info = intent.getStringExtra(IntentIntegrator.EXTRA_CERTIFICATE_INFO);
                String msg  = getResources().getString(R.string.msg_dlg_certificate);
                if(info != null)
                    msg = msg.concat("\n" + info);

                final AlertDialog dlg = new AlertDialog.Builder(BaseActivity.this)
                .setIcon(R.drawable.ic_alfresco_logo)
                .setTitle(R.string.title_dlg_certificate)
                .setMessage(Html.fromHtml(msg))
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        GeneralPreferences.setCertificatePref(1,BaseActivity.this);
                        NetworkHttpInvoker.onSetSertf();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int whichButton) {
                        GeneralPreferences.setCertificatePref(0,BaseActivity.this);
                        NetworkHttpInvoker.onSetSertf();
                        dialog.dismiss();
                    }
                })
                .create();
                dlg.show();
                return;
            }
        }
    }

    public static Context getCurrentContext(){
        return mContext;
    }
}
