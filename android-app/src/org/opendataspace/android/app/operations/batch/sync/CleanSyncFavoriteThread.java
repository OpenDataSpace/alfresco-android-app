/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.app.operations.batch.sync;

import java.io.File;






import org.opendataspace.android.app.accounts.AccountManager;
import org.opendataspace.android.app.manager.StorageManager;
import org.opendataspace.android.app.operations.OperationRequest;
import org.opendataspace.android.app.operations.batch.BatchOperationSchema;
import org.opendataspace.android.app.operations.batch.impl.AbstractBatchOperationThread;
import org.opendataspace.android.app.operations.sync.SyncOperation;
import org.opendataspace.android.app.operations.sync.SynchroProvider;
import org.opendataspace.android.app.operations.sync.SynchroSchema;
import org.opendataspace.android.app.utils.IOUtils;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class CleanSyncFavoriteThread extends AbstractBatchOperationThread<Void>
{
    private static final String TAG = CleanSyncFavoriteThread.class.getName();

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public CleanSyncFavoriteThread(Context context, OperationRequest request)
    {
        super(context, request);
        if (request instanceof CleanSyncFavoriteRequest)
        {
            this.accountId = ((CleanSyncFavoriteRequest) request).getAccountId();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Void> doInBackground()
    {
        LoaderResult<Void> result = new LoaderResult<Void>();
        try
        {
            //Delete All local files in sync folder
            acc = AccountManager.retrieveAccount(context, accountId);
            File synchroFolder = StorageManager.getSynchroFolder(context, acc);
            
            if (synchroFolder != null && synchroFolder.exists())
            {
                IOUtils.deleteContents(synchroFolder);
            }
            
            //For each sync row, reset status
            Cursor allFavoritesCursor = context.getContentResolver().query(SynchroProvider.CONTENT_URI,
                    SynchroSchema.COLUMN_ALL, null, null, null);
            while (allFavoritesCursor.moveToNext())
            {
                // Update Sync Info
                ContentValues cValues = new ContentValues();
                cValues.put(BatchOperationSchema.COLUMN_STATUS, SyncOperation.STATUS_PENDING);
                context.getContentResolver().update(request.getNotificationUri(), cValues, null, null);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, Log.getStackTraceString(e));
            result.setException(e);
        }
        return result;
    }

}
