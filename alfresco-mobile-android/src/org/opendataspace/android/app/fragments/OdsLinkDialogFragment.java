package org.opendataspace.android.app.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MessengerManager;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.operations.OdsUpdateLinkRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class OdsLinkDialogFragment extends BaseFragment
{
    public static final String TAG = "OdsLinkDialogFragment";

    public static final String ARGUMENT_LINK = "link";

    private EditText tvn, tve, tvp, tvm;
    private TextView dpe;

    private static abstract class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener
    {
    }

    public OdsLinkDialogFragment()
    {
    }

    public static Bundle createBundle(OdsLink link)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_LINK, link);
        return args;
    }

    public static OdsLinkDialogFragment newInstance(OdsLink link)
    {
        OdsLinkDialogFragment adf = new OdsLinkDialogFragment();
        adf.setArguments(createBundle(link));
        adf.setRetainInstance(true);
        return adf;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        alfSession = SessionUtils.getSession(getActivity());
        SessionUtils.checkSession(getActivity(), alfSession);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart()
    {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_site_flatten);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.links_add);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.ods_link, container, false);

        int width = (int) Math.round(UIUtils.getScreenDimension(getActivity())[0] *
                (Float.parseFloat(getResources().getString(android.R.dimen.dialog_min_width_major).replace("%", "")) *
                        0.01));
        v.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

        final OdsLink lnk = (OdsLink) getArguments().getSerializable(ARGUMENT_LINK);
        final Calendar exp = lnk.getExpires();
        final Button bcreate =
                UIUtils.initValidation(v, TextUtils.isEmpty(lnk.getObjectId()) ? R.string.create : R.string.update);

        tvn = (EditText) v.findViewById(R.id.link_name);
        tve = (EditText) v.findViewById(R.id.link_email);
        tvp = (EditText) v.findViewById(R.id.link_password);
        tvm = (EditText) v.findViewById(R.id.link_message);
        dpe = (TextView) v.findViewById(R.id.link_expires);

        tvn.setText(lnk.getName());
        tve.setText(lnk.getEmail());
        tvp.setText(lnk.getPassword());
        tvm.setText(lnk.getMessage());
        dpe.setText(SimpleDateFormat.getDateInstance().format(exp.getTime()));

        if (!TextUtils.isEmpty(lnk.getObjectId()))
        {
            tvn.setEnabled(false);
            tve.setEnabled(false);
            tvm.setEnabled(false);
        }

        dpe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = new DatePickerFragment()
                {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState)
                    {
                        return new DatePickerDialog(getActivity(), this, exp.get(Calendar.YEAR),
                                exp.get(Calendar.MONDAY) + 1, exp.get(Calendar.DATE));
                    }

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);

                        if (cal.before(Calendar.getInstance()))
                        {
                            MessengerManager.showToast(getActivity(), R.string.link_date_invalid);
                            return;
                        }

                        lnk.setExpires(cal);
                        dpe.setText(SimpleDateFormat.getDateInstance().format(lnk.getExpires().getTime()));
                        bcreate.setEnabled(validate(lnk));
                    }
                };

                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                OdsLinkDialogFragment.this.dismiss();
            }
        });

        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!validate(lnk))
                {
                    return;
                }

                InputMethodManager imm =
                        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvn.getWindowToken(), 0);

                OperationsRequestGroup group =
                        new OperationsRequestGroup(getActivity(), SessionUtils.getAccount(getActivity()));
                group.enqueue(
                        new OdsUpdateLinkRequest(lnk).setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                BatchOperationManager.getInstance(getActivity()).enqueue(group);

                OperationWaitingDialogFragment
                        .newInstance(OdsUpdateLinkRequest.TYPE_ID, R.drawable.ic_add, getString(R.string.links_add),
                                null, null, 0)
                        .show(getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);

                dismiss();
            }
        });

        TextWatcher tv = new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                bcreate.setEnabled(validate(lnk));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        };

        tvn.addTextChangedListener(tv);
        tve.addTextChangedListener(tv);
        tvp.addTextChangedListener(tv);
        tvm.addTextChangedListener(tv);
        bcreate.setEnabled(validate(lnk));
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

    private boolean validate(OdsLink lnk)
    {
        lnk.setEmail(tve.getText().toString().trim());
        lnk.setMessage(tvm.getText().toString().trim());
        lnk.setName(tvn.getText().toString().trim());
        lnk.setPassword(tvp.getText().toString().trim());

        return lnk.isValid();
    }
}
