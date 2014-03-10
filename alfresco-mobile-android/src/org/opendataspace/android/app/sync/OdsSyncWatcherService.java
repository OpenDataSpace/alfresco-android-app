package org.opendataspace.android.app.sync;

import java.util.ArrayList;

import org.alfresco.mobile.android.application.preferences.GeneralPreferences;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class OdsSyncWatcherService extends Service
{
    private class PrefListenner implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            if (key == GeneralPreferences.ODS_SYNCHONISATION)
                updateWatching(prefs);
        }
    }

    private final ArrayList<FileObserver> monitors = new ArrayList<FileObserver>();
    private boolean watching = false;
    private final PrefListenner prefListener = new PrefListenner();
    private PendingIntent pending = null;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        regiterObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        regiterObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        regiterObserver(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateWatching(prefs);
        prefs.registerOnSharedPreferenceChangeListener(prefListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(prefListener);
        setWatching(false);
        super.onDestroy();
    }

    private void regiterObserver(String path)
    {
        FileObserver fo = new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE |
                FileObserver.MODIFY | FileObserver.MOVED_FROM | FileObserver.MOVED_TO)
        {
            @Override
            public void onEvent(int event, String path)
            {
                if (watching)
                    OdsSyncReceiver.reschedule(OdsSyncWatcherService.this, pending);
            }
        };

        monitors.add(fo);
    }

    public void setWatching(boolean val)
    {
        if (watching == val)
            return;

        watching = val;

        for (FileObserver cur : monitors)
            if (val)
                cur.startWatching();
            else
                cur.stopWatching();

        if (!watching)
            pending.cancel();
        else
            pending = OdsSyncReceiver.getPendingInetent(this);
    }

    private void updateWatching(SharedPreferences prefs)
    {
        String id = prefs.getString(GeneralPreferences.ODS_SYNCHONISATION, "");
        setWatching(id != null && !"".equals(id));
    }
}
