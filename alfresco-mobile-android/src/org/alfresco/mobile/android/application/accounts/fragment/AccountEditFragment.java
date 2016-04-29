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
package org.alfresco.mobile.android.application.accounts.fragment;

import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.activity.BaseActivity;
import org.alfresco.mobile.android.application.activity.HomeScreenActivity;
import org.alfresco.mobile.android.application.activity.MainActivity;
import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.AccessibilityHelper;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.account.CreateAccountRequest;
import org.alfresco.mobile.android.application.operations.batch.node.favorite.FavoriteNodeRequest;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.account.OdsAccountAuthenticator;
import org.opendataspace.android.app.session.OdsRepositorySession;

import java.net.MalformedURLException;
import java.net.URL;

public class AccountEditFragment extends DialogFragment
{
    public static final String TAG = "AccountEditFragment";

    private Button validate;

    private String url = null;
    private String username = null;
    private String password = null;
    private String description =
            null;

    private Account.ProtocolType proto = Account.ProtocolType.JSON;

    private AccountsReceiver receiver;

    public AccountEditFragment()
    {
        setStyle(android.R.style.Theme_Holo_Light_Dialog, android.R.style.Theme_Holo_Light_Dialog);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // LIFECYCLE
    // ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (getDialog() != null)
        {
            getDialog().setTitle(R.string.account_authentication);
            getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);
        }
        else
        {
            UIUtils.displayTitle(getActivity(), R.string.account_authentication,
                    !(getActivity() instanceof HomeScreenActivity));
        }

        View v = inflater.inflate(R.layout.app_wizard_account_step2, container, false);

