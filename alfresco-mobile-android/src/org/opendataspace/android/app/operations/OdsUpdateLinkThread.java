package org.opendataspace.android.app.operations;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SecondaryTypeIds;
import org.opendataspace.android.app.data.OdsDataHelper;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.session.OdsFolder;
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

    public OdsUpdateLinkThread(Context context, OperationRequest request)
    {
        super(context, request);

        OdsUpdateLinkRequest rq = (OdsUpdateLinkRequest) request;
        link = rq.getLink();
    }

    @Override
    protected LoaderResult<OdsUpdateLinkContext> doInBackground()
    {
        LoaderResult<OdsUpdateLinkContext> result = new LoaderResult<OdsUpdateLinkContext>();

        try
        {
            super.doInBackground();

            Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();

            if (!TextUtils.isEmpty(link.getObjectId()))
            {
                cmisSession.delete(new ObjectIdImpl(link.getObjectId()));
                link.setObjectId(null);
            }

            if (!TextUtils.isEmpty(link.getNodeId()))
            {
                final Map<String, Object> properties = new HashMap<String, Object>();

                properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:item");
                properties.put(PropertyIds.EXPIRATION_DATE, link.getExpires());
                properties.put("gds:objectIds", Arrays.asList(link.getNodeId()));
                properties.put("gds:subject", link.getName());
                properties.put("gds:message", link.getMessage());
                properties.put("gds:comment", "");
                properties.put("gds:emailAddress", link.getEmail());
                properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                        Arrays.asList(SecondaryTypeIds.CLIENT_MANAGED_RETENTION, "gds:downloadLink"));

                if (!TextUtils.isEmpty(link.getPassword()))
                {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(link.getPassword().getBytes("UTF-8"));
                    properties.put("gds:password", new BigInteger(1, hash).toString(16));
                }

                DocumentFolderService svc = session.getServiceRegistry().getDocumentFolderService();
                OdsFolder folder = (OdsFolder) svc.getParentFolder(svc.getNodeByIdentifier(link.getNodeId()));
                final ObjectId id = cmisSession.createItem(properties, folder.getCmisObject());
                final Item item = (Item) cmisSession.getObject(id);
                final Property<String> property = item.getProperty("gds:url");

                link.setUrl(property.getFirstValue());
                link.setObjectId(item.getId());
            }

            OdsDataHelper.getHelper().getLinkDAO().process(link);
            ctx = new OdsUpdateLinkContext();
            result.setData(ctx);
        }
        catch (Exception e)
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
