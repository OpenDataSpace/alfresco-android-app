package org.opendataspace.android.app.operations;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.opendataspace.android.app.config.OdsConfigContext;
import org.opendataspace.android.app.config.OdsConfigManager;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class OdsConfigThread extends AbstractBatchOperationThread<OdsConfigContext>
{
    private static final String TAG = "OdsConfigThread";

    private static final int MAX_BUFFER_SIZE = 1024;

    private OdsConfigContext ctx;

    public OdsConfigThread(Context context, OperationRequest request)
    {
        super(context, request);
    }

    protected LoaderResult<OdsConfigContext> doInBackground()
    {
        LoaderResult<OdsConfigContext> result = new LoaderResult<OdsConfigContext>();

        try
        {
            super.doInBackground();

            OdsRepositorySession config = null;

            if (session instanceof OdsRepositorySession)
            {
                config = ((OdsRepositorySession) session).getConfig();
            }

            Folder folder = null;
            DocumentFolderService svc = null;

            if (config != null)
            {
                svc = config.getServiceRegistry().getDocumentFolderService();
                folder = (Folder) svc.getChildByPath(config.getRootFolder(), "config");
            }

            if (folder != null)
            {
                for (String cur : OdsConfigManager.FILES)
                {
                    try
                    {
                        Document doc = (Document) svc.getChildByPath(folder, cur);

                        if (doc != null)
                        {
                            File f = OdsConfigManager.getBrandingFile(context, cur, acc);
                            ContentStream contentStream = svc.getContentStream(doc);
                            copyFile(contentStream.getInputStream(), contentStream.getLength(), f);
                        }
                    } catch (Exception ex) {
                        // nothing
                    }
                }

                ctx = new OdsConfigContext();
                result.setData(ctx);
            }
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
            result.setException(e);
        }
        return result;
    }

    private void copyFile(InputStream src, long size, File dest)
    {
        OutputStream os = null;

        try
        {
            long downloaded = 0;
            os = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            while (size - downloaded > 0)
            {
                if (isInterrupted())
                {
                    hasCancelled = true;
                    os.close();
                    throw new IOException(EXCEPTION_OPERATION_CANCEL);
                }

                if (size - downloaded < MAX_BUFFER_SIZE)
                {
                    buffer = new byte[(int) (size - downloaded)];
                }

                int read = src.read(buffer);
                if (read == -1)
                {
                    break;
                }

                os.write(buffer, 0, read);
                downloaded += read;
            }
        }
        catch (FileNotFoundException e)
        {
            OdsLog.ex(TAG, e);
        }
        catch (IOException e)
        {
            OdsLog.ex(TAG, e);
        }
        finally
        {
            org.alfresco.mobile.android.api.utils.IOUtils.closeStream(src);
            org.alfresco.mobile.android.api.utils.IOUtils.closeStream(os);
        }
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_CONFIGURATION_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_CONFIGURATION, ctx);
        b.putLong(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
