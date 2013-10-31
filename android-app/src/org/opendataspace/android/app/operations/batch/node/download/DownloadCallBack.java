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
package org.opendataspace.android.app.operations.batch.node.download;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.manager.NotificationHelper;
import org.opendataspace.android.app.operations.Operation;
import org.opendataspace.android.app.operations.OperationsGroupCallBack;
import org.opendataspace.android.app.operations.batch.BatchOperationManager;
import org.opendataspace.android.app.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.utils.thirdparty.LocalBroadcastManager;
import org.opendataspace.android.cmisapi.model.ContentFile;

import android.content.Context;

public class DownloadCallBack extends AbstractBatchOperationCallback<ContentFile> implements OperationsGroupCallBack
{
    public DownloadCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.download_progress);
        complete = getBaseContext().getString(R.string.download_complete);
        finalComplete = R.plurals.download_complete_description;
    }

    @Override
    public void onPreExecute(Operation<ContentFile> task)
    {
        groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
        if (groupRecord.totalRequests == 1)
        {
            NotificationHelper.createProgressNotification(getBaseContext(), getNotificationId(), inProgress,
                    ((DownloadThread) task).getDocument().getName(), groupRecord.completeRequest.size() + "/"
                            + groupRecord.totalRequests, 0, 100);
        }
        else
        {
            NotificationHelper.createIndeterminateNotification(
                    getBaseContext(),
                    getNotificationId(),
                    inProgress,
                    String.format(
                            getBaseContext().getResources().getQuantityString(R.plurals.download_in_progress,
                                    groupRecord.runningRequest.size()), groupRecord.runningRequest.size() + ""),
                    groupRecord.completeRequest.size() + "/" + groupRecord.totalRequests);
        }
    }

    @Override
    public void onProgressUpdate(Operation<ContentFile> task, Long values)
    {
        groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
        if (groupRecord.totalRequests == 1)
        {
            if (values == 100)
            {
                NotificationHelper.createIndeterminateNotification(getBaseContext(), getNotificationId(), inProgress,
                        ((DownloadThread) task).getDocument().getName(), groupRecord.completeRequest.size() + "/"
                                + groupRecord.totalRequests);
            }
            else
            {
                NotificationHelper.createProgressNotification(getBaseContext(), getNotificationId(), inProgress,
                        ((DownloadThread) task).getDocument().getName(), groupRecord.completeRequest.size() + "/"
                                + groupRecord.totalRequests, values,
                        ((DownloadRequest) task.getOperationRequest()).getContentStreamLength());
            }
        }
    }

    @Override
    public void onPostExecute(Operation<ContentFile> task, ContentFile results)
    {
        if (task.getCompleteBroadCastIntent() != null)
        {
            LocalBroadcastManager.getInstance(context).sendBroadcast(task.getCompleteBroadCastIntent());
        }

        groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());
        if (groupRecord.totalRequests == 1)
        {
            NotificationHelper.createIndeterminateNotification(getBaseContext(), getNotificationId(), getBaseContext()
                    .getString(R.string.download_progress), ((DownloadThread) task).getDocument().getName(),
                    groupRecord.completeRequest.size() + "/" + groupRecord.totalRequests);
        }
        else
        {
            NotificationHelper.createIndeterminateNotification(
                    getBaseContext(),
                    getNotificationId(),
                    inProgress,
                    String.format(
                            getBaseContext().getResources().getQuantityString(R.plurals.download_in_progress,
                                    groupRecord.runningRequest.size()), groupRecord.runningRequest.size() + ""),
                    groupRecord.completeRequest.size() + "/" + groupRecord.totalRequests);
        }
    }

    @Override
    public void onError(Operation<ContentFile> task, Exception e)
    {
        // TODO Auto-generated method stub

    }

    protected int getNotificationId()
    {
        return NotificationHelper.DOWNLOAD_NOTIFICATION_ID;
    }
}
