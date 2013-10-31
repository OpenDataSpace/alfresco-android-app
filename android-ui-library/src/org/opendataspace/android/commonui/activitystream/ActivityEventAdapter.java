/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.commonui.activitystream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendataspace.android.cmisapi.constants.CloudConstant;
import org.opendataspace.android.cmisapi.constants.OnPremiseConstant;
import org.opendataspace.android.cmisapi.model.ActivityEntry;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;
import org.opendataspace.android.commonui.fragments.BaseListAdapter;
import org.opendataspace.android.commonui.manager.MimeTypeManager;
import org.opendataspace.android.commonui.manager.RenditionManager;
import org.opendataspace.android.commonui.utils.GenericViewHolder;
import org.opendataspace.android.commonui.R;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;

/**
 * Provides access to activity entries and displays them as a view based on
 * GenericViewHolder.
 * 
 * @author Jean Marie Pascal
 */
public class ActivityEventAdapter extends BaseListAdapter<ActivityEntry, GenericViewHolder>
{

    private RenditionManager renditionManager;

    public ActivityEventAdapter(Activity context, AlfrescoSession session, int textViewResourceId,
            List<ActivityEntry> listItems)
    {
        super(context, textViewResourceId, listItems);
        this.vhClassName = GenericViewHolder.class.getCanonicalName();
        this.renditionManager = new RenditionManager(context, session);
    }

    @Override
    protected void updateTopText(GenericViewHolder vh, ActivityEntry item)
    {
        vh.topText.setMaxLines(3);
        vh.topText.setTypeface(Typeface.DEFAULT);
        vh.topText.setText(Html.fromHtml(getActivityTypeMessage(item)));
    }

    @Override
    protected void updateBottomText(GenericViewHolder vh, ActivityEntry item)
    {
        String s = "";
        if (item.getCreatedAt() != null)
        {
            s = formatDate(getContext(), item.getCreatedAt().getTime());
        }
        vh.bottomText.setText(s);
    }

    @Override
    protected void updateIcon(GenericViewHolder vh, ActivityEntry item)
    {
        switch (iconItemType)
        {
            case DISPLAY_ICON_NONE:
                vh.icon.setVisibility(View.GONE);
                break;
            case DISPLAY_ICON_DEFAULT:
                vh.icon.setImageDrawable(getContext().getResources().getDrawable(getFileDrawableId(item)));
                break;
            case DISPLAY_ICON_CREATOR:
                getCreatorAvatar(vh, item);
                break;
            default:
                break;
        }
    }

    private void getCreatorAvatar(GenericViewHolder vh, ActivityEntry item)
    {
        String type = item.getType();
        String tmp = null;

        if (type.startsWith(PREFIX_FILE))
        {
            renditionManager.display(vh.icon, item.getCreatedBy(), getFileDrawableId(item));
        }
        else if (type.startsWith(PREFIX_GROUP))
        {
            vh.icon.setImageDrawable(getContext().getResources().getDrawable(getFileDrawableId(item)));
        }
        else if (type.startsWith(PREFIX_USER))
        {
            tmp = getData(item, CloudConstant.MEMEBERUSERNAME_VALUE);
            if (tmp.isEmpty())
            {
                tmp = null;
            }
            renditionManager.display(vh.icon, tmp, getFileDrawableId(item));
        }
        else if (type.startsWith(PREFIX_SUBSCRIPTION))
        {
            tmp = getData(item, CloudConstant.FOLLOWERUSERNAME_VALUE);
            if (tmp.isEmpty())
            {
                tmp = null;
            }
            renditionManager.display(vh.icon, tmp, getFileDrawableId(item));
        }
        else
        {
            renditionManager.display(vh.icon, item.getCreatedBy(), getFileDrawableId(item));
        }
    }

    private int getFileDrawableId(ActivityEntry item)
    {
        int drawable = R.drawable.ic_menu_notifications;
        String s = item.getType();

        if (s.startsWith(PREFIX_FILE))
        {
            drawable = MimeTypeManager.getIcon(getData(item, OnPremiseConstant.TITLE_VALUE));
        }
        else
        {
            for (Entry<String, Integer> icon : EVENT_ICON.entrySet())
            {
                if (s.startsWith(icon.getKey()))
                {
                    drawable = icon.getValue();
                    break;
                }
            }
        }
        return drawable;
    }

    public static final String PREFIX_LINK = "org.opendataspace.links.link";

    public static final String PREFIX_EVENT = "org.opendataspace.calendar.event";

    public static final String PREFIX_WIKI = "org.opendataspace.wiki.page";

    public static final String PREFIX_FILE = "org.opendataspace.documentlibrary.file";

    public static final String PREFIX_USER = "org.opendataspace.site.user";

