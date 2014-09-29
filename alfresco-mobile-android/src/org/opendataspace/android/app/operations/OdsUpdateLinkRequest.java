package org.opendataspace.android.app.operations;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.application.operations.batch.impl.AbstractBatchOperationRequestImpl;
import org.opendataspace.android.app.links.OdsLink;

public class OdsUpdateLinkRequest extends AbstractBatchOperationRequestImpl
{
    private static final long serialVersionUID = 1L;
    public static final int TYPE_ID = 6601;

    private String nodeId;
    private OdsLink link;

    public OdsUpdateLinkRequest(Node node, OdsLink link)
    {
        requestTypeId = TYPE_ID;
        this.link = link;
        this.nodeId = node.getIdentifier();
    }

    @Override
    public String getRequestIdentifier()
    {
        return "ods-update-link";
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public OdsLink getLink()
    {
        return link;
    }
}
