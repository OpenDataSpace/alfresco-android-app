package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;

import android.os.Parcel;

public class OdsDocument extends DocumentImpl
{
    private static final long serialVersionUID = 1L;

    OdsDocument(CmisObject o, boolean hasAllProperties)
    {
        super(o, hasAllProperties);
    }

    private OdsDocument(CmisObject o)
    {
        super(o);
    }

    private OdsDocument(Parcel o)
    {
        super(o);
    }

    public CmisObject getCmisObject()
    {
        return object;
    }
}
