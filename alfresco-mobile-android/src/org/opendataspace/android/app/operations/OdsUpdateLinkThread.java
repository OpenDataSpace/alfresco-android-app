package org.opendataspace.android.app.operations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SecondaryTypeIds;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class OdsUpdateLinkThread extends AbstractBatchOperationThread<OdsUpdateLinkContext>
{
    private static final String TAG = "OdsUpdateLinkThread";

    private OdsUpdateLinkContext ctx;
    private OdsLink link;
    private String nodeId;

    public OdsUpdateLinkThread(Context context, OperationRequest request)
    {
        super(context, request);

        OdsUpdateLinkRequest rq = (OdsUpdateLinkRequest) request;
        link = rq.getLink();
        nodeId = rq.getNodeId();
    }

    @Override
    protected LoaderResult<OdsUpdateLinkContext> doInBackground()
    {
        LoaderResult<OdsUpdateLinkContext> result = new LoaderResult<OdsUpdateLinkContext>();

        try
        {
            Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();

            if (!TextUtils.isEmpty(link.getObjectId()))
            {
                cmisSession.delete(cmisSession.getObject(link.getObjectId()));
            }

            if (!TextUtils.isEmpty(nodeId))
            {
                final Map<String, Object> properties = new HashMap<String, Object>();

                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:item");
                properties.put(PropertyIds.EXPIRATION_DATE, null);
                properties.put("gds:objectIds", Arrays.asList(nodeId));
                properties.put("gds:subject", link.getName());
                properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                        Arrays.asList(SecondaryTypeIds.CLIENT_MANAGED_RETENTION, "gds:downloadLink"));

                DocumentFolderService svc = session.getServiceRegistry().getDocumentFolderService();
                Folder folder = svc.getParentFolder(svc.getNodeByIdentifier(nodeId));
                final ObjectId objectId = cmisSession.createItem(properties,
                        cmisSession.getObject(folder.getIdentifier()));
                final Item item = (Item) cmisSession.getObject(objectId);
                final Property<String> property = item.getProperty("gds:url");

                link.setUrl(property.getFirstValue());
            }

            ctx = new OdsUpdateLinkContext();
            result.setData(ctx);
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
        broadcastIntent.setAction(IntentIntegrator.ACTION_UPDATE_LINK_COMPLETED);
        Bundle b = new Bundle();
        b.putSerializable(IntentIntegrator.EXTRA_CONFIGURATION, ctx);
        broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, b);
        return broadcastIntent;
    }
}
