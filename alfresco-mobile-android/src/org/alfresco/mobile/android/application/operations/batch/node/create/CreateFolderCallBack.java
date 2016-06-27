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
package org.alfresco.mobile.android.application.operations.batch.node.create;

import android.content.Context;
import android.os.Bundle;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.commons.fragments.SimpleAlertDialogFragment;
import org.alfresco.mobile.android.application.exception.SessionExceptionHelper;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.opendataspace.android.app.R;

public class CreateFolderCallBack extends AbstractBatchOperationCallback<Folder>
{

    public CreateFolderCallBack(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        inProgress = getBaseContext().getString(R.string.create_folder_in_progress);
        complete = getBaseContext().getString(R.string.create_folder_complete);
    }

    @Override
    public void onError(Operation<Folder> task, Exception e)
    {
        super.onError(task, e);

        if (SessionExceptionHelper.checkCause(e, CmisNameConstraintViolationException.class))
        {
            Bundle b = new Bundle();
            b.putInt(SimpleAlertDialogFragment.PARAM_ICON, R.drawable.ic_edit);
            b.putInt(SimpleAlertDialogFragment.PARAM_TITLE, R.string.error_duplicate_title);
            b.putString(SimpleAlertDialogFragment.PARAM_MESSAGE_STRING,
                    String.format(context.getString(R.string.error_duplicate_rename),
                            ((CreateFolderThread) task).getFolderName()));
            b.putInt(SimpleAlertDialogFragment.PARAM_POSITIVE_BUTTON, android.R.string.ok);
            ActionManager.actionDisplayDialog(context, b);
        }
    }
}
