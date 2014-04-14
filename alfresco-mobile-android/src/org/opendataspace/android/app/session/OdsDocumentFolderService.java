package org.opendataspace.android.app.session;

import java.util.List;

import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.impl.ContentStreamImpl;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.onpremise.OnPremiseDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;

public class OdsDocumentFolderService extends OnPremiseDocumentFolderServiceImpl
{

    public OdsDocumentFolderService(AlfrescoSession repositorySession)
    {
        super(repositorySession);
    }

    @Override
    public ContentStream getRenditionStream(String identifier, String type)
    {
        try
        {
            Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
            OperationContext context = cmisSession.createOperationContext();
            context.setRenditionFilterString(type == DocumentFolderService.RENDITION_PREVIEW ? "image/*" : "cmis:thumbnail");

            CmisObject targetDocument = cmisSession.getObject(identifier, context);
            List<Rendition> renditions = targetDocument.getRenditions();
            Rendition r = null;

            for (Rendition cur : renditions)
            {
                if (r == null || r.getWidth() * r.getHeight() < cur.getHeight() * cur.getWidth())
                {
                    r = cur;
                }
            }

            if (r != null)
            {
                return new ContentStreamImpl(targetDocument.getName(), r.getContentStream());
            }
        }
        catch (Exception e)
        {
            convertException(e);
        }
        return null;
    }

    @Override
    public Permissions getPermissions(Node node)
    {
        return new OdsPermissions(node);
    }
}
