package org.opendataspace.android.app.sync;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.operations.sync.SynchroManager;
import org.opendataspace.android.app.account.OdsAccountAuthenticator;

import android.content.ContentResolver;
import android.content.Context;

public class OdsSyncManager extends SynchroManager
{
    public OdsSyncManager(Context applicationContext)
    {
        super(applicationContext);
    }

    @Override
    public boolean hasActivateSync(Account account)
    {
        return ContentResolver.getIsSyncable(new android.accounts.Account(account.getDescription(),
                OdsAccountAuthenticator.ACCOUNT_TYPE), "org.opendataspace.android.app.provider.sync") > 0;
    }
}
