package org.opendataspace.android.app.links;

import java.io.Serializable;

public class OdsLink implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name = "";
    private String url = "";
    private String objectId = "";

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }
}
