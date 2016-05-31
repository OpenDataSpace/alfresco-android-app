package org.opendataspace.android.app.operations;

import android.content.Context;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.R;

public class OdsUpdateLinkCallback extends AbstractBatchOperationCallback<OdsUpdateLinkContext>
{
    public OdsUpdateLinkCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
        complete = getBaseContext().getString(R.string.link_deleted);
    }
}
