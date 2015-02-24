package org.opendataspace.android.app.data;

import java.sql.SQLException;

import org.opendataspace.android.app.fileinfo.OdsFileInfo;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class OdsFileInfoDAO extends BaseDaoImpl<OdsFileInfo, String>
{
    protected OdsFileInfoDAO(ConnectionSource connectionSource, Class<OdsFileInfo> dataClass) throws SQLException
    {
        super(connectionSource, dataClass);
    }

    public CloseableIterator<OdsFileInfo> getInfoByFolder(String id, int mask) throws SQLException
    {
        QueryBuilder<OdsFileInfo, String> queryBuilder = queryBuilder();
        queryBuilder.where().eq(OdsFileInfo.FOLDER_ID_FIELD, id).and().rawComparison(OdsFileInfo.TYPE_FIELD, "&", mask);
        return iterator(queryBuilder.prepare());
    }

    public CloseableIterator<OdsFileInfo> getInfo(int mask) throws SQLException
    {
        QueryBuilder<OdsFileInfo, String> queryBuilder = queryBuilder();
        queryBuilder.where().rawComparison(OdsFileInfo.TYPE_FIELD, "&", mask);
        return iterator(queryBuilder.prepare());
    }

    public CloseableIterator<OdsFileInfo> getInfo(int mask, boolean foldersOnly) throws SQLException
    {
        QueryBuilder<OdsFileInfo, String> queryBuilder = queryBuilder();
        Where<OdsFileInfo, String> w = queryBuilder.where();

        if (foldersOnly)
        {
            w.rawComparison(OdsFileInfo.TYPE_FIELD, "&", mask | OdsFileInfo.TYPE_DIRECTORY);
        }
        else
        {
            w.rawComparison(OdsFileInfo.TYPE_FIELD, "&", mask).and()
                    .raw(OdsFileInfo.TYPE_FIELD + " & " + String.valueOf(OdsFileInfo.TYPE_DIRECTORY) + " = 0");
        }

        return iterator(queryBuilder.prepare());
    }
}
