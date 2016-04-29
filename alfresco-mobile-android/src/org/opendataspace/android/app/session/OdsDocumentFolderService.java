package org.opendataspace.android.app.session;

import com.j256.ormlite.dao.CloseableIterator;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;
import org.alfresco.mobile.android.api.model.impl.ContentStreamImpl;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.services.impl.onpremise.OnPremiseDocumentFolderServiceImpl;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.opendataspace.android.app.data.OdsDataHelper;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            context.setRenditionFilterString(
                    type.equals(DocumentFolderService.RENDITION_PREVIEW) ? "image/*" : "cmis:thumbnail");

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

    @Override
    protected Node convertNode(CmisObject object, boolean hasAllProperties)
    {
        if (isObjectNull(object))
        {
            throw new IllegalArgumentException(
                    String.format(Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "object"));
        }

        switch (object.getBaseTypeId())
        {
        case CMIS_DOCUMENT:
            return new OdsDocument(object, hasAllProperties);
        case CMIS_FOLDER:
            return new OdsFolder(object, hasAllProperties);
        default:
            throw new AlfrescoServiceException(ErrorCodeRegistry.DOCFOLDER_WRONG_NODE_TYPE,
                    Messagesl18n.getString("AlfrescoService.2") + object.getBaseTypeId());
        }
    }

    @Override
    public PagingResult<Node> getChildren(Folder parentFolder, ListingContext lcontext)
    {
        PagingResult<Node> res = super.getChildren(parentFolder, lcontext);

        try
        {
            CloseableIterator<OdsFileInfo> it = OdsDataHelper.getHelper().getFileInfoDAO()
                    .getLinksByFolder(parentFolder.getIdentifier(), OdsFileInfo.TYPE_DOWNLOAD);
            Map<String, OdsFileInfo> mp = new HashMap<String, OdsFileInfo>();

            while (it.hasNext())
            {
                OdsFileInfo nfo = it.next();
                mp.put(nfo.getNodeId(), nfo);
            }

            for (Node cur : res.getList())
            {
                OdsFileInfo nfo = mp.get(cur.getIdentifier());

                if (nfo != null && cur instanceof OdsDocument)
                {
                    OdsDocument doc = (OdsDocument) cur;
                    doc.setDownloaded(nfo.isValid(doc));
                    doc.setFileInfo(nfo);
                }
            }

            it.closeQuietly();
        }
        catch (Exception ex)
        {
            convertException(ex);
        }

        return res;
    }
}
