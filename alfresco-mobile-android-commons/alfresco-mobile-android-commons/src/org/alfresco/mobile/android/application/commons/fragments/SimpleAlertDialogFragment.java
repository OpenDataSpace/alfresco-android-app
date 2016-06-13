/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * <p>
 * This file is part of Alfresco Mobile for Android.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.commons.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Gravity;
import android.widget.TextView;

public class SimpleAlertDialogFragment extends DialogFragment
{

    public static final String TAG = "SimpleAlertDialogFragment";

    /**
     * Associated Value must be a String id (int value).
     */
    public static final String PARAM_TITLE = "alertDialogFragment_TitleId";

    /**
     * Associated Value must be a Drawable id (int value).
     */
    public static final String PARAM_ICON = "alertDialogFragment_IconId";

    /**
     * Associated Value must be a String id (int value).
     */
    public static final String PARAM_POSITIVE_BUTTON = "alertDialogFragment_PositiveId";

    /**
     * Associated Value must be a String id (int value).
     */
    public static final String PARAM_MESSAGE = "alertDialogFragment_MessageId";

    /**
     * Associated Value must be a String (String value).
     */
    public static final String PARAM_MESSAGE_STRING = "alertDialogFragment_MessageString";

    /**
     * Associated Value must be a String id (int value).
     */
    public static final String PARAM_CUSTOM_BUTTON = "alertDialogFragment_CustomId";

    public static SimpleAlertDialogFragment newInstance(Bundle b)
    {
        SimpleAlertDialogFragment frag = new SimpleAlertDialogFragment();
        if (b != null)
        {
            frag.setArguments(b);
        }
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Bundle arguments = getArguments();

        if (arguments == null)
        {
            return null;
        }

        int titleId = arguments.getInt(PARAM_TITLE);
        int iconId = arguments.getInt(PARAM_ICON);
        int positiveId = arguments.getInt(PARAM_POSITIVE_BUTTON);
        String message = "";
        if (arguments.containsKey(PARAM_MESSAGE))
        {
            message = getString(arguments.getInt(PARAM_MESSAGE));
        }
        else if (arguments.containsKey(PARAM_MESSAGE_STRING))
        {
            message = arguments.getString(PARAM_MESSAGE_STRING);
        }

        Builder builder = new AlertDialog.Builder(getActivity()).setIcon(iconId).setTitle(titleId)
                .setMessage(Html.fromHtml(message)).setPositiveButton(positiveId, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        dialog.dismiss();
                    }
                });

        if (arguments.containsKey(PARAM_CUSTOM_BUTTON))
        {
            final int id = arguments.getInt(PARAM_CUSTOM_BUTTON);
            builder.setNeutralButton(id, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    Intent intent = new Intent("org.alfresco.mobile.android.intent.ACTION_DIALOG_EXTRA");
                    intent.putExtra("org.alfresco.mobile.android.intent.EXTRA_DIALOG_ACTION", id);
                    LocalBroadcastManager.getInstance(getDialog().getContext()).sendBroadcast(intent);
                }
            });
        }

        return builder.create();
    }

    @Override
    public void onResume()
    {
        if (getDialog() != null)
        {
            TextView messageText = (TextView) getDialog().findViewById(android.R.id.message);
            messageText.setGravity(Gravity.CENTER);
            getDialog().show();
        }
        super.onResume();
    }
}
