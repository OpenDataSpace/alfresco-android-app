package org.opendataspace.android.app.links;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.opendataspace.android.app.data.OdsDataHelper;
import com.j256.ormlite.dao.CloseableIterator;

import android.content.Context;

public class OdsLinksLoader extends AbstractPagingLoader<LoaderResult<CloseableIterator<OdsLink>>>
{
    public static final int ID = OdsLinksLoader.class.hashCode();

    private Node node;

    public OdsLinksLoader(Context context, AlfrescoSession session, Node node)
    {
        super(context);
        this.session = session;
        this.node = node;
    }

    @Override
    public LoaderResult<CloseableIterator<OdsLink>> loadInBackground()
    {
        LoaderResult<CloseableIterator<OdsLink>> result = new LoaderResult<CloseableIterator<OdsLink>>();
        CloseableIterator<OdsLink> it = null;

        try
        {
            it = OdsDataHelper.getHelper().getLinkDAO().getLinksByNode(node.getIdentifier());
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(it);
        return result;
    }
}
