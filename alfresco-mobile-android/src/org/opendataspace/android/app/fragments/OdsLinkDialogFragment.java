package org.opendataspace.android.app.fragments;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.fragments.operations.OperationWaitingDialogFragment;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.application.utils.UIUtils;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.links.OdsLink;
import org.opendataspace.android.app.operations.OdsUpdateLinkRequest;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class OdsLinkDialogFragment extends BaseFragment
{
    public static final String TAG = "OdsLinkDialogFragment";

    public static final String ARGUMENT_NODE = "node";
    public static final String ARGUMENT_LINK = "link";

    public OdsLinkDialogFragment()
    {
    }

    public static Bundle createBundle(Node node, OdsLink link)
    {
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_NODE, node);
        args.putSerializable(ARGUMENT_LINK, link);
        return args;
    }

    public static OdsLinkDialogFragment newInstance(Node node, OdsLink link)
    {
        OdsLinkDialogFragment adf = new OdsLinkDialogFragment();
        adf.setArguments(createBundle(node, link));
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

        int width = (int) Math
                .round(UIUtils.getScreenDimension(getActivity())[0]
                        * (Float.parseFloat(getResources().getString(android.R.dimen.dialog_min_width_major).replace(
                                "%", "")) * 0.01));
        v.setLayoutParams(new LayoutParams(width, LayoutParams.MATCH_PARENT));

        OdsLink lnk = (OdsLink) getArguments().getSerializable(ARGUMENT_LINK);
        final EditText tv = (EditText) v.findViewById(R.id.link_name);

        tv.setText(lnk.getName());

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                OdsLinkDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = UIUtils.initValidation(v, R.string.create);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tv.getWindowToken(), 0);

                Node nod = (Node) getArguments().getParcelable(ARGUMENT_NODE);
                OdsLink lnk = (OdsLink) getArguments().getSerializable(ARGUMENT_LINK);
                lnk.setName(tv.getText().toString().trim());

                OperationsRequestGroup group = new OperationsRequestGroup(getActivity(), SessionUtils
                        .getAccount(getActivity()));
                group.enqueue(new OdsUpdateLinkRequest(nod, lnk)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_DIALOG));
                BatchOperationManager.getInstance(getActivity()).enqueue(group);

                OperationWaitingDialogFragment.newInstance(OdsUpdateLinkRequest.TYPE_ID, R.drawable.ic_add,
                        getString(R.string.links_add), null, nod, 0).show(
                        getActivity().getFragmentManager(), OperationWaitingDialogFragment.TAG);

                dismiss();
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