    public static final String PREFIX_DATALIST = "org.opendataspace.datalists.list";

    public static final String PREFIX_DISCUSSIONS = "org.opendataspace.discussions";

    public static final String PREFIX_FOLDER = "org.opendataspace.documentlibrary.folder";

    public static final String PREFIX_COMMENT = "org.opendataspace.comments.comment";

    public static final String PREFIX_BLOG = "org.opendataspace.blog";

    public static final String PREFIX_SUBSCRIPTION = "org.opendataspace.subscriptions";

    public static final String PREFIX_GROUP = "org.opendataspace.site.group";

    // TODO Constant Manager ?
    @SuppressWarnings("serial")
    private static final Map<String, Integer> EVENT_ICON = new HashMap<String, Integer>()
    {
        {
            put(PREFIX_LINK, R.drawable.ic_menu_share);
            put(PREFIX_EVENT, R.drawable.ic_menu_today);
            put(PREFIX_WIKI, R.drawable.ic_menu_notifications);
            put(PREFIX_USER, R.drawable.ic_avatar);
            put(PREFIX_DATALIST, R.drawable.ic_menu_notifications);
            put(PREFIX_DISCUSSIONS, R.drawable.ic_menu_start_conversation);
            put(PREFIX_FOLDER, R.drawable.ic_menu_archive);
            put(PREFIX_COMMENT, R.drawable.ic_menu_start_conversation);
            put(PREFIX_BLOG, R.drawable.ic_menu_notifications);
            put(PREFIX_SUBSCRIPTION, R.drawable.ic_menu_notifications);
            put(PREFIX_GROUP, R.drawable.ic_menu_notifications);
        }
    };

    //
    private static final String PARAM_TITLE = "{0}";

    private static final String PARAM_USER_PROFILE = "{1}";

    private static final String PARAM_CUSTOM = "{2}";

    private static final String PARAM_SITE_LINK = "{4}";
    
    private static final String PARAM_SUBSCRIBER = "{5}";

    private static final String PARAM_STATUS = "{6}";

    private String getActivityTypeMessage(ActivityEntry item)
    {
        String s = item.getType();
        if (MAP_ACTIVITY_TYPE.get(s) != null)
        {
            s = getContext().getResources().getString(MAP_ACTIVITY_TYPE.get(item.getType()));

            if (s.contains(PARAM_CUSTOM))
            {
                s = s.replace(PARAM_CUSTOM, getData(item, OnPremiseConstant.ROLE_VALUE));
                s = s.replace(PARAM_USER_PROFILE, "<b>" + getData(item, OnPremiseConstant.MEMEBERFIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.MEMBERLASTNAME_VALUE) + "</b>");
            }
            else
            {
                s = s.replace(PARAM_USER_PROFILE, "<b>" + getData(item, OnPremiseConstant.FIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.LASTNAME_VALUE) + "</b>");
            }

            if (s.contains(PARAM_TITLE))
            {
                s = s.replace(PARAM_TITLE, "<b>" + getData(item, OnPremiseConstant.TITLE_VALUE) + "</b>");
            }

            if (s.contains(PARAM_SITE_LINK))
            {
                s = s.replace(PARAM_SITE_LINK, item.getSiteShortName());
            }

            if (s.contains(PARAM_STATUS))
            {
                s = s.replace(PARAM_STATUS, getData(item, OnPremiseConstant.STATUS_VALUE));
            }

            if (s.contains(PARAM_SUBSCRIBER))
            {
                s = s.replace(PARAM_SUBSCRIBER, "<b>" + getData(item, OnPremiseConstant.USERFIRSTNAME_VALUE) + " "
                        + getData(item, OnPremiseConstant.USERLASTNAME_VALUE) + "</b>");
            }
        }
        return s;
    }

    private String getData(ActivityEntry entry, String key)
    {
        String value = "";
        if (entry == null) { return value; }

        value = entry.getData(key);
        if (value == null)
        {
            value = "";
        }
        return value;
    }

