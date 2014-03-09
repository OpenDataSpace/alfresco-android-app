package org.opendataspace.android.app.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OdsSyncReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED)
            startWatcher(context);
    }

    public static void startWatcher(Context context)
    {
        Intent i = new Intent(OdsSyncWatcherService.class.getCanonicalName());
        i.setClass(context, OdsSyncWatcherService.class);
        context.startService(i);
    }
}
