/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.cmisapi.model.impl;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.opendataspace.android.cmisapi.model.Folder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Folder Base object
 * 
 * @author Jean Marie Pascal
 */
public class FolderImpl extends NodeImpl implements Folder
{

    private static final long serialVersionUID = 1L;

    public FolderImpl()
    {
    }

    public FolderImpl(CmisObject o)
    {
        super(o);
    }
    
    public FolderImpl(CmisObject o, boolean hasAllProperties)
    {
        super(o, hasAllProperties);
    }

    // ////////////////////////////////////////////////////
    // INTERNAL
    // ////////////////////////////////////////////////////

    /**
     * Internal method to serialize Folder object.
     */
    public static final Parcelable.Creator<FolderImpl> CREATOR = new Parcelable.Creator<FolderImpl>()
    {
        public FolderImpl createFromParcel(Parcel in)
        {
            return new FolderImpl(in);
        }

        public FolderImpl[] newArray(int size)
        {
            return new FolderImpl[size];
        }
    };

    public FolderImpl(Parcel o)
    {
        super(o);
    }
}
