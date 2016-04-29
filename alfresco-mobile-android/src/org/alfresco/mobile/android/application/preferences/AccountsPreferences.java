/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * <p/>
 * This file is part of Alfresco Mobile for Android.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountManager;

/**
 * Manage application preferences associated to accounts objects.
 *
 * @author Jean Marie Pascal
 */
public final class AccountsPreferences
{
    private AccountsPreferences()
    {
    }

    public static final String ACCOUNT_PREFS = "org.alfresco.mobile.android.account.preferences";

    public static final String ACCOUNT_DEFAULT = "org.alfresco.mobile.android.account.preferences.default";
    public static final String ACCOUNT_VERSION = "org.alfresco.mobile.android.account.preferences.version";
    public static final int CURRENT_VERSION = 1;

    public static Account getDefaultAccount(Context context)
    {
        // Default account to load
        SharedPreferences settings = context.getSharedPreferences(ACCOUNT_PREFS, 0);
        long id = settings.getLong(ACCOUNT_DEFAULT, -1);
        if (id == -1)
        {
            return AccountManager.retrieveFirstAccount(context);
        }
        else
        {
            Account acc = AccountManager.retrieveAccount(context, id);
            if (acc == null)
            {
                acc = AccountManager.retrieveFirstAccount(context);
                if (acc != null)
                {
                    setDefaultAccount(context, acc.getId());
                }
            }
            return acc;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setDefaultAccount(Context context, long id)
    {
        SharedPreferences settings = context.getSharedPreferences(ACCOUNT_PREFS, 0);
        if (settings != null)
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong(AccountsPreferences.ACCOUNT_DEFAULT, id);
            editor.commit();
        }
    }

}
