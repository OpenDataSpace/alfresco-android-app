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
package org.opendataspace.android.app.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.activity.PublicDispatcherActivity;
import org.opendataspace.android.app.fragments.WaitingDialogFragment;
import org.opendataspace.android.app.intent.IntentIntegrator;
import org.opendataspace.android.app.operations.batch.account.CreateAccountRequest;
import org.opendataspace.android.app.security.DataProtectionManager;
import org.opendataspace.android.app.utils.SessionUtils;
import org.opendataspace.android.app.utils.thirdparty.LocalBroadcastManager;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

public class ActionManager extends org.alfresco.mobile.android.ui.manager.ActionManager
{
    public static final String TAG = ActionManager.class.getName();

    public static void actionOpenIn(final Fragment fr, final File myFile)
    {
        try
        {
            final String mimeType = MimeTypeManager.getMIMEType(myFile.getName());
            if (DataProtectionManager.getInstance(fr.getActivity()).isEncrypted(myFile.getPath()))
            {
                final WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        R.string.decryption_title, true);
                dialog.show(fr.getActivity().getFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(fr.getActivity()).decrypt(SessionUtils.getAccount(fr.getActivity()),
                        myFile, DataProtectionManager.ACTION_COPY);
            } else
                actionView(fr.getActivity(), myFile, mimeType, null);
        }
        catch (final Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION VIEW
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Open a local file with a 3rd party application. Manage automatically with
     * Data Protection.
     * 
     * @param fr
     * @param myFile
     * @param listener
     */
    public static void actionView(final Fragment fr, final File myFile, final ActionManagerListener listener)
    {
        try
        {
            final String mimeType = MimeTypeManager.getMIMEType(myFile.getName());
            if (DataProtectionManager.getInstance(fr.getActivity()).isEncrypted(myFile.getPath()))
            {
                final WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(R.string.data_protection,
                        R.string.decryption_title, true);
                dialog.show(fr.getActivity().getFragmentManager(), WaitingDialogFragment.TAG);
                DataProtectionManager.getInstance(fr.getActivity()).decrypt(SessionUtils.getAccount(fr.getActivity()),
                        myFile, DataProtectionManager.ACTION_VIEW);
            } else
                actionView(fr.getActivity(), myFile, mimeType, listener);
        }
        catch (final Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    public static Intent createViewIntent(final Activity activity, final File contentFile)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri data = Uri.fromFile(contentFile);
        intent.setDataAndType(data, MimeTypeManager.getMIMEType(contentFile.getName()).toLowerCase());
        return intent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ERRORS & DIALOG
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionDisplayDialog(final Context context, final Bundle bundle)
    {
        final String intentId = IntentIntegrator.ACTION_DISPLAY_DIALOG;
        final Intent i = new Intent(intentId);
        if (bundle != null)
            i.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    public static void actionDisplayCertifDialog(final String info)
    {
        final Context c = BaseActivity.getCurrentContext();
        if(c == null)
            return;
        final String intentId = IntentIntegrator.ACTION_DISPLAY_CERTIFICATE;
        final Intent i = new Intent(intentId);
        if(info != null)
            i.putExtra(IntentIntegrator.EXTRA_CERTIFICATE_INFO, info);
        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    public static void actionDisplayError(final Fragment f, final Exception e)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_DISPLAY_ERROR);
        if (e != null)
            i.putExtra(IntentIntegrator.EXTRA_ERROR_DATA, e);
        LocalBroadcastManager.getInstance(f.getActivity()).sendBroadcast(i);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PDF
    // ///////////////////////////////////////////////////////////////////////////
    public static boolean launchPDF(final Context c, final String pdfFile)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(pdfFile)), "application/pdf");

        final PackageManager pm = c.getPackageManager();
        final List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        if (activities.size() > 0)
            c.startActivity(intent);
        else
            return false;

        return true;
    }

    public static void getAdobeReader(final Context c)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.adobe.reader"));
        c.startActivity(intent);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PLAY STORE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Open Play Store application or its web version if no play store
     * available.
     * 
     * @param c : Android Context
     */
    public static void actionDisplayPlayStore(final Context c)
    {
        // Retrieve list of application that understand market Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=org.opendataspace.android.app"));
        final PackageManager mgr = c.getPackageManager();
        final List<ResolveInfo> list = mgr.queryIntentActivities(intent, 0);

        // By default we redirect to the webbrowser version of play store.
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/"));

        for (final ResolveInfo resolveInfo : list)
            // If we find something related to android we open the application
            // version of play store.
            if (resolveInfo.activityInfo.applicationInfo.packageName.contains("android"))
            {
                intent.setComponent(new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                        resolveInfo.activityInfo.name));
                intent.setData(Uri.parse("market://"));
                break;
            }
        c.startActivity(intent);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION SEND / SHARE
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionSendDocument(final Fragment fr, final File myFile)
    {
        actionSend(fr.getActivity(), myFile);
    }

    public static void actionSend(final Activity activity, final File contentFile)
    {
        try
        {
            final Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
            activity.startActivity(Intent.createChooser(i, activity.getText(R.string.share_content)));
        }
        catch (final ActivityNotFoundException e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_share_content);
        }
    }

    public static Intent createSendIntent(final Activity activity, final File contentFile)
    {
        final Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
        i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
        return i;
    }

    public static boolean actionSendMailWithAttachment(final Fragment fr, final String subject, final String content, final Uri attachment,
            final int requestCode)
    {
        try
        {
            final Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(content));
            i.putExtra(Intent.EXTRA_STREAM, attachment);
            i.setType("text/plain");
            fr.startActivityForResult(Intent.createChooser(i, fr.getString(R.string.send_email)), requestCode);

            return true;
        }
        catch (final Exception e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.decryption_failed);
            Log.d(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    public static boolean actionSendMailWithLink(final Context c, final String subject, final String content, final Uri link)
    {
        final Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, content);
        i.setType("text/plain");
        c.startActivity(Intent.createChooser(i, String.format(c.getString(R.string.send_email), link.toString())));

        return true;
    }

    public static void actionSendDocumentToAlfresco(final Activity activity, final File file)
    {
        try
        {
            if (DataProtectionManager.getInstance(activity).isEncryptionEnable())
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), file,
                        DataProtectionManager.ACTION_SEND_ALFRESCO);
            else
                actionSendFileToAlfresco(activity, file);
        }
        catch (final Exception e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_open_file);
        }
    }

    public static void actionSendFileToAlfresco(final Activity activity, final File contentFile)
    {
        try
        {
            activity.startActivity(createSendFileToAlfrescoIntent(activity, contentFile));
        }
        catch (final ActivityNotFoundException e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_share_content);
        }
    }

    public static Intent createSendFileToAlfrescoIntent(final Activity activity, final File contentFile)
    {
        final Intent i = new Intent(activity, PublicDispatcherActivity.class);
        i.setAction(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
        i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
        return i;
    }

    public static void actionShareContent(final Activity activity, final File myFile)
    {
        try
        {
            if (DataProtectionManager.getInstance(activity).isEncrypted(myFile.getPath()))
                DataProtectionManager.getInstance(activity).decrypt(SessionUtils.getAccount(activity), myFile,
                        DataProtectionManager.ACTION_SEND);
            else
                actionSend(activity, myFile);
        }
        catch (final Exception e)
        {
            MessengerManager.showToast(activity, R.string.error_unable_open_file);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACTION SEND MULTIPLE
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionSendDocumentsToAlfresco(final Fragment fr, final List<File> files)
    {
        if (files.size() == 1)
        {
            actionSendDocumentToAlfresco(fr.getActivity(), files.get(0));
            return;
        }

        try
        {
            final Intent i = new Intent(fr.getActivity(), PublicDispatcherActivity.class);
            i.setAction(Intent.ACTION_SEND_MULTIPLE);
            final ArrayList<Uri> uris = new ArrayList<Uri>();
            // convert from paths to Android friendly Parcelable Uri's
            for (final File file : files)
            {
                final Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            i.setType(MimeTypeManager.getMIMEType("text/plain"));
            fr.getActivity().startActivity(i);
        }
        catch (final ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    public static void actionSendDocuments(final Fragment fr, final List<File> files)
    {
        if (files.size() == 1)
        {
            actionSendDocument(fr, files.get(0));
            return;
        }

        try
        {
            final Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            final ArrayList<Uri> uris = new ArrayList<Uri>();
            // convert from paths to Android friendly Parcelable Uri's
            for (final File file : files)
            {
                final Uri u = Uri.fromFile(file);
                uris.add(u);
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            i.setType(MimeTypeManager.getMIMEType("text/plain"));
            fr.getActivity().startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)));
        }
        catch (final ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PICK FILE
    // ///////////////////////////////////////////////////////////////////////////
    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionPickFile(final Fragment f, final int requestCode)
    {
        try
        {
            final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("*/*");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            f.startActivityForResult(Intent.createChooser(i, f.getText(R.string.content_app_pick_file)), requestCode);
        }
        catch (final ActivityNotFoundException e)
        {
            MessengerManager.showToast(f.getActivity(), R.string.error_unable_open_file);
        }
    }
    // ///////////////////////////////////////////////////////////////////////////
    // AUTHENTICATION
    // ///////////////////////////////////////////////////////////////////////////
    public static void actionRequestUserAuthentication(final Context activity, final Account account)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH);
        if (account != null)
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void actionRequestAuthentication(final Context activity, final Account account)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_USER_AUTHENTICATION);
        i.addCategory(IntentIntegrator.CATEGORY_OAUTH_REFRESH);
        if (account != null)
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    public static void reloadAccount(final Activity activity, final Account account, final String networkId)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
            i.putExtra(IntentIntegrator.EXTRA_NETWORK_ID, networkId);
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void reloadAccount(final Activity activity, final Account account)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_RELOAD_ACCOUNT);
        if (account != null)
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void loadAccount(final Activity activity, final Account account)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        if (account != null)
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void loadAccount(final Activity activity, final Account account, final OAuthData data)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_LOAD_ACCOUNT);
        if (account != null)
        {
            i.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, account.getId());
            i.putExtra(IntentIntegrator.EXTRA_OAUTH_DATA, data);
        }
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }

    public static void createAccount(final Activity activity, final CreateAccountRequest request)
    {
        final Intent i = new Intent(IntentIntegrator.ACTION_CREATE_ACCOUNT);
        i.putExtra(IntentIntegrator.EXTRA_CREATE_REQUEST, request);
        LocalBroadcastManager.getInstance(activity).sendBroadcast(i);
    }
}
