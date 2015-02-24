package org.opendataspace.android.app.session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
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
import org.opendataspace.android.app.data.OdsFileInfoDAO;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;
import org.opendataspace.android.ui.logging.OdsLog;

import com.j256.ormlite.dao.CloseableIterator;

public class OdsDocumentFolderService extends OnPremiseDocumentFolderServiceImpl
{
    private static final String TAG = OdsDocumentFolderService.class.getSimpleName();

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
            context.setRenditionFilterString(type == DocumentFolderService.RENDITION_PREVIEW ? "image/*"
                    : "cmis:thumbnail");

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
            throw new IllegalArgumentException(String.format(
                    Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "object"));
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
                    .getInfoByFolder(parentFolder.getIdentifier(), OdsFileInfo.TYPE_DOWNLOAD);
            Map<String, OdsFileInfo> mp = new HashMap<String, OdsFileInfo>();

            try
            {
                while (it.hasNext())
                {
                    OdsFileInfo nfo = it.next();
                    mp.put(nfo.getNodeId(), nfo);
                }
            }
            finally
            {
                it.closeQuietly();
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

        }
        catch (Exception ex)
        {
            convertException(ex);
        }

        return res;
    }

    @Override
    public List<Document> getFavoriteDocuments()
    {
        List<Document> ls = new ArrayList<Document>();

        try
        {
            CloseableIterator<OdsFileInfo> it = OdsDataHelper.getHelper().getFileInfoDAO()
                    .getInfo(OdsFileInfo.TYPE_FAVORITE, false);

            try
            {
                while (it.hasNext())
                {
                    ls.add((Document) getNodeByIdentifier(it.next().getNodeId()));
                }
            }
            finally
            {
                it.closeQuietly();
            }
        }
        catch (SQLException ex)
        {
            convertException(ex);
        }

        return ls;
    }

    @Override
    public List<Folder> getFavoriteFolders()
    {
        List<Folder> ls = new ArrayList<Folder>();

        try
        {
            CloseableIterator<OdsFileInfo> it = OdsDataHelper.getHelper().getFileInfoDAO()
                    .getInfo(OdsFileInfo.TYPE_FAVORITE, true);

            try
            {
                while (it.hasNext())
                {
                    ls.add((Folder) getNodeByIdentifier(it.next().getNodeId()));
                }
            }
            finally
            {
                it.closeQuietly();
            }
        }
        catch (SQLException ex)
        {
            convertException(ex);
        }

        return ls;
    }

    @Override
    public List<Node> getFavoriteNodes()
    {
        List<Node> ls = new ArrayList<Node>();

        try
        {
            CloseableIterator<OdsFileInfo> it = OdsDataHelper.getHelper().getFileInfoDAO()
                    .getInfo(OdsFileInfo.TYPE_FAVORITE);

            try
            {
                while (it.hasNext())
                {
                    ls.add(getNodeByIdentifier(it.next().getNodeId()));
                }
            }
            finally
            {
                it.closeQuietly();
            }
        }
        catch (SQLException ex)
        {
            convertException(ex);
        }

        return ls;
    }

    @Override
    public boolean isFavorite(Node node)
    {
        try
        {
            OdsFileInfo info = OdsDataHelper.getHelper().getFileInfoDAO().queryForId(node.getIdentifier());
            return info != null && ((info.getType() & OdsFileInfo.TYPE_FAVORITE) != 0);
        }
        catch (SQLException ex)
        {
            OdsLog.ex(TAG, ex);
        }

        return false;
    }

    @Override
    public void addFavorite(Node node)
    {
        try
        {
            OdsFileInfoDAO dao = OdsDataHelper.getHelper().getFileInfoDAO();
            OdsFileInfo info = dao.queryForId(node.getIdentifier());

            if (info != null && ((info.getType() & OdsFileInfo.TYPE_FAVORITE) != 0))
            {
                return;
            }

            if (info == null)
            {
                info = new OdsFileInfo();
                info.setNodeId(node.getIdentifier());
                info.setFolderId(getParentFolder(node).getIdentifier());
                info.setPath("");
            }

            info.setType(info.getType() | OdsFileInfo.TYPE_FAVORITE);
            dao.createOrUpdate(info);
        }
        catch (SQLException ex)
        {
            convertException(ex);
        }
    }

    @Override
    public void removeFavorite(Node node)
    {
        try
        {
            OdsFileInfoDAO dao = OdsDataHelper.getHelper().getFileInfoDAO();
            OdsFileInfo info = dao.queryForId(node.getIdentifier());

            if (info == null || ((info.getType() & OdsFileInfo.TYPE_FAVORITE) == 0))
            {
                return;
            }

            info.setType(info.getType() & ~OdsFileInfo.TYPE_FAVORITE);

            if (info.getType() == 0)
            {
                dao.delete(info);
            }
            else
            {
                dao.update(info);
            }
        }
        catch (SQLException ex)
        {
            convertException(ex);
        }
    }
}
