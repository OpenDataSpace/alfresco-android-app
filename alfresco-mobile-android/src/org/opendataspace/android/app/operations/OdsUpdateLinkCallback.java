package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.links.OdsUpdateLinkContext;

import android.content.Context;

public class OdsUpdateLinkCallback extends AbstractBatchOperationCallback<OdsUpdateLinkContext>
{
    public OdsUpdateLinkCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }
}
