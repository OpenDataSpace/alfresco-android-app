package org.opendataspace.android.app.session;

import org.alfresco.mobile.android.api.model.impl.DocumentImpl;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.opendataspace.android.app.fileinfo.OdsFileInfo;

import android.os.Parcel;

public class OdsDocument extends DocumentImpl
{
    private static final long serialVersionUID = 1L;

    private boolean downloaded = false;
    private OdsFileInfo fileInfo;

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

    public boolean isDownloaded()
    {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded)
    {
        this.downloaded = downloaded;
    }

    public OdsFileInfo getFileInfo()
    {
        return fileInfo;
    }

    public void setFileInfo(OdsFileInfo fileInfo)
    {
        this.fileInfo = fileInfo;
    }
}
