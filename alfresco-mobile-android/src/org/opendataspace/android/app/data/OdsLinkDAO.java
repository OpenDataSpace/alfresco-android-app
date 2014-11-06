package org.opendataspace.android.app.data;

import java.sql.SQLException;

import org.opendataspace.android.app.links.OdsLink;

import android.text.TextUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class OdsLinkDAO extends BaseDaoImpl<OdsLink, Long>
{
    protected OdsLinkDAO(ConnectionSource connectionSource, Class<OdsLink> dataClass) throws SQLException
    {
        super(connectionSource, dataClass);
    }

    public CloseableIterator<OdsLink> getLinksByNode(String id) throws SQLException
    {
        QueryBuilder<OdsLink, Long> queryBuilder = queryBuilder();
        queryBuilder.where().eq(OdsLink.NODE_ID_FIELD, id);
        return iterator(queryBuilder.prepare());
    }

    public void process(OdsLink link) throws Exception
    {
        if (TextUtils.isEmpty(link.getObjectId()))
        {
            if (link.getId() != -1)
            {
                delete(link);
            }

            return;
        }

        createOrUpdate(link);
    }
}
