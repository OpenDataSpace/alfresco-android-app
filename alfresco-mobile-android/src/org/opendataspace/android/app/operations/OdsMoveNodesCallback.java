package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;

import android.content.Context;

public class OdsMoveNodesCallback extends AbstractBatchOperationCallback<Void>
{
    public OdsMoveNodesCallback(Context context, int totalItems, int pendingItems)
    {
        super(context, totalItems, pendingItems);
    }
}
