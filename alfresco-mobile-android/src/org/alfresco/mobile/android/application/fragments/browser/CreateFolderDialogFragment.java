/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco Mobile SDK.
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
package org.alfresco.mobile.android.application.fragments.browser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.node.create.CreateFolderRequest;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.opendataspace.android.app.R;

public abstract class CreateFolderDialogFragment extends BaseFragment
{

    public static final String TAG = "CreateFolderDialogFragment";

    public static final String ARGUMENT_FOLDER = "folder";

    public CreateFolderDialogFragment()
    {
    }

    public static Bundle createBundle(Folder folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_FOLDER, folder);
        return args;
    }

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.mime_folder);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.folder_create);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.sdk_create_folder, container, false);
        int width = (int) Math.round(UIUtils.getScreenDimension(getActivity())[0] * 0.8);
        v.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

        final EditText tv = (EditText) v.findViewById(R.id.folder_name);

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                CreateFolderDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = UIUtils.initValidation(v, R.string.create);
        bcreate.setEnabled(false);

        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);

                Folder folder = (Folder) getArguments().get(ARGUMENT_FOLDER);

                if (folder != null)
                {
                    OperationsRequestGroup group =
                            new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
                    group.enqueue(new CreateFolderRequest(folder, tv.getText().toString().trim())
                            .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG).setRepoType(alfSession));
                    BatchOperationManager.getInstance(getActivity()).enqueue(group);

                    OperationWaitingDialogFragment.newInstance(CreateFolderRequest.TYPE_ID, R.drawable.ic_add_folder,
                            getString(R.string.folder_create), null, (Folder) getArguments().get(ARGUMENT_FOLDER), 0)
                            .show(getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
                }

                dismiss();
            }
        });

        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (tv.getText().toString().trim().length() == 0)
                {
                    bcreate.setEnabled(false);
                    tv.setError(null);
                }
                else
                {
                    bcreate.setEnabled(true);
                    if (UIUtils.hasInvalidName(tv.getText().toString().trim()))
                    {
                        tv.setError(getString(R.string.filename_error_character));
                        tv.requestFocus();
                        bcreate.setEnabled(false);
                    }
                    else
                    {
                        tv.setError(null);
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        return v;
    }

    @Override
    public void onDestroyView()
    {
        if (getDialog() != null && getRetainInstance())
        {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Avoid background stretching
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
        {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }
}
