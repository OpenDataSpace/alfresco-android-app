package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.opendataspace.android.app.links.OdsLink;

public class OdsUpdateLinkRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;
    public static final int TYPE_ID = 6601;

    private OdsLink link;

    public OdsUpdateLinkRequest(OdsLink link)
    {
        requestTypeId = TYPE_ID;
        this.link = link;
    }

    @Override
    public String getRequestIdentifier()
    {
        return "ods-update-link";
    }

    public OdsLink getLink()
    {
        return link;
    }
}
