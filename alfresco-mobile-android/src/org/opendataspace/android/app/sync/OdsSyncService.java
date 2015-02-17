package org.opendataspace.android.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OdsSyncService extends Service
{
    private OdsSyncAdapter adp;

    @Override
    public void onCreate()
    {
        synchronized (this)
        {
            if (adp == null)
            {
                adp = new OdsSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return adp.getSyncAdapterBinder();
    }
}
