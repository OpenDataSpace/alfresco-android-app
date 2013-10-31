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
package org.opendataspace.android.app.operations.sync.node.download;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.manager.NotificationHelper;
import org.opendataspace.android.app.operations.Operation;
import org.opendataspace.android.app.operations.OperationsGroupCallBack;
import org.opendataspace.android.app.operations.sync.impl.AbstractSyncOperationCallback;
import org.opendataspace.android.cmisapi.model.ContentFile;

import android.content.Context;

public class SyncDownloadCallBack extends AbstractSyncOperationCallback<ContentFile> implements OperationsGroupCallBack
{
    public SyncDownloadCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }

    @Override
    public void onPreExecute(Operation<ContentFile> task)
    {
        NotificationHelper.createProgressNotification(getBaseContext(),
                getNotificationId(),
                getBaseContext().getString(R.string.download_progress), ((SyncDownloadThread) task).getDocument()
                        .getName(), totalItems - pendingItems + "/" + totalItems, 0, PROGRESS_MAX);
    }

    @Override
    public void onPostExecute(Operation<ContentFile> task, ContentFile results)
    {
        // Improvement : Better notification with share button or open in.
        NotificationHelper.createIndeterminateNotification(getBaseContext(),
                getNotificationId(),
                getBaseContext().getString(R.string.download_progress), ((SyncDownloadThread) task).getDocument()
                        .getName(), totalItems - pendingItems + "/" + totalItems);
    }

    @Override
    public void onProgressUpdate(Operation<ContentFile> task, Long values)
    {
        if (values == PROGRESS_MAX)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(),
                    getNotificationId(),
                    getBaseContext().getString(R.string.download_progress), ((SyncDownloadThread) task).getDocument()
                            .getName(), totalItems - pendingItems + "/" + totalItems);
        }
        else
        {
            NotificationHelper.createProgressNotification(getBaseContext(),
                    getNotificationId(),
                    getBaseContext().getString(R.string.download_progress), ((SyncDownloadThread) task).getDocument()
                            .getName(), totalItems - pendingItems + "/" + totalItems, values,
                    ((SyncDownloadRequest) task.getOperationRequest()).getContentStreamLength());
        }
    }

    @Override
    public void onError(Operation<ContentFile> task, Exception e)
    {
        // TODO Auto-generated method stub

    }
}
