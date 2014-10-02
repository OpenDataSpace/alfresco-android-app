package org.opendataspace.android.app.links;

import java.io.Serializable;
import java.util.Calendar;

import android.text.TextUtils;

public class OdsLink implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String name = "";
    private String url = "";
    private String message = "";
    private String email = "";
    private String password = "";
    private String objectId = "";
    private Calendar expires;

    public OdsLink()
    {
        expires = Calendar.getInstance();
        expires.add(Calendar.DAY_OF_MONTH, 7);
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
        return expires;
    }

    public void setExpires(Calendar expires)
    {
        this.expires = expires;
    }

    public boolean isValid()
    {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(message) && !TextUtils.isEmpty(email) && expires != null;
    }
}
