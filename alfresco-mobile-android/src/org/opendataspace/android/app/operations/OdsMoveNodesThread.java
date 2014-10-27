package org.opendataspace.android.app.operations;

import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.opendataspace.android.app.session.OdsDocument;
import org.opendataspace.android.app.session.OdsFolder;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class OdsMoveNodesThread extends AbstractBatchOperationThread<Boolean>
{
    private static final String TAG = "OdsMoveNodesThread";

    private final List<String> ids;
    private final String targetId;
    private OdsFolder target;

    public OdsMoveNodesThread(Context context, OperationRequest request)
    {
        super(context, request);

        OdsMoveNodesRequest rq = (OdsMoveNodesRequest) request;
        ids = rq.getIds();
        targetId = rq.getTargetId();
    }

    @Override
    protected LoaderResult<Boolean> doInBackground()
    {
        LoaderResult<Boolean> result = new LoaderResult<Boolean>();

        try
        {
            super.doInBackground();

            DocumentFolderService svc = session.getServiceRegistry().getDocumentFolderService();
            target = (OdsFolder) svc.getNodeByIdentifier(targetId);

            for (String id : ids)
            {
                try
                {
                    OdsDocument cur = (OdsDocument) svc.getNodeByIdentifier(id);
                    org.apache.chemistry.opencmis.client.api.Document doc = (org.apache.chemistry.opencmis.client.api.Document) cur
                            .getCmisObject();

                    doc.copy(target.getCmisObject());
                } catch (Exception ex)
                {
                    OdsLog.ex(TAG, ex);
                }
            }

            result.setData(true);
        } catch (Exception e)
        {
            OdsLog.exw(TAG, e);
            result.setException(e);
        }

        return result;
    }

    @Override
    public Intent getCompleteBroadCastIntent()
    {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentIntegrator.ACTION_MOVE_NODES_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_FOLDER, target);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
