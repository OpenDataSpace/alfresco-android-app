package org.opendataspace.android.app.sync;

import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.sync.SyncPrepareRequest;
import org.opendataspace.android.ui.logging.OdsLog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

public class OdsSyncAdapter extends AbstractThreadedSyncAdapter
{
    public OdsSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
    }

    public OdsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
            SyncResult syncResult)
    {
        try
        {
            AccountManager am = AccountManager.get(getContext());
            org.alfresco.mobile.android.application.accounts.Account acc = org.alfresco.mobile.android.application.accounts.AccountManager
                    .retrieveAccount(getContext(), Long.valueOf(am.getPassword(account)));

            if (acc == null)
            {
                return;
            }

            Context context = getContext().getApplicationContext();
            OperationsRequestGroup group = new OperationsRequestGroup(context, acc);
            group.enqueue(new SyncPrepareRequest().setNotificationVisibility(OperationRequest.VISIBILITY_HIDDEN));
            BatchOperationManager.getInstance(context).enqueue(group);
        }
        catch (Exception ex)
        {
            OdsLog.ex("OdsSyncAdapter", ex);
        }
    }
}
