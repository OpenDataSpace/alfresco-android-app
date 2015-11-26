package org.opendataspace.android.app.links;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class OdsLink implements Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Type
    {
        DOWNLOAD, UPLOAD
    }

    private String name = "";
    private String url = "";
    private String message = "";
    private String email = "";
    private String password = "";
    private String objectId = "";
    private Date expires;
    private String nodeId;
    private Type type = Type.DOWNLOAD;
    private String relationId = "";

    public OdsLink()
    {
        Calendar exp = Calendar.getInstance();
        exp.add(Calendar.DAY_OF_MONTH, 7);
        expires = exp.getTime();
    }

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

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Calendar getExpires()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(expires);
        return c;
    }

    public void setExpires(Calendar expires)
    {
        if (expires != null)
        {
            this.expires = expires.getTime();
        }
    }

    public boolean isValid()
    {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(message) && expires != null;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String val)
    {
        nodeId = val;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public String getRelationId()
    {
        return relationId;
    }

    public void setRelationId(String relationId)
    {
        this.relationId = relationId;
    }
}
