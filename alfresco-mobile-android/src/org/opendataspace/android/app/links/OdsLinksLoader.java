package org.opendataspace.android.app.links;

import android.content.Context;

import com.j256.ormlite.dao.CloseableIterator;
import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.impl.AbstractAlfrescoSessionImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.opendataspace.android.app.data.OdsDataHelper;
import org.opendataspace.android.app.session.OdsRepositorySession;
import org.opendataspace.android.app.session.OdsTypeDefinition;
import org.opendataspace.android.app.utils.OdsStringUtils;
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
        boolean hasRelations = false;

        if (session instanceof OdsRepositorySession)
        {
            OdsRepositorySession ses = (OdsRepositorySession) session;
            hasRelations = ses.getLinkCapablilty() == OdsRepositorySession.LinkCapablilty.COMBINED;
        }

        LoaderResult<List<OdsLink>> result = new LoaderResult<List<OdsLink>>();
        List<OdsLink> data = new ArrayList<OdsLink>();

        if (hasRelations)
        {
            loadRelations(data);
        }
        else
        {
            loadLocal(data);
        }

        result.setData(data);
        return result;
    }

    private void loadLocal(List<OdsLink> data)
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
            OdsLog.exw("OdsLinksLoader", ex);
        }
        finally
        {
            if (it != null)
            {
                it.closeQuietly();
            }
        }
    }

    private void loadRelations(List<OdsLink> data)
    {
        final Session cmisSession = ((AbstractAlfrescoSessionImpl) session).getCmisSession();
        final OperationContext context = cmisSession.createOperationContext();
        context.setIncludeAllowableActions(false);
        context.setIncludePathSegments(false);
        context.setIncludeRelationships(IncludeRelationships.TARGET);

        try
        {
            checkRelations(data, cmisSession.getObject(node.getIdentifier(), context).getRelationships());
        }
        catch (Exception ex)
        {
            OdsLog.exw("OdsLinksLoader", ex);
        }
    }

    private void checkRelations(List<OdsLink> data, List<Relationship> relationships)
    {
        if (relationships == null)
        {
            return;
        }

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
                List<String> emails = cmo.getPropertyValue(OdsTypeDefinition.EMAIL_PROP_ID);
                link.setType(type);
                link.setEmail(OdsStringUtils.join(emails, ", "));
                link.setExpires((Calendar) cmo.getPropertyValue(PropertyIds.EXPIRATION_DATE));
                link.setMessage((String) cmo.getPropertyValue(OdsTypeDefinition.MESSAGE_PROP_ID));
                link.setName((String) cmo.getPropertyValue(OdsTypeDefinition.SUBJECT_PROP_ID));
                link.setNodeId(node.getIdentifier());
                link.setObjectId(cmo.getId());
                link.setUrl((String) cmo.getPropertyValue(OdsTypeDefinition.URL_PROP_ID));
                link.setRelationId(relationship.getId());

                if (link.isValid())
                {
                    data.add(link);
                }
            }
        }
    }
}
