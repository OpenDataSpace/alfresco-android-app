package org.opendataspace.android.app.links;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.api.asynchronous.AbstractPagingLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

import android.content.Context;

public class OdsLinksLoader extends AbstractPagingLoader<LoaderResult<PagingResult<OdsLink>>>
{
    public static final int ID = OdsLinksLoader.class.hashCode();

    @SuppressWarnings("unused")
    private Node node;

    public OdsLinksLoader(Context context, AlfrescoSession session, Node node)
    {
        super(context);
        this.session = session;
        this.node = node;
    }

    @Override
    public LoaderResult<PagingResult<OdsLink>> loadInBackground()
    {
        LoaderResult<PagingResult<OdsLink>> result = new LoaderResult<PagingResult<OdsLink>>();
        PagingResult<OdsLink> pagingResult = null;

        try
        {
            pagingResult = new PagingResult<OdsLink>()
            {
                private static final long serialVersionUID = 1L;

                private List<OdsLink> ls = new ArrayList<OdsLink>();

                @Override
                public Boolean hasMoreItems()
                {
                    return false;
                }

                @Override
                public int getTotalItems()
                {
                    return ls.size();
                }

                @Override
                public List<OdsLink> getList()
                {
                    return ls;
                }
            };
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(pagingResult);
        return result;
    }
}
