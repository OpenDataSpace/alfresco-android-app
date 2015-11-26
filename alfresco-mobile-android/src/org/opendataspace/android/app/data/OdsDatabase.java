package org.opendataspace.android.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;
import org.opendataspace.android.ui.logging.OdsLog;

import java.sql.SQLException;

public class OdsDatabase extends OrmLiteSqliteOpenHelper
{
    private static final String DATABASE_NAME = "odsdata.db";
    private static final String TAG = "odsdb";

    private static final int DATABASE_VERSION = 3;

    private OdsFileInfoDAO files;

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
    }

    public OdsFileInfoDAO getFileInfoDAO() throws SQLException
    {
        if (files == null)
        {
            files = new OdsFileInfoDAO(getConnectionSource(), OdsFileInfo.class);
        }

        return files;
    }
}