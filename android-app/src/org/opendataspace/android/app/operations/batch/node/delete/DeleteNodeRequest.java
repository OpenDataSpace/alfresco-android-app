/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *  
 *  This file is part of Alfresco Mobile for Android.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.app.operations.batch.node.delete;


import org.opendataspace.android.app.operations.batch.node.NodeOperationRequest;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.Node;

import android.database.Cursor;

public class DeleteNodeRequest extends NodeOperationRequest
{
    private static final long serialVersionUID = 1L;
    
    public static final int TYPE_ID = 40;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////////////////////////////////////////////////
    public DeleteNodeRequest(Folder parent, Node node)
    {
        super(parent.getIdentifier(), node.getIdentifier());
        requestTypeId = TYPE_ID;
        
        setNotificationTitle(node.getName());
        setMimeType(node.getName());
    }

    public DeleteNodeRequest(Cursor cursor)
    {
        super(cursor);
        requestTypeId = TYPE_ID;
    }
}
