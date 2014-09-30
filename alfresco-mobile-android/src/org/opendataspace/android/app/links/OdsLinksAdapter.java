package org.opendataspace.android.app.links;

import java.util.List;

import org.alfresco.mobile.android.application.fragments.menu.MenuActionItem;
import org.alfresco.mobile.android.application.operations.OperationRequest;
import org.alfresco.mobile.android.application.operations.OperationsRequestGroup;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.fragments.BaseListAdapter;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.fragments.OdsLinksFragment;
import org.opendataspace.android.app.operations.OdsUpdateLinkRequest;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class OdsLinksAdapter extends BaseListAdapter<OdsLink, GenericViewHolder> implements OnMenuItemClickListener
{
    private OdsLinksFragment fr;
    private OdsLink menuCtx;

    public OdsLinksAdapter(OdsLinksFragment fr, int textViewResourceId, List<OdsLink> objects)
    {
        super(fr.getActivity(), textViewResourceId, objects);
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
        this.fr = fr;
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, OdsLink item)
    {
        vh.bottomText.setText(item.getUrl());
        vh.choose.setVisibility(View.GONE);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, OdsLink item)
    {
        vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_all_sites_light));

        vh.choose.setVisibility(View.VISIBLE);
        vh.choose.setTag(R.id.link_action, item);
        vh.choose.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                menuCtx = (OdsLink) v.getTag(R.id.link_action);
                PopupMenu popup = new PopupMenu(fr.getActivity(), v);
                MenuItem mi;

                mi = popup.getMenu().add(Menu.NONE, MenuActionItem.MENU_COPY, Menu.FIRST + MenuActionItem.MENU_COPY,
                        R.string.link_copy_url);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                mi = popup.getMenu().add(Menu.NONE, MenuActionItem.MENU_EDIT, Menu.FIRST + MenuActionItem.MENU_EDIT,
                        R.string.edit);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                mi = popup.getMenu().add(Menu.NONE, MenuActionItem.MENU_DELETE,
                        Menu.FIRST + MenuActionItem.MENU_DELETE, R.string.delete);
                mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                popup.setOnMenuItemClickListener(OdsLinksAdapter.this);
                popup.show();
            }
        });
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, OdsLink item)
    {
        vh.topText.setText(item.getName());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        boolean onMenuItemClick = true;

        switch (item.getItemId())
        {
        case MenuActionItem.MENU_EDIT:
            edit(menuCtx);
            break;
        case MenuActionItem.MENU_DELETE:
            delete(menuCtx);
            break;
        case MenuActionItem.MENU_COPY:
            copyLink();
            break;
        default:
            onMenuItemClick = false;
            break;
        }
        return onMenuItemClick;
    }

    private void copyLink()
    {
        final ClipboardManager clipboard = (ClipboardManager) fr.getActivity().getSystemService(
                Context.CLIPBOARD_SERVICE);

        clipboard.setPrimaryClip(ClipData.newPlainText("url", menuCtx.getUrl()));
    }

    private void edit(OdsLink link)
    {
        OdsLinksFragment.editLink(((OdsLinksFragment) fr).getNode(), link, fr.getActivity().getFragmentManager());
    }

    private void delete(final OdsLink link)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(fr.getActivity());
        builder.setTitle(R.string.delete);
        builder.setMessage(link.getName());

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                OperationsRequestGroup group = new OperationsRequestGroup(fr.getActivity(), SessionUtils.getAccount(fr
                        .getActivity()));

                group.enqueue(new OdsUpdateLinkRequest(null, link)
                        .setNotificationVisibility(OperationRequest.VISIBILITY_TOAST));

                BatchOperationManager.getInstance(fr.getActivity()).enqueue(group);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
