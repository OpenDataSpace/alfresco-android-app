package org.opendataspace.android.app.fileinfo;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "files")
public class OdsFileInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    public final static String NODE_ID_FIELD = "nid";
    public final static String FOLDER_ID_FIELD = "fid";
    public final static String TYPE_FIELD = "type";

    public final static int TYPE_DOWNLOAD = 0x1;

    @DatabaseField(id = true, columnName = NODE_ID_FIELD)
    private String nodeId;
    @DatabaseField(unique = true, columnName = FOLDER_ID_FIELD, canBeNull = false)
    private String folderId;
    @DatabaseField(canBeNull = false)
    private String path;
    @DatabaseField(index = true, columnName = TYPE_FIELD, canBeNull = false)
    private int type;

    public OdsFileInfo()
    {
        // requited by orm
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }
}
