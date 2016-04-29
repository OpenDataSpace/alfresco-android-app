package org.opendataspace.android.app.operations;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationThread;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Item;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SecondaryTypeIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.session.OdsFolder;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.app.session.OdsTypeDefinition;
import org.opendataspace.android.app.utils.OdsStringUtils;
import org.opendataspace.android.ui.logging.OdsLog;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OdsUpdateLinkThread extends AbstractBatchOperationThread<OdsUpdateLinkContext>
{
    private static final String TAG = "OdsUpdateLinkThread";

    private OdsUpdateLinkContext ctx;
    private final OdsLink link;

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
            boolean isCombined = false;

            if (session instanceof OdsRepositorySession)
            {
                isCombined = ((OdsRepositorySession) session).getLinkCapablilty() ==
                        OdsRepositorySession.LinkCapablilty.COMBINED;
            }

            Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
            boolean hasObjectId = !TextUtils.isEmpty(link.getObjectId());
            boolean hasNodeId = !TextUtils.isEmpty(link.getNodeId());

            if (!hasNodeId && hasObjectId)
            {
                String relId = link.getRelationId();

                if (!TextUtils.isEmpty(relId))
                {
                    cmisSession.delete(new ObjectIdImpl(link.getRelationId()));
                    link.setRelationId(null);
                }

                cmisSession.delete(new ObjectIdImpl(link.getObjectId()));
                link.setObjectId(null);
                hasObjectId = false;
            }

            if (hasNodeId)
            {
                if (!hasObjectId)
                {
                    createLink(cmisSession, isCombined);
                }
                else
                {
                    updateLink(cmisSession, isCombined);
                }
            }

            ctx = new OdsUpdateLinkContext();
            result.setData(ctx);
        }
        catch (Exception ex)
        {
            OdsLog.exw(TAG, ex);
            result.setException(ex);
        }

        return result;
    }

    private void updateLink(Session cmisSession, boolean isCombined) throws Exception
    {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.EXPIRATION_DATE, link.getExpires());
        properties.put(OdsTypeDefinition.SUBJECT_PROP_ID, link.getName());
        properties.put(OdsTypeDefinition.MESSAGE_PROP_ID, link.getMessage());

        if (isCombined)
        {
            properties.put(OdsTypeDefinition.EMAIL_PROP_ID,
                    Arrays.asList(OdsStringUtils.splitString(link.getEmail(), ",")));
        }
        else
        {
            properties.put(OdsTypeDefinition.EMAIL_PROP_ID, link.getEmail());
        }

        if (!TextUtils.isEmpty(link.getPassword()))
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(link.getPassword().getBytes("UTF-8"));
            properties.put("gds:password", new BigInteger(1, hash).toString(16));
        }

        final Item item = (Item) cmisSession.getObject(link.getObjectId());
        item.updateProperties(properties);
    }

    private void createLink(Session cmisSession, boolean isCombined) throws Exception
    {
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_ITEM.value());
        properties.put(PropertyIds.EXPIRATION_DATE, link.getExpires());
        properties.put(OdsTypeDefinition.SUBJECT_PROP_ID, link.getName());
        properties.put(OdsTypeDefinition.MESSAGE_PROP_ID, link.getMessage());

        if (isCombined)
        {
            properties.put(OdsTypeDefinition.EMAIL_PROP_ID,
                    Arrays.asList(OdsStringUtils.splitString(link.getEmail(), ",")));
            properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                    Collections.singletonList(OdsTypeDefinition.LINK_TYPE_ID));
            properties.put(OdsTypeDefinition.LTYPE_PROP_ID,
                    link.getType() == OdsLink.Type.DOWNLOAD ? OdsTypeDefinition.LINK_TYPE_DOWNLAOD :
                            OdsTypeDefinition.LINK_TYPE_UPLOAD);
        }
        else
        {
            properties.put(OdsTypeDefinition.EMAIL_PROP_ID, link.getEmail());
            properties.put(OdsTypeDefinition.LEGACY_OBIDS_ID, Collections.singletonList(link.getNodeId()));
            properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,
                    Arrays.asList(SecondaryTypeIds.CLIENT_MANAGED_RETENTION, OdsTypeDefinition.LINK_TYPE_DOWNLAOD));
        }

        if (!TextUtils.isEmpty(link.getPassword()))
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(link.getPassword().getBytes("UTF-8"));
            properties.put("gds:password", new BigInteger(1, hash).toString(16));
        }

        DocumentFolderService svc = session.getServiceRegistry().getDocumentFolderService();
        OdsFolder folder = (OdsFolder) svc.getParentFolder(svc.getNodeByIdentifier(link.getNodeId()));
        final ObjectId id = cmisSession.createItem(properties, folder.getCmisObject());
        final CmisObject item = cmisSession.getObject(id);
        final Property<String> property = item.getProperty(OdsTypeDefinition.URL_PROP_ID);

        link.setUrl(property.getFirstValue());
        link.setObjectId(item.getId());

        if (isCombined)
        {
            final Map<String, Object> rel = new HashMap<String, Object>();
            rel.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_RELATIONSHIP.value());
            rel.put(PropertyIds.SOURCE_ID, item.getId());
            rel.put(PropertyIds.TARGET_ID, link.getNodeId());
            link.setRelationId(cmisSession.createRelationship(rel).getId());
        }
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
