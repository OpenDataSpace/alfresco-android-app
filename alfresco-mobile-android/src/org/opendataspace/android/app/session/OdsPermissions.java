package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.impl.NodeImpl;
import org.alfresco.mobile.android.api.model.impl.PermissionsImpl;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class OdsPermissions extends PermissionsImpl
{
    private static final long serialVersionUID = 1L;

    private final NodeImpl nd;

    OdsPermissions(Node node)
    {
        super(node);
        nd = (node instanceof NodeImpl) ? (NodeImpl) node : null;
    }

    public boolean canCreateFolder()
    {
        return nd != null ? nd.hasAllowableAction(Action.CAN_CREATE_FOLDER) : false;
    }

    public boolean canCreateFile()
    {
        return nd != null ? nd.hasAllowableAction(Action.CAN_CREATE_DOCUMENT) : false;
    }
}
