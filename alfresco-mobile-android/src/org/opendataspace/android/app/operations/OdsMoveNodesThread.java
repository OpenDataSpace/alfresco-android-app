package org.opendataspace.android.app.operations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
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
    private final boolean isMove;
    private OdsFolder target;
    private long pos = 0;
    private long max = 0;

    public OdsMoveNodesThread(Context context, OperationRequest request)
    {
        super(context, request);

        OdsMoveNodesRequest rq = (OdsMoveNodesRequest) request;
        ids = rq.getIds();
        targetId = rq.getTargetId();
        isMove = rq.isMove();
        max = ids.size();
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
                    Node cur = svc.getNodeByIdentifier(id);
                    processNode(cur, target, svc, true);
                }
                catch (Exception ex)
                {
                    OdsLog.ex(TAG, ex);
                }
            }

            result.setData(true);
        }
        catch (Exception e)
        {
            OdsLog.exw(TAG, e);
            result.setException(e);
        }

        return result;
    }

    private boolean processNode(Node node, OdsFolder to, DocumentFolderService svc, boolean checkParent)
    {
        try
        {
            boolean res;

            if (node.isDocument())
            {
                res = processDocument((OdsDocument) node, to, svc);
            }
            else
            {
                res = processFolder((OdsFolder) node, to, svc, checkParent);
            }

            if (res && listener != null)
            {
                ++pos;
                listener.onProgressUpdate(this, 100 * pos / max);
            }

            return res;
        }
        catch (Exception ex)
        {
            OdsLog.ex(TAG, ex);
        }

        return false;
    }

    private boolean processFolder(OdsFolder from, OdsFolder to, DocumentFolderService svc, boolean checkParent)
    {
        if (checkParent)
        {
            Folder p = to;

            while (p != null)
            {
                if (p.getIdentifier().equals(from.getIdentifier()))
                {
                    return false;
                }

                if (p.getIdentifier().equals(svc.getRootFolder().getIdentifier()))
                {
                    break;
                }

                p = svc.getParentFolder(p);
            }
        }

        Map<String, Serializable> properties = new HashMap<String, Serializable>(2);
        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
        OdsFolder fld = (OdsFolder) svc.createFolder(to, from.getName(), properties);
        List<Node> ls = svc.getChildren(from);
        max += ls.size();

        for (Node cur : ls)
        {
            if (!processNode(cur, fld, svc, false))
            {
                return false;
            }
        }

        if (isMove)
        {
            svc.deleteNode(from);
        }

        return true;
    }

    private boolean processDocument(OdsDocument doc, OdsFolder to, DocumentFolderService svc)
    {
        org.apache.chemistry.opencmis.client.api.Document cmisdoc = (org.apache.chemistry.opencmis.client.api.Document) doc
                .getCmisObject();

        if (isMove)
        {
            OdsFolder from = (OdsFolder) svc.getParentFolder(doc);

            if (from.getIdentifier().equals(to.getIdentifier()))
            {
                return true;
            }

            cmisdoc.move(from.getCmisObject(), to.getCmisObject());
        }
        else
        {
            cmisdoc.copy(to.getCmisObject());
        }

        return true;
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
