package org.opendataspace.android.app.links;

import android.content.Context;

import com.j256.ormlite.dao.CloseableIterator;
import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.opendataspace.android.app.data.OdsDataHelper;
import org.opendataspace.android.app.session.OdsFolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OdsLinksLoader extends AbstractPagingLoader<LoaderResult<List<OdsLink>>>
{
    public static final int ID = OdsLinksLoader.class.hashCode();

    private Node node;
    private OdsLink.Type type;

    public OdsLinksLoader(Context context, AlfrescoSession session, Node node, OdsLink.Type type)
    {
        super(context);
        this.session = session;
        this.node = node;
        this.type = type;
    }

    @Override
    public LoaderResult<List<OdsLink>> loadInBackground()
    {
        LoaderResult<List<OdsLink>> result = new LoaderResult<List<OdsLink>>();
        List<OdsLink> data = new ArrayList<OdsLink>();

        if (type == OdsLink.Type.DOWNLOAD)
        {
            CloseableIterator<OdsLink> it = null;

            try
            {
                it = OdsDataHelper.getHelper().getLinkDAO().getLinksByNode(node.getIdentifier(), type);

                while (it.hasNext())
                {
                    data.add(it.nextThrow());
                }
            }
            catch (Exception ex)
            {
                result.setException(ex);
            }
            finally
            {
                if (it != null)
                {
                    it.closeQuietly();
                }
            }
        }
        else
        {
            Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
            OdsFolder ods = (OdsFolder) node;
            Folder folder = (Folder) ods.getCmisObject();

            final OperationContext context = cmisSession.createOperationContext();
            context.setFilterString(PropertyIds.SOURCE_ID);
            context.setIncludeAllowableActions(false);
            context.setIncludePathSegments(false);
            List<Tree<FileableCmisObject>> ls = folder.getFolderTree(1, context);

            for (final Tree<FileableCmisObject> tree : ls)
            {
                final FileableCmisObject uploadFolder = tree.getItem();
                final List<Relationship> relationships = uploadFolder.getRelationships();

                if (relationships != null)
                {
                    for (final Relationship relationship : relationships)
                    {
                        final CmisObject uploadLink = relationship.getSource();
                        OdsLink link = new OdsLink();
                        link.setType(OdsLink.Type.UPLOAD);
                        link.setEmail((String) uploadLink.getPropertyValue("gds:emailAddress"));
                        link.setExpires((Calendar) uploadLink.getPropertyValue(PropertyIds.EXPIRATION_DATE));
                        link.setMessage((String) uploadLink.getPropertyValue("gds:message"));
                        link.setName((String) uploadLink.getPropertyValue("gds:subject"));
                        link.setNodeId(node.getIdentifier());
                        link.setObjectId(uploadLink.getId());
                        link.setUrl((String) uploadLink.getPropertyValue("gds:url"));
                        data.add(link);
                    }
                }
            }
        }

        result.setData(data);
        return result;
    }
}
