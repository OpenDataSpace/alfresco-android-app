package org.opendataspace.android.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.ui.logging.OdsLog;

import java.sql.SQLException;

public class OdsDatabase extends OrmLiteSqliteOpenHelper
{
    private static final String DATABASE_NAME = "odsdata.db";
    private static final String TAG = "odsdb";

    private static final int DATABASE_VERSION = 4;

    private OdsFileInfoDAO files;
    private OdsLinkDAO links;

    public OdsDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource)
    {
        try
        {
            TableUtils.createTable(connectionSource, OdsFileInfo.class);
            TableUtils.createTable(connectionSource, OdsLink.class);
        }
        catch (Exception ex)
        {
            OdsLog.ex(TAG, ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer)
    {
        try
        {
            TableUtils.createTableIfNotExists(connectionSource, OdsFileInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, OdsLink.class);

            if (newVer <= 3)
            {
                DatabaseConnection c = connectionSource.getReadWriteConnection();
                c.executeStatement("ALTER TABLE links ADD COLUMN type INTEGER;", 0);
                c.executeStatement("CREATE INDEX IF NOT EXISTS links_type_idx ON links (type);", 0);
                c.executeStatement("CREATE INDEX IF NOT EXISTS links_nodeid_idx ON links (nodeid);", 0);
                c.executeStatement("UPDATE links SET type = 0;", 0);
            }
        }
        catch (Exception ex)
        {
            OdsLog.ex(TAG, ex);
        }
    }

    @Override
    public void close()
    {
        super.close();
        files = null;
        links = null;
    }

    public OdsFileInfoDAO getFileInfoDAO() throws SQLException
    {
        if (files == null)
        {
            files = new OdsFileInfoDAO(getConnectionSource());
        }

        return files;
    }

    public OdsLinkDAO getLinkDAO() throws SQLException
    {
        if (links == null)
        {
            links = new OdsLinkDAO(getConnectionSource());
        }

        return links;
    }
}
