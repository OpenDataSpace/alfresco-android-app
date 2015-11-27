package org.opendataspace.android.app.data;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;

import java.sql.SQLException;

public class OdsFileInfoDAO extends BaseDaoImpl<OdsFileInfo, String>
{
    protected OdsFileInfoDAO(ConnectionSource connectionSource) throws SQLException
    {
        super(connectionSource, OdsFileInfo.class);
    }

    public CloseableIterator<OdsFileInfo> getLinksByFolder(String id, int type) throws SQLException
    {
        QueryBuilder<OdsFileInfo, String> queryBuilder = queryBuilder();
        queryBuilder.where().eq(OdsFileInfo.FOLDER_ID_FIELD, id).and().eq(OdsFileInfo.TYPE_FIELD, type);
        return iterator(queryBuilder.prepare());
    }
}
