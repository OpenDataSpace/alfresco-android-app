package org.opendataspace.android.app.account;

import org.alfresco.mobile.android.application.activity.PublicDispatcherActivity;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.opendataspace.android.ui.logging.OdsLog;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class OdsAccountAuthenticator extends AbstractAccountAuthenticator
{
    public static final String ACCOUNT_TYPE = "app.android.opendataspace.org";

    private final Context context;

    public OdsAccountAuthenticator(Context context)
    {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        Intent intent = new Intent(context, PublicDispatcherActivity.class);
        intent.setAction(IntentIntegrator.ACTION_CREATE_ACCOUNT);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
            throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
            Bundle options) throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(String authTokenType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
            throws NetworkErrorException
    {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
            Bundle options) throws NetworkErrorException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account)
            throws NetworkErrorException
    {
        boolean res = false;

        try
        {
            AccountManager am = AccountManager.get(context);
            String pwd = am.getPassword(account);
            res = TextUtils.isEmpty(pwd);
        }
        catch (Exception ex)
        {
            OdsLog.ex("OdsAccountAuthenticator", ex);
        }

        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, res);
        return result;
    }

    public static boolean createSystemAccount(org.alfresco.mobile.android.application.accounts.Account acc,
            Context context)
    {
        android.accounts.Account sysAcc = new android.accounts.Account(acc.getDescription(),
                OdsAccountAuthenticator.ACCOUNT_TYPE);
        android.accounts.AccountManager am = android.accounts.AccountManager.get(context);
        Bundle bu = new Bundle();

        bu.putString(IntentIntegrator.EXTRA_ACCOUNT_ID, String.valueOf(acc.getId()));
        return am.addAccountExplicitly(sysAcc, String.valueOf(acc.getId()), bu);
    }
}
