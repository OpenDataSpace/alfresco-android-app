/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.security;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.mobile.android.application.ApplicationManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.ui.logging.OdsLog;
import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.fragments.fileexplorer.FileExplorerHelper;
import org.alfresco.mobile.android.application.preferences.AccountsPreferences;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class DataCleaner extends AsyncTask<String, Integer, Boolean>
{
    private static final String TAG = "DataCleaner";

    private List<File> listingFiles = new ArrayList<File>();

    private Activity activity;

    public DataCleaner(Activity activity)
    {
        super();
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(String... params)
    {
        try
        {
            // Remove preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
            Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();
            SharedPreferences settings = activity.getSharedPreferences(AccountsPreferences.ACCOUNT_PREFS, 0);
            editor = settings.edit();
            editor.clear();
            editor.commit();
            SharedPreferences prefs = activity.getSharedPreferences(FileExplorerHelper.FILEEXPLORER_PREFS, 0);
            editor = prefs.edit();
            editor.clear();
            editor.commit();

            // Remove All Accounts
            activity.getContentResolver().delete(AccountManager.CONTENT_URI, null, null);

            //Delete loaded accounts
            ApplicationManager.getInstance(activity).clear();

            // Find folders
            File cache = activity.getCacheDir();
            File folder = activity.getExternalFilesDir(null);

            listingFiles.add(cache);
            listingFiles.add(folder);

            // Remove Files/folders
            for (File file : listingFiles)
            {
                if (file.exists())
                {
                    if (file.isDirectory())
                    {
                        recursiveDelete(file);
                    }
                    else
                    {
                        file.delete();
                    }
                }
            }
            return true;
        }
        catch (Exception fle)
        {
            OdsLog.ex(TAG, fle);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean statut)
    {
        if (statut)
        {
            MessengerManager.showLongToast(activity, activity.getString(R.string.passcode_erase_data_complete));
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finish();
        }
    }

    private boolean recursiveDelete(File file)
    {
        File[] files = file.listFiles();
        File childFile;
        if (files != null)
        {
            for (int x = 0; x < files.length; x++)
            {
                childFile = files[x];
                if (childFile.isDirectory())
                {
                    if (!recursiveDelete(childFile)) { return false; }
                }
                else
                {
                    if (!childFile.delete()) { return false; }
                }
            }
        }
        if (!file.delete()) { return false; }

        return true;
    }
}
