/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * <p/>
 * This file is part of Alfresco Mobile for Android.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.operations.batch.impl;

import android.content.Context;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.impl.AbstractOperationThread;
import org.opendataspace.android.ui.logging.OdsLog;

public abstract class AbstractBatchOperationThread<T> extends AbstractOperationThread<T>
{
    private static final String TAG = AbstractBatchOperationThread.class.getName();

    protected static final String EXCEPTION_OPERATION_CANCEL = "Operation Cancelled";

    protected boolean hasCancelled = false;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public AbstractBatchOperationThread(Context context, OperationRequest request)
    {
        super(context, request);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    protected LoaderResult<T> doInBackground()
    {
        try
        {
            acc = AccountManager.retrieveAccount(context, accountId);

            session = requestSession();
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
        }
        return new LoaderResult<T>();
    }

    protected void onPostExecute(LoaderResult<T> result)
    {
        int resultStatus = Operation.STATUS_SUCCESSFUL;
        if (listener != null)
        {
            if (result.hasException())
            {
                listener.onError(this, result.getException());
                resultStatus = STATUS_FAILED;
                if (hasCancelled)
                {
                    resultStatus = STATUS_CANCEL;
                }
            }
        }
        saveStatus(resultStatus);
        BatchOperationManager.getInstance(context).notifyCompletion(getOperationId(), resultStatus);

        if (listener != null && !result.hasException())
        {
            listener.onPostExecute(this, result.getData());
        }
    }

    protected void onCancelled(LoaderResult<T> result)
    {
        saveStatus(Operation.STATUS_CANCEL);
    }

    public boolean requireNetwork()
    {
        return true;
    }

    public Account getAccount()
    {
        return acc;
    }
}
