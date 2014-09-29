package org.opendataspace.android.app.operations;

import java.io.Serializable;

public class OdsConfigContext implements Serializable
{
    private static final long serialVersionUID = 1L;

    private boolean updated = false;

    public boolean isUpdated()
    {
        return updated;
    }

    public void setUpdated(boolean updated)
    {
        this.updated = updated;
    }
}
