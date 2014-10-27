package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.model.impl.FolderImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;

import android.os.Parcel;

public class OdsFolder extends FolderImpl
{
    private static final long serialVersionUID = 1L;

    public OdsFolder()
    {
        super();
    }

    public OdsFolder(CmisObject o, boolean hasAllProperties)
    {
        super(o, hasAllProperties);
    }

    public OdsFolder(CmisObject o)
    {
        super(o);
    }

    public OdsFolder(Parcel o)
    {
        super(o);
    }

    public CmisObject getCmisObject()
    {
        return object;
    }
}
