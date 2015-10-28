/**
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
package org.alfresco.mobile.android.application.operations.batch.node.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.AbstractDocumentFolderServiceImpl;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.StorageManager;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.alfresco.mobile.android.application.operations.batch.node.AbstractUpThread;
import org.alfresco.mobile.android.application.security.DataProtectionManager;
import org.opendataspace.android.app.security.OdsEncryptionUtils;
import org.opendataspace.android.ui.logging.OdsLog;

public class UpdateContentThread extends AbstractUpThread
{
    private static final String TAG = UpdateContentThread.class.getName();

    private Document originalDocument;

    private String originalIdentifier;

    private Document updatedDocument = null;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public UpdateContentThread(Context ctx, AbstractBatchOperationRequestImpl request)
    {
        super(ctx, request);
        if (request instanceof UpdateContentRequest)
        {
            this.originalIdentifier = ((UpdateContentRequest) request).getNodeIdentifier();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFE CYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    protected LoaderResult<Document> doInBackground()
    {
        LoaderResult<Document> result = new LoaderResult<Document>();

        try
        {
            result = super.doInBackground();

            DocumentFolderService ds = session.getServiceRegistry().getDocumentFolderService();
            originalDocument = (Document) ds.getNodeByIdentifier(originalIdentifier);

            if (contentFile != null)
            {
                if (!StorageManager.isTempFile(context, contentFile.getFile()) &&
                        DataProtectionManager.getInstance(context).isEncrypted(contentFile.getFile().getPath()))
                {
                    //Decrypt now !
                    OdsEncryptionUtils.decryptFile(context, contentFile.getFile().getPath());
                }
                
                if (ds instanceof AbstractDocumentFolderServiceImpl &&
                        acc.getProtocolType() == Account.ProtocolType.ATOM)
                {
                    AbstractDocumentFolderServiceImpl ads = (AbstractDocumentFolderServiceImpl) ds;
                    updatedDocument = ads.updateContent(originalDocument, contentFile, 512 * 1024);
                }
                else
                {
                    updatedDocument = ds.updateContent(originalDocument, contentFile);
                }

                // Encrypt if necessary / Delete otherwise
                StorageManager.manageFile(context, contentFile.getFile());
            }
        }
        catch (Exception e)
        {
            if (result == null)
            {
                result = new LoaderResult<Document>();
            }
            result.setException(e);
            OdsLog.ex(TAG, e);
        }

        result.setData(updatedDocument);

        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // GETTERS
    // ///////////////////////////////////////////////////////////////////////////
    public Document getDocument()
    {
        return originalDocument;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // EVENTS
    // ///////////////////////////////////////////////////////////////////////////
    public Intent getStartBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_STARTED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, originalDocument);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }

    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_COMPLETED);
        Bundle b = new Bundle();
        b.putParcelable(IntentIntegrator.EXTRA_FOLDER, getParentFolder());
        b.putParcelable(IntentIntegrator.EXTRA_DOCUMENT, originalDocument);
        b.putParcelable(IntentIntegrator.EXTRA_UPDATED_NODE, updatedDocument);
        b.putString(IntentIntegrator.EXTRA_FILE_PATH, contentFile.getFile().getPath());
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
