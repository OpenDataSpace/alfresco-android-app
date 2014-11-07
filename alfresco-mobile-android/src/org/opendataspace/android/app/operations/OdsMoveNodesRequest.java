package org.opendataspace.android.app.operations;

import java.util.List;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

public class OdsMoveNodesRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 6602;

    private final List<String> ids;
    private final String targetId;
    private final String sourceId;
    private final boolean isMove;

    public OdsMoveNodesRequest(List<String> ids, String targetId, String sourceId, boolean isMove)
    {
        super();

        this.ids = ids;
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.isMove = isMove;
        requestTypeId = TYPE_ID;
    }

    @Override
    public String getRequestIdentifier()
    {
        return "ods-move-nodes";
    }

    public List<String> getIds()
    {
        return ids;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public boolean isMove()
    {
        return isMove;
    }

    public String getSourceId()
    {
        return sourceId;
    }
}
