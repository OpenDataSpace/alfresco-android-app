package org.opendataspace.android.app.links;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.text.TextUtils;

@DatabaseTable(tableName = "links")
public class OdsLink implements Serializable
{
    private static final long serialVersionUID = 1L;

    public final static String NODE_ID_FIELD = "nodeid";

    @DatabaseField(generatedId = true)
    private long id = -1;
    @DatabaseField
    private String name = "";
    @DatabaseField
    private String url = "";
    @DatabaseField
    private String message = "";
    @DatabaseField
    private String email = "";
    @DatabaseField
    private String password = "";
    @DatabaseField
    private String objectId = "";
    @DatabaseField
    private Date expires;
    @DatabaseField(columnName = NODE_ID_FIELD)
    private String nodeId;

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
        this.expires = expires.getTime();
    }

    public boolean isValid()
    {
        return !TextUtils.isEmpty(name) && !TextUtils.isEmpty(message) && !TextUtils.isEmpty(email) && expires != null;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String val)
    {
        nodeId = val;
    }

    public long getId()
    {
        return id;
    }
}
