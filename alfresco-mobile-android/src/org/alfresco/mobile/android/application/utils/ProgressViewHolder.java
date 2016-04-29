package org.alfresco.mobile.android.application.utils;

import org.opendataspace.android.app.R;
import org.alfresco.mobile.android.ui.utils.GenericViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public final class ProgressViewHolder extends GenericViewHolder
{
    public final ProgressBar progress;

    public final ImageView iconTopRight;

    public final ImageView iconBottomRight;

    public final ImageView favoriteIcon;

    public LinearLayout bg;

    public ProgressViewHolder(View v)
    {
        super(v);
        this.progress = (ProgressBar) v.findViewById(R.id.status_progress);
        this.iconTopRight = (ImageView) v.findViewById(R.id.icon_top_right);
        this.iconBottomRight = (ImageView) v.findViewById(R.id.icon_bottom_right);
        this.favoriteIcon = (ImageView) v.findViewById(R.id.favorite_icon);
    }
}
