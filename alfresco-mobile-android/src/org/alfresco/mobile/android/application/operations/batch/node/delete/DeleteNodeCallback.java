/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * <p>
 * This file is part of Alfresco Mobile for Android.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.node.delete;

import android.content.Context;

import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsGroupResult;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.R;

/**
 * @author Jean Marie Pascal
 */
public class DeleteNodeCallback extends AbstractBatchOperationCallback<Void>
{
    public DeleteNodeCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.delete_in_progress);
        complete = getBaseContext().getString(R.string.delete_complete);
    }

    @Override
    public void onPostExecution(OperationsGroupResult result)
    {
        if (result.notificationVisibility != OperationRequest.VISIBILITY_HIDDEN &&
                (result.completeRequest == null || result.completeRequest.isEmpty()))
        {
            return;
        }

        super.onPostExecution(result);
    }
}
