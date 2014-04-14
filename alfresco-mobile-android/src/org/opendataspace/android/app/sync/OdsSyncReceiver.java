package org.opendataspace.android.app.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.commons.utils.ConnectivityUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateUtils;

public class OdsSyncReceiver extends BroadcastReceiver
{
    private static final String SYNC_START = "org.opendataspace.android.app.sync.SYNC_START";

    private static final String[] SYNC_SOURCES =
        {Environment.DIRECTORY_PICTURES, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_DCIM};

    private static final String[] SYNC_DICM = {"100MEDIA", "100ANDRO", "100LGDSC", "100SHARP", "Camera"};

    @Override
    public void onReceive(Context context, Intent intent)
    {
        final String action = intent.getAction();

        if (action == Intent.ACTION_BOOT_COMPLETED)
        {
            startWatcher(context);
        } else if (action == SYNC_START)
        {
            if (!ConnectivityUtils.isWifiAvailable(context))
            {
                reschedule(context, getPendingInetent(context));
            } else
            {
                startWorker(context);
            }
        }
    }

    public static void startWatcher(Context context)
    {
        final Intent i = new Intent(OdsSyncWatcherService.class.getCanonicalName());
        i.setClass(context, OdsSyncWatcherService.class);
        context.startService(i);
    }

    public static void startWorker(Context context)
    {
        final Intent i = new Intent(OdsSyncWorkerService.class.getCanonicalName());
        i.setClass(context, OdsSyncWorkerService.class);
        context.startService(i);
    }

    public static void reschedule(Context context, PendingIntent intent)
    {
        final AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15 * DateUtils.MINUTE_IN_MILLIS, intent);
    }

    public static PendingIntent getPendingInetent(Context context)
    {
        final Intent in = new Intent(context, OdsSyncReceiver.class);
        in.setAction(OdsSyncReceiver.SYNC_START);
        return PendingIntent.getBroadcast(context, 0, in, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static List<File> getSources()
    {
        ArrayList<File> src = new ArrayList<File>();

        for (String cur : SYNC_SOURCES)
        {
            File f = Environment.getExternalStoragePublicDirectory(cur);

            if (cur == Environment.DIRECTORY_DCIM)
            {
                for (String dicm : SYNC_DICM)
                {
                    File tmp = new File(f, dicm);

                    if (tmp.exists() && tmp.isDirectory())
                    {
                        f = tmp;
                        break;
                    }
                }
            }

            if (f.exists() && f.isDirectory())
            {
                src.add(f);
            }
        }

        return src;
    }
}
