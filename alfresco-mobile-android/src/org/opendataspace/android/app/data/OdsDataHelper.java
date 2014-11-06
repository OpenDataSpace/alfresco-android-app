package org.opendataspace.android.app.data;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class OdsDataHelper
{
    private static OdsDatabase databaseHelper;

    public static OdsDatabase getHelper()
    {
        return databaseHelper;
    }

    public static void setHelper(Context context)
    {
        databaseHelper = OpenHelperManager.getHelper(context, OdsDatabase.class);
    }

    public static void releaseHelper()
    {
        OpenHelperManager.releaseHelper();
        databaseHelper = null;
    }
}
