package org.opendataspace.android.app.links;

import android.content.Context;

import com.j256.ormlite.dao.CloseableIterator;
import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.opendataspace.android.app.data.OdsDataHelper;

public class OdsLinksLoader extends AbstractPagingLoader<LoaderResult<CloseableIterator<OdsLink>>>
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
    public LoaderResult<CloseableIterator<OdsLink>> loadInBackground()
    {
        LoaderResult<CloseableIterator<OdsLink>> result = new LoaderResult<CloseableIterator<OdsLink>>();
        CloseableIterator<OdsLink> it = null;

        try
        {
            it = OdsDataHelper.getHelper().getLinkDAO().getLinksByNode(node.getIdentifier(), type);
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(it);
        return result;
    }
}
