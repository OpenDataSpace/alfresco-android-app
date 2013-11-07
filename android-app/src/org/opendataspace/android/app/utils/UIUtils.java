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
package org.opendataspace.android.app.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendataspace.android.app.R;
import org.opendataspace.android.app.accounts.AccountManager;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Utility around UI Management.
 * 
 * @author Jean Marie Pascal
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class UIUtils
{

    /**
     * Set the background view with the drawable associated.
     * 
     * @param v
     * @param background
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View v, Drawable background)
    {
        if (AndroidVersion.isJBOrAbove())
        {
            v.setBackground(background);
        }
        else
        {
            v.setBackgroundDrawable(background);
        }
    }

    @SuppressWarnings("deprecation")
	public static int[] getScreenDimension(Activity activity)
    {
        int width = 0;
        int height = 0;

        Display display = activity.getWindowManager().getDefaultDisplay();
        if (AndroidVersion.isHCMR2OrAbove())
        {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        }
        else
        {
            width = display.getWidth(); // deprecated
            height = display.getHeight(); // deprecated
        }

        return new int[] { width, height };
    }

    private static final Pattern NAME_PATTERN = Pattern
            .compile("(.*[\"\\*\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)");
    
    public static boolean hasInvalidName(String name)
    {
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }

    public static void displayTitle(Activity activity, int titleId)
    {
        displayTitle(activity, activity.getString(titleId));
    }

    public static void displayTitle(Activity activity, String title)
    {
        if (activity.getActionBar() != null)
        {
            ActionBar bar = activity.getActionBar();

            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayShowCustomEnabled(true);
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
            bar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO, ActionBar.DISPLAY_USE_LOGO);
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            if (AndroidVersion.isICSOrAbove())
            {
                bar.setHomeButtonEnabled(true);
            }
            activity.getActionBar().setDisplayHomeAsUpEnabled(true);

            View v = bar.getCustomView();
            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.app_title, null);
            }

            TextView tv = (TextView) v.findViewById(R.id.toptext);
            if (SessionUtils.getAccount(activity) != null && AccountManager.getInstance(activity).hasMultipleAccount())
            {
                tv.setText(SessionUtils.getAccount(activity).getDescription());
                tv.setVisibility(View.VISIBLE);
            }
            else
            {
                tv.setVisibility(View.GONE);
            }
            tv = (TextView) v.findViewById(R.id.bottomtext);
            tv.setText(title);

            if (bar.getCustomView() == null)
            {
                bar.setCustomView(v);
            }
            
            activity.invalidateOptionsMenu();
        }
    }
}
