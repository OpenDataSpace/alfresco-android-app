package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.config.OdsConfigContext;

import android.content.Context;

public class OdsConfigCallback extends AbstractBatchOperationCallback<OdsConfigContext>
{
    public OdsConfigCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }
}
