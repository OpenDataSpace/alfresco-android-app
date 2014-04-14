package org.opendataspace.android.app.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.impl.ContentFileImpl;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.application.ApplicationManager;
import org.alfresco.mobile.android.application.operations.batch.account.LoadSessionHelper;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.opendataspace.android.ui.logging.OdsLog;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

public class OdsSyncWorkerService extends IntentService
{
    private static final String TAG = OdsSyncWorkerService.class.getCanonicalName();

    public OdsSyncWorkerService()
    {
        super(OdsSyncWorkerService.class.getCanonicalName());
        setIntentRedelivery(true);
    }

    private static volatile PowerManager.WakeLock lockStatic = null;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null)
        {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, OdsSyncWorkerService.class.getCanonicalName());
            lockStatic.setReferenceCounted(true);
        }

        return lockStatic;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager.WakeLock lock = getLock(getApplicationContext());

        if (!lock.isHeld() || (flags & START_FLAG_REDELIVERY) != 0)
        {
            lock.acquire();
        }

        super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try
        {
            performSync();
        }
        finally
        {
            PowerManager.WakeLock lock = getLock(this.getApplicationContext());

            if (lock.isHeld())
            {
                lock.release();
            }
        }
    }

    private void performSync()
    {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long accId = prefs.getLong(GeneralPreferences.ODS_SYNCHONISATION_ACCOUNT, -1);
        String folderId = prefs.getString(GeneralPreferences.ODS_SYNCHONISATION, "");

        if (accId == -1 || folderId == null || "".equals(folderId))
        {
            return;
        }

        AlfrescoSession ses = requestSession(accId);

        if (ses == null)
        {
            return;
        }

        DocumentFolderService svc = ses.getServiceRegistry().getDocumentFolderService();
        Folder target = (Folder) svc.getNodeByIdentifier(folderId);

        if (target == null)
        {
            return;
        }

        final List<Document> remote = svc.getDocuments(target);
        final List<File> ls = findLocalFiles();

        for (File f : ls)
        {
            boolean upload = true;

            for (Document doc : remote)
            {
                if (doc.getName().equals(f.getName()))
                {
                    upload = false;
                    break;
                }
            }

            if (!upload)
            {
                continue;
            }

            try
            {
                svc.createDocument(target, f.getName(), null, new ContentFileImpl(f));
            }
            catch (Exception ex)
            {
                OdsLog.exw(TAG, ex);
            }
        }
    }

    private List<File> findLocalFiles()
    {
        final List<File> ls = new ArrayList<File>();

        for (File cur : OdsSyncReceiver.getSources())
        {
            for (File f : cur.listFiles())
            {
                if (f.isFile())
                {
                    ls.add(f);
                }
            }
        }

        return ls;
    }

    private AlfrescoSession requestSession(long accountId)
    {
        if (ApplicationManager.getInstance(this).hasSession(accountId))
        {
            return ApplicationManager.getInstance(this).getSession(accountId);
        } else
        {
            LoadSessionHelper helper = new LoadSessionHelper(this, accountId);
            AlfrescoSession session = helper.requestSession();
            ApplicationManager.getInstance(this).saveSession(helper.getAccount(), session);
            return session;
        }
    }
}
