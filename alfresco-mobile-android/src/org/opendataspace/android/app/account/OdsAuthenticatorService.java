package org.opendataspace.android.app.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class OdsAuthenticatorService extends Service
{
    private OdsAccountAuthenticator auth;

    @Override
    public void onCreate()
    {
        auth = new OdsAccountAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return auth.getIBinder();
    }
}
