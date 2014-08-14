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
package org.alfresco.mobile.android.application;

import java.io.File;

import org.alfresco.mobile.android.application.accounts.AccountManager;
import org.alfresco.mobile.android.application.preferences.GeneralPreferences;
import org.alfresco.mobile.android.application.utils.IOUtils;
import org.opendataspace.android.ui.logging.OdsLog;

import android.app.Application;
import android.preference.PreferenceManager;

public class AlfrescoApplication extends Application
{

    private ApplicationManager helper;

    @Override
    public void onCreate()
    {
        super.onCreate();

        File fld = getExternalFilesDir(null).getParentFile();
        fld = IOUtils.createFolder(fld,  "logs");
        OdsLog.init(fld.getAbsolutePath(), PreferenceManager.getDefaultSharedPreferences(this).getBoolean(GeneralPreferences.ODS_LOGGING, false));

        helper = ApplicationManager.getInstance(this);
        helper.setAccountManager(AccountManager.getInstance(this));
    }
}
