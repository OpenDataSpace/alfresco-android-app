package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.operations.Operation;
import org.alfresco.mobile.android.application.operations.batch.BatchOperationManager;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationCallback;
import org.opendataspace.android.app.R;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class OdsMoveNodesCallback extends AbstractBatchOperationCallback<Boolean>
{
    public OdsMoveNodesCallback(Context context, int totalItems, int pendingItems, boolean isMove)
    {
        super(context, totalItems, pendingItems);
        inProgress = context.getString(isMove ? R.string.move_operation : R.string.copy_operation);
    }

    @Override
    public void onProgressUpdate(Operation<Boolean> task, Long values)
    {
        groupRecord = BatchOperationManager.getInstance(context).getOperationGroup(task.getOperationId());

        if (groupRecord.totalRequests == 1)
        {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IntentIntegrator.ACTION_OPERATION_PROGRESS_UPDATE);
            broadcastIntent.putExtra(IntentIntegrator.EXTRA_DATA, values);

            LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
        }
    }
}
