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

package org.opendataspace.android.app.preferences;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.Account;
import org.opendataspace.android.app.utils.SessionUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Manage global application preferences.
 * 
 * @author Jean Marie Pascal
 */
public class GeneralPreferences extends PreferenceFragment
{

    public static final String TAG = "GeneralPreferencesFragment";

    public static final String HAS_ACCESSED_PAID_SERVICES = "HasAccessedPaidServices";

    public static final String REQUIRES_ENCRYPT = "RequiresEncrypt";

    public static final String ENCRYPTION_USER_INTERACTION = "EncryptionUserInteraction";

    public static final String PRIVATE_FOLDERS = "privatefolders";

    //private static final String PRIVATE_FOLDERS_BUTTON = "privatefoldersbutton";

    private static final String SYNCHRO_PREFIX = "SynchroEnable-";

    private static final String SYNCHRO_WIFI_PREFIX = "SynchroWifiEnable-";

    private static final String SYNCHRO_DISPLAY_PREFIX = "SynchroDisplayEnable-";

    private static final String CERTIFICATE_PREF = "certificate";

    //private Account account;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.general_preferences);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        /*
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Preference privateFoldersPref = findPreference(PRIVATE_FOLDERS_BUTTON);

        // DATA PROTECTION
        if (!sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false))
        {
            privateFoldersPref.setSelectable(false);
            privateFoldersPref.setEnabled(false);
            privateFoldersPref.setSummary(R.string.data_protection_unavailable);
            sharedPref.edit().putBoolean(PRIVATE_FOLDERS, false).commit();
        }
        else
        {
            privateFoldersPref.setSelectable(true);
            privateFoldersPref.setEnabled(true);
            privateFoldersPref.setSummary(sharedPref.getBoolean(PRIVATE_FOLDERS, false) ? R.string.data_protection_on
                    : R.string.data_protection_off);
        }

        privateFoldersPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                final File folder = StorageManager.getPrivateFolder(getActivity(), "", null);
                if (folder != null)
                {
                    DataProtectionUserDialogFragment.newInstance(false).show(getActivity().getFragmentManager(),
                            DataProtectionUserDialogFragment.TAG);
                }
                else
                {
                    MessengerManager.showLongToast(getActivity(), getString(R.string.sdinaccessible));
                }

                return false;
            }
        });

        // PASSCODE
        Boolean passcodeEnable = sharedPref.getBoolean(PasscodePreferences.KEY_PASSCODE_ENABLE, false);
        Preference pref = findPreference(getString(R.string.passcode_title));

        boolean isActivate = sharedPref.getBoolean(HAS_ACCESSED_PAID_SERVICES, false);
        pref.setSelectable(isActivate);
        pref.setEnabled(isActivate);

        int summaryId = R.string.passcode_disable;
        if (passcodeEnable)
        {
            summaryId = R.string.passcode_enable;
        }
        pref.setSummary(summaryId);

        pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Fragment f = new PasscodePreferences();
                FragmentDisplayer.replaceFragment(getActivity(), f, DisplayUtils.getMainPaneId(getActivity()),
                        PasscodePreferences.TAG, true);
                return false;
            }
        });

        // FAVORITE SYNC
        final CheckBoxPreference cpref = (CheckBoxPreference) findPreference(getString(R.string.favorite_sync));
        final CheckBoxPreference wifiPref = (CheckBoxPreference) findPreference(getString(R.string.favorite_sync_wifi));
        account = SessionUtils.getAccount(getActivity());

        if (account == null)
        {
            cpref.setSelectable(false);
            wifiPref.setSelectable(false);
            return;
        }

        final Boolean syncEnable = sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false);
        cpref.setChecked(syncEnable);
        cpref.setTitle(String.format(getString(R.string.settings_favorite_sync), account.getDescription()));

        final Boolean syncWifiEnable = sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), true);

        if (wifiPref != null)
        {
            wifiPref.setChecked(syncWifiEnable);
            if (syncWifiEnable)
            {
                wifiPref.setSummary(R.string.settings_favorite_sync_data_wifi);
            } else
            {
                wifiPref.setSummary(R.string.settings_favorite_sync_data_all);
            }
        }

        cpref.setOnPreferenceClickListener(new OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(final Preference preference)
            {
                boolean isSync = false;
                if (preference instanceof CheckBoxPreference)
                {
                    isSync = ((CheckBoxPreference) preference).isChecked();
                }

                if (isSync)
                {
                    sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), isSync).commit();
                    if (SynchroManager.getInstance(getActivity()).canSync(account))
                    {
                        SynchroManager.getInstance(getActivity()).sync(account);
                    }
                }
                else
                {
                    final OnFavoriteChangeListener favListener = new FavoriteAlertDialogFragment.OnFavoriteChangeListener()
                    {
                        @Override
                        public void onPositive()
                        {
                            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), false).commit();
                            cpref.setChecked(false);
                            SynchroManager.getInstance(getActivity()).unsync(account);
                        }

                        @Override
                        public void onNegative()
                        {
                            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), true).commit();
                            cpref.setChecked(true);
                        }
                    };
                    FavoriteAlertDialogFragment.newInstance(favListener).show(getActivity().getFragmentManager(),
                            FavoriteAlertDialogFragment.TAG);
                    return true;
                }

                return false;
            }
        });

        // Check if 3G Present
        if (!ConnectivityUtils.hasMobileConnectivity(getActivity()) && wifiPref != null)
        {
            final PreferenceCategory mCategory = (PreferenceCategory) findPreference(getString(R.string.favorite_sync_group));
            mCategory.removePreference(wifiPref);
        }

        if (wifiPref != null)
        {
            wifiPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(final Preference preference)
                {
                    boolean isWifiOnly = false;
                    if (preference instanceof CheckBoxPreference)
                    {
                        isWifiOnly = ((CheckBoxPreference) preference).isChecked();
                    }
                    sharedPref.edit().putBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), isWifiOnly).commit();

                    if (isWifiOnly)
                    {
                        wifiPref.setSummary(R.string.settings_favorite_sync_data_wifi);
                    } else
                    {
                        wifiPref.setSummary(R.string.settings_favorite_sync_data_all);
                    }

                    return false;
                }
            });
        }
         */
        getActivity().invalidateOptionsMenu();

    }

    public static boolean hasWifiOnlySync(final Context context, final Account account)
    {
        if (account != null)
        {
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPref.getBoolean(SYNCHRO_WIFI_PREFIX + account.getId(), false);
        }
        return false;
    }

    public static boolean hasActivateSync(final Context context, final Account account)
    {
        if (account != null)
        {
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPref.getBoolean(SYNCHRO_PREFIX + account.getId(), false);
        }
        return false;
    }

    public static void setActivateSync(final Activity activity, final boolean isActive)
    {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            sharedPref.edit().putBoolean(SYNCHRO_PREFIX + account.getId(), isActive).commit();
        }
    }

    public static void setDisplayActivateSync(final Activity activity, final boolean isActive)
    {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            sharedPref.edit().putBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), isActive).commit();
        }
    }

    public static boolean hasDisplayedActivateSync(final Activity activity)
    {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        if (SessionUtils.getAccount(activity) != null)
        {
            final Account account = SessionUtils.getAccount(activity);
            return sharedPref.getBoolean(SYNCHRO_DISPLAY_PREFIX + account.getId(), false);
        }
        return false;
    }

    public static void setCertificatePref(final int checkSertfc, final Context c)
    {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        if(sharedPref == null)
        {
            return;
        }

        try{
            final Editor editor = sharedPref.edit();
            editor.putInt(CERTIFICATE_PREF, checkSertfc);
            editor.commit();

        }catch(final Exception e){
            Log.e("", "ERROR: set pref failed: "+e.getMessage());
        }
    }

    public static int getCertificatePref(final Context c)
    {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        if(sharedPref == null)
        {
            return -1;
        }

        return sharedPref.getInt(CERTIFICATE_PREF, -1);
    }
}
