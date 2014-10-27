package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;

public class OdsConfigRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;

    public static final int TYPE_ID = 6600;

    public OdsConfigRequest()
    {
        super();

        requestTypeId = TYPE_ID;
    }

    @Override
    public String getRequestIdentifier()
    {
        return "ods-config-cache";
    }
}