        validate = (Button) v.findViewById(R.id.next);
        validate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                validateServer(v);
            }
        });

        final CheckBox sw = (CheckBox) v.findViewById(R.id.repository_https);
        final EditText portForm = (EditText) v.findViewById(R.id.repository_port);
        sw.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!sw.isChecked() &&
                        (portForm.getText().toString().isEmpty() || portForm.getText().toString().equals("443")))
                {
                    portForm.setText("80");
                    AccessibilityHelper.addContentDescription(buttonView, R.string.account_https_off_hint);
                }
                else if (sw.isChecked() &&
                        (portForm.getText().toString().isEmpty() || portForm.getText().toString().equals("80")))
                {
                    portForm.setText("443");
                    AccessibilityHelper.addContentDescription(buttonView, R.string.account_https_on_hint);
                }
            }
        });

        sw.setChecked(true);
        portForm.setText("443");

        // Accessibility
        if (AccessibilityHelper.isEnabled(getActivity()))
        {
            ((EditText) v.findViewById(R.id.repository_username))
                    .setHint(getString(R.string.account_username_required_hint));
            ((EditText) v.findViewById(R.id.repository_password))
                    .setHint(getString(R.string.account_password_required_hint));
            ((EditText) v.findViewById(R.id.repository_hostname))
                    .setHint(getString(R.string.account_hostname_required_hint));
            ((EditText) v.findViewById(R.id.repository_description))
                    .setHint(getString(R.string.account_description_optional_hint));
            sw.setContentDescription(getString(R.string.account_https_on_hint));
            portForm.setHint(getString(R.string.account_port_hint));
            ((EditText) v.findViewById(R.id.repository_servicedocument))
                    .setHint(getString(R.string.account_servicedocument_hint));
        }

        return v;
    }

    @Override
    public void onStart()
    {
        if (receiver == null)
        {
            receiver = new AccountsReceiver();
            IntentFilter filters = new IntentFilter(IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filters);
        }

        if (getDialog() != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_alfresco);
        }

        initForm();

        if (retrieveFormValues())
        {
            validate.setEnabled(true);
        }
        else
        {
            validate.setEnabled(false);
        }

        super.onStart();
    }

    @Override
    public void onPause()
    {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onPause();
    }

    // /////////////////////////////////////////////////////////////
    // INTERNALS
    // ////////////////////////////////////////////////////////////
    private void validateServer(View v)
    {
        if (retrieveFormValues())
        {
            // Remove Keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // Create Account + Session
            OperationsRequestGroup group = new OperationsRequestGroup(getActivity());
            group.enqueue(new CreateAccountRequest(url, username, password, description, proto)
                    .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
            BatchOperationManager.getInstance(getActivity()).enqueue(group);

            if (getActivity() instanceof MainActivity || isAuthenticator())
            {
                OperationWaitingDialogFragment
                        .newInstance(FavoriteNodeRequest.TYPE_ID, R.drawable.ic_onpremise, getString(R.string.account),
                                getString(R.string.account_verify), null, -1)
                        .show(getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);
            }
        }
    }

    private boolean isAuthenticator()
    {
        return getActivity().getIntent().hasExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE) &&
                getActivity() instanceof PublicDispatcherActivity;
    }

    private void initForm()
    {
        int[] ids = new int[] {R.id.repository_username, R.id.repository_hostname, R.id.repository_password,
                R.id.repository_port};
        EditText formValue;
        for (int id : ids)
        {
            formValue = (EditText) findViewByIdInternal(id);
            formValue.addTextChangedListener(watcher);
        }

        Spinner spin = (Spinner) findViewByIdInternal(R.id.repository_proto);
        spin.setSelection(proto == Account.ProtocolType.JSON ? 0 : 1);
    }

    private final TextWatcher watcher = new TextWatcher()
    {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (retrieveFormValues())
            {
                validate.setEnabled(true);
            }
            else
            {
                validate.setEnabled(false);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // Nothing special
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    };

    private boolean retrieveFormValues()
    {

        EditText formValue = (EditText) findViewByIdInternal(R.id.repository_username);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            username = formValue.getText().toString();
        }
        else
        {
            AccessibilityHelper.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        formValue = (EditText) findViewByIdInternal(R.id.repository_description);
        description = formValue.getText().toString();

        formValue = (EditText) findViewByIdInternal(R.id.repository_password);
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            password = formValue.getText().toString();
        }
        else
        {
            AccessibilityHelper.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        formValue = (EditText) findViewByIdInternal(R.id.repository_hostname);
        String host;
        if (formValue != null && formValue.getText() != null && formValue.getText().length() > 0)
        {
            host = formValue.getText().toString();
        }
        else
        {
            AccessibilityHelper.addContentDescription(validate, R.string.account_validate_disable_hint);
            return false;
        }

        if ("".equals(description))
        {
            description = host;
        }

        CheckBox sw = (CheckBox) findViewByIdInternal(R.id.repository_https);
        boolean https = sw.isChecked();
        String protocol = https ? "https" : "http";

        formValue = (EditText) findViewByIdInternal(R.id.repository_port);
        int port;
        if (formValue.getText().length() > 0)
        {
            port = Integer.parseInt(formValue.getText().toString());
        }
        else
        {
            port = (protocol.equals("https")) ? 443 : 80;
        }

        Spinner spin = (Spinner) findViewByIdInternal(R.id.repository_proto);
        proto = spin.getSelectedItemId() == 1 ? Account.ProtocolType.ATOM : Account.ProtocolType.JSON;

        formValue = (EditText) findViewByIdInternal(R.id.repository_servicedocument);
        String servicedocument = formValue.getText().toString();
        URL u;
        try
        {
            if ("".equals(servicedocument))
            {
                servicedocument = proto == Account.ProtocolType.JSON ? OdsRepositorySession.BINDING_JSON :
                        OdsRepositorySession.BINDING_ATOM;
            }

            u = new URL(protocol, host, port, servicedocument);
        }
        catch (MalformedURLException e)
        {
            AccessibilityHelper.addContentDescription(validate, R.string.account_validate_disable_url_hint);
            return false;
        }

        url = u.toString();
        AccessibilityHelper.addContentDescription(validate, R.string.account_validate_hint);

        return true;
    }

    private View findViewByIdInternal(int id)
    {
        if (getDialog() != null)
        {
            return getDialog().findViewById(id);
        }
        else
        {
            return getActivity().findViewById(id);
        }
    }

    private void createAccount(long accountId)
    {
        if (isAuthenticator())
        {
            Bundle bu = new Bundle();
            bu.putLong(IntentIntegrator.EXTRA_ACCOUNT_ID, accountId);
            bu.putString(AccountManager.KEY_ACCOUNT_NAME, description);
            bu.putString(AccountManager.KEY_ACCOUNT_TYPE, OdsAccountAuthenticator.ACCOUNT_TYPE);
            ((PublicDispatcherActivity) getActivity()).setAccountAuthenticatorResult(bu);
            getActivity().finish();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST RECEIVER
    // ///////////////////////////////////////////////////////////////////////////
    private class AccountsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (IntentIntegrator.ACTION_CREATE_ACCOUNT_COMPLETED.equals(intent.getAction()) &&
                    (getActivity() instanceof MainActivity || isAuthenticator()))
            {
                getActivity().getFragmentManager()
                        .popBackStack(AccountEditFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if (intent.getExtras() != null && intent.hasExtra(IntentIntegrator.EXTRA_ACCOUNT_ID))
                {
                    long accountId = intent.getLongExtra(IntentIntegrator.EXTRA_ACCOUNT_ID, -1);

                    AccountsFragment frag = (AccountsFragment) getActivity().getFragmentManager()
                            .findFragmentByTag(AccountsFragment.TAG);

                    if (frag != null)
                    {
                        frag.select(accountId);
                    }

                    ((BaseActivity) getActivity()).setCurrentAccount(accountId);
                    createAccount(accountId);
                }
            }
        }
    }
}
