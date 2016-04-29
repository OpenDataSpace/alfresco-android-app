/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 *  This file is part of Alfresco Mobile for Android.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.file.delete;

import java.io.File;
import java.io.IOException;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.batch.file.FileOperationThread;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DeleteFileThread extends FileOperationThread<Void>
{
    private static final String TAG = DeleteFileThread.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DeleteFileThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            result = super.doInBackground();

            if (file.isDirectory())
            {
                if (!deleteDirectory(file)) { throw new IOException("Unable to delete the file"); }
            }
            else if (!file.delete()) { throw new IOException("Unable to delete the file"); }
        }
        catch (Exception e)
        {
            OdsLog.ex(TAG, e);
            result.setException(e);
        }

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_DELETE_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, parentFile);
        b.putSerializable(IntentIntegrator.EXTRA_FILE, file);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // INTERNALS
    // ///////////////////////////////////////////////////////////////////////////
    private static boolean deleteDirectory(File path)
    {
        if (path.exists())
        {
            File[] files = path.listFiles();
            if (files == null) { return true; }
            for (File file1 : files)
            {
                if (file1.isDirectory())
                {
                    deleteDirectory(file1);
                }
                else
                {
                    //noinspection ResultOfMethodCallIgnored
                    file1.delete();
                }
            }
        }
        return (path.delete());
    }
}
