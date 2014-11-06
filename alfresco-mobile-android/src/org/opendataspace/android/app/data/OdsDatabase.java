package org.opendataspace.android.app.data;

import java.sql.SQLException;

import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.ui.logging.OdsLog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class OdsDatabase extends OrmLiteSqliteOpenHelper
{
    private static final String DATABASE_NAME = "odsdata.db";
    private static final int DATABASE_VERSION = 1;

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
            TableUtils.createTable(connectionSource, OdsLink.class);
        } catch (Exception ex)
        {
            OdsLog.ex("odsdb", ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVer, int newVer)
    {
        // nothing
    }

    public OdsLinkDAO getLinkDAO() throws SQLException
    {
        if (links == null)
        {
            links = new OdsLinkDAO(getConnectionSource(), OdsLink.class);
        }

        return links;
    }

    @Override
    public void close()
    {
        super.close();
        links = null;
    }
}