    // TODO Constant Manager ?
    @SuppressWarnings("serial")
    private static final Map<String, Integer> MAP_ACTIVITY_TYPE = new HashMap<String, Integer>()
    {
        {
            put("org.opendataspace.blog.post-created", R.string.org_alfresco_blog_post_created);
            put("org.opendataspace.blog.post-updated", R.string.org_alfresco_blog_post_updated);
            put("org.opendataspace.blog.post-deleted", R.string.org_alfresco_blog_post_deleted);
            put("org.opendataspace.comments.comment-created", R.string.org_alfresco_comments_comment_created);
            put("org.opendataspace.comments.comment-updated", R.string.org_alfresco_comments_comment_updated);
            put("org.opendataspace.comments.comment-deleted", R.string.org_alfresco_comments_comment_deleted);
            put("org.opendataspace.discussions.post-created", R.string.org_alfresco_discussions_post_created);
            put("org.opendataspace.discussions.post-updated", R.string.org_alfresco_discussions_post_updated);
            put("org.opendataspace.discussions.post-deleted", R.string.org_alfresco_discussions_post_deleted);
            put("org.opendataspace.discussions.reply-created", R.string.org_alfresco_discussions_reply_created);
            put("org.opendataspace.discussions.reply-updated", R.string.org_alfresco_discussions_reply_updated);
            put("org.opendataspace.calendar.event-created", R.string.org_alfresco_calendar_event_created);
            put("org.opendataspace.calendar.event-updated", R.string.org_alfresco_calendar_event_updated);
            put("org.opendataspace.calendar.event-deleted", R.string.org_alfresco_calendar_event_deleted);
            put("org.opendataspace.documentlibrary.file-added", R.string.org_alfresco_documentlibrary_file_added);
            put("org.opendataspace.documentlibrary.files-added", R.string.org_alfresco_documentlibrary_files_added);
            put("org.opendataspace.documentlibrary.file-created", R.string.org_alfresco_documentlibrary_file_created);
            put("org.opendataspace.documentlibrary.file-deleted", R.string.org_alfresco_documentlibrary_file_deleted);
            put("org.opendataspace.documentlibrary.files-deleted", R.string.org_alfresco_documentlibrary_files_deleted);
            put("org.opendataspace.documentlibrary.file-updated", R.string.org_alfresco_documentlibrary_file_updated);
            put("org.opendataspace.documentlibrary.files-updated", R.string.org_alfresco_documentlibrary_files_updated);
            put("org.opendataspace.documentlibrary.folder-added", R.string.org_alfresco_documentlibrary_folder_added);
            put("org.opendataspace.documentlibrary.folder-deleted", R.string.org_alfresco_documentlibrary_folders_deleted);
            put("org.opendataspace.documentlibrary.folders-added", R.string.org_alfresco_documentlibrary_folder_added);
            put("org.opendataspace.documentlibrary.folders-deleted", R.string.org_alfresco_documentlibrary_folders_deleted);
            put("org.opendataspace.documentlibrary.google-docs-checkout",
                    R.string.org_alfresco_documentlibrary_google_docs_checkout);
            put("org.opendataspace.documentlibrary.google-docs-checkin",
                    R.string.org_alfresco_documentlibrary_google_docs_checkin);
            put("org.opendataspace.documentlibrary.inline-edit", R.string.org_alfresco_documentlibrary_inline_edit);
            put("org.opendataspace.documentlibrary.file-liked", R.string.org_alfresco_documentlibrary_file_liked);
            put("org.opendataspace.documentlibrary.folder-liked", R.string.org_alfresco_documentlibrary_folder_liked);
            put("org.opendataspace.wiki.page-created", R.string.org_alfresco_wiki_page_created);
            put("org.opendataspace.wiki.page-edited", R.string.org_alfresco_wiki_page_edited);
            put("org.opendataspace.wiki.page-renamed", R.string.org_alfresco_wiki_page_renamed);
            put("org.opendataspace.wiki.page-deleted", R.string.org_alfresco_wiki_page_deleted);
            put("org.opendataspace.site.group-added", R.string.org_alfresco_site_group_added);
            put("org.opendataspace.site.group-removed", R.string.org_alfresco_site_group_removed);
            put("org.opendataspace.site.group-role_changed", R.string.org_alfresco_site_group_role_changed);
            put("org.opendataspace.site.user-joined", R.string.org_alfresco_site_user_joined);
            put("org.opendataspace.site.user-left", R.string.org_alfresco_site_user_left);
            put("org.opendataspace.site.user-role-changed", R.string.org_alfresco_site_user_role_changed);
            put("org.opendataspace.links.link-created", R.string.org_alfresco_links_link_created);
            put("org.opendataspace.links.link-updated", R.string.org_alfresco_links_link_updated);
            put("org.opendataspace.links.link-deleted", R.string.org_alfresco_links_link_deleted);
            put("org.opendataspace.datalists.list-created", R.string.org_alfresco_datalists_list_created);
            put("org.opendataspace.datalists.list-updated", R.string.org_alfresco_datalists_list_updated);
            put("org.opendataspace.datalists.list-deleted", R.string.org_alfresco_datalists_list_deleted);
            put("org.opendataspace.subscriptions.followed", R.string.org_alfresco_subscriptions_followed);
            put("org.opendataspace.subscriptions.subscribed", R.string.org_alfresco_subscriptions_subscribed);
            put("org.opendataspace.profile.status-changed", R.string.org_alfresco_profile_status_changed);
        }
    };
}
