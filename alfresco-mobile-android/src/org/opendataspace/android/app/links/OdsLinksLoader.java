package org.opendataspace.android.app.links;

import android.content.Context;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.services.DocumentFolderService;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.opendataspace.android.app.session.OdsFolder;
import org.opendataspace.android.app.session.OdsTypeDefinition;
import org.opendataspace.android.ui.logging.OdsLog;

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

        Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
        DocumentFolderService svc = session.getServiceRegistry().getDocumentFolderService();
        OdsFolder ods = (OdsFolder) svc.getParentFolder(svc.getNodeByIdentifier(node.getIdentifier()));
        Folder folder = (Folder) ods.getCmisObject();

        final OperationContext context = cmisSession.createOperationContext();
        context.setIncludeRelationships(IncludeRelationships.TARGET);

        try
        {
            recFindLinks(data, folder.getFolderTree(1, context), cmisSession);
        }
        catch (Exception ex)
        {
            OdsLog.exw("OdsLinksLoader", ex);
        }

        result.setData(data);
        return result;
    }

    private void recFindLinks(List<OdsLink> data, List<Tree<FileableCmisObject>> ls, Session cmisSession)
    {
        if (ls == null)
        {
            return;
        }

        for (final Tree<FileableCmisObject> tree : ls)
        {
            final FileableCmisObject uploadFolder = tree.getItem();
            final List<Relationship> relationships = uploadFolder.getRelationships();

            if (relationships != null)
            {
                for (final Relationship relationship : relationships)
                {
                    CmisObject cmo;
                    try
                    {
                        cmo = relationship.getSource();
                    }
                    catch (Exception ex)
                    {
                        OdsLog.exw("OdsLinksLoader", ex);
                        continue;
                    }

                    boolean found = false;

                    if (cmo == null || cmo.getSecondaryTypes() == null)
                    {
                        continue;
                    }

                    for (final SecondaryType secondaryType : cmo.getSecondaryTypes())
                    {
                        if (OdsTypeDefinition.LINK_TYPE_ID.equals(secondaryType.getId()))
                        {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                    {
                        continue;
                    }

                    String ltype = cmo.getPropertyValue(OdsTypeDefinition.LTYPE_PROP_ID);

                    if ((OdsTypeDefinition.LINK_TYPE_UPLOAD.equals(ltype) && type == OdsLink.Type.UPLOAD) ||
                            (OdsTypeDefinition.LINK_TYPE_DOWNLAOD.equals(ltype) && type == OdsLink.Type.DOWNLOAD))
                    {
                        OdsLink link = new OdsLink();
                        link.setType(type);
                        link.setEmail((String) cmo.getPropertyValue(OdsTypeDefinition.EMAIL_PROP_ID));
                        link.setExpires((Calendar) cmo.getPropertyValue(PropertyIds.EXPIRATION_DATE));
                        link.setMessage((String) cmo.getPropertyValue(OdsTypeDefinition.MESSAGE_PROP_ID));
                        link.setName((String) cmo.getPropertyValue(OdsTypeDefinition.SUBJECT_PROP_ID));
                        link.setNodeId(node.getIdentifier());
                        link.setObjectId(cmo.getId());
                        link.setUrl((String) cmo.getPropertyValue(OdsTypeDefinition.URL_PROP_ID));

                        if (link.isValid())
                        {
                            data.add(link);
                        }
                    }
                }
            }

            recFindLinks(data, tree.getChildren(), cmisSession);
        }
    }
}
