package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.services.impl.onpremise.OnPremiseServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;

@SuppressWarnings("unused")
public class OdsServiceRegistry extends OnPremiseServiceRegistry
{
    public OdsServiceRegistry(AlfrescoSession session)
    {
        super(session);
        this.documentFolderService = new OdsDocumentFolderService(session);
    }
}
