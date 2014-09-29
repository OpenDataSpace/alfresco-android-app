package org.opendataspace.android.app.config;

import java.io.File;
import java.io.Serializable;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.opendataspace.android.app.operations.OdsConfigContext;
import org.opendataspace.android.app.operations.OdsConfigRequest;
import org.opendataspace.android.ui.logging.OdsLog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class OdsConfigManager
{
    private static final Object LOCK = new Object();

    private static OdsConfigManager mInstance;

    public static final String BRAND_ICON = "ic_logo.png";

    public static final String BRAND_LARGE = "logo_large.png";

    public static final String BRAND_NOTIF = "ic_notif.png";

    public static final String BRAND_STUB = "bg_logo_light.9.png";

    public static final String[] FILES = new String[] {BRAND_ICON, BRAND_LARGE, BRAND_NOTIF, BRAND_STUB};

    private OdsConfigManager(Context applicationContext)
    {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(new ConfigurationReceiver(),
                new IntentFilter(IntentIntegrator.ACTION_CONFIGURATION_COMPLETED));
    }

    public static OdsConfigManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = new OdsConfigManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    public void retrieveConfiguration(Activity activity, Account acc)
    {
        if (hasConfig(activity, acc))
        {
            notifyRebrand(activity, acc.getId());
            return;
        }

        OperationsRequestGroup group = new OperationsRequestGroup(activity, SessionUtils.getAccount(activity));
        group.enqueue(new OdsConfigRequest().setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
        BatchOperationManager.getInstance(activity).enqueue(group);
    }

    private boolean hasConfig(Context ctx, Account acc)
    {
        /*
        for (String cur : FILES)
        {
            File f = getBrandingFile(ctx, cur, acc);
            if (f != null && f.exists())
            {
                return true;
            }
        }
         */

        return false;
    }

    public static File getBrandingFile(Context ctx, String name, Account acc)
    {
        File folder = null;

        try
        {
            if (StorageManager.isExternalStorageAccessible() && acc != null)
            {
                folder = new File(IOUtils.createFolder(ctx.getExternalFilesDir(null).getParentFile(),
                        "brand" + File.separator + StorageManager.getAccountFolder(acc.getUrl(), acc.getUsername())), name);
            }
        }
        catch (Exception ex)
        {
            OdsLog.exw("brand", ex);
        }

        return folder;
    }

    private void notifyRebrand(Context context, long accId)
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CONFIGURATION_BRAND);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, accId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    public class ConfigurationReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                if (intent != null && intent.getExtras() != null
                        && intent.getExtras().containsKey(IntentIntegrator.EXTRA_DATA))
                {
                    Bundle b = intent.getExtras().getBundle(IntentIntegrator.EXTRA_DATA);
                    if (b != null && b.containsKey(IntentIntegrator.EXTRA_CONFIGURATION))
                    {
                        Serializable ctx = b.getSerializable(IntentIntegrator.EXTRA_CONFIGURATION);

                        if (ctx instanceof OdsConfigContext && ((OdsConfigContext) ctx).isUpdated())
                        {
                            OdsConfigManager.this.notifyRebrand(context, b.getLong(IntentIntegrator.EXTRA_ACCOUNT_ID));
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // nothing
            }
        }
    }

    public Drawable getBrandingDrawable(Context ctx, String name, Account acc)
    {
        File f = getBrandingFile(ctx, name, acc);
        return (f != null && f.exists()) ? Drawable.createFromPath(f.getAbsolutePath()) : null;
    }
}
