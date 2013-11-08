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
package org.opendataspace.android.asynchronous;

import org.opendataspace.android.cmisapi.model.Node;
import org.opendataspace.android.cmisapi.session.AlfrescoSession;

import android.content.Context;

public abstract class AbstractBooleanLoader extends AbstractBaseLoader<LoaderResult<Boolean>>
{
    /** Node object (Folder or Document). */
    protected Node node;

    /**
     * Determine if the user has been liked this node.
     * 
     * @param context : Android Context
     * @param session : Repository Session
     * @param node : Node object (Folder or Document)
     */
    public AbstractBooleanLoader(Context context, AlfrescoSession session, Node node)
    {
        super(context);
        this.session = session;
        this.node = node;
    }

    @Override
    public LoaderResult<Boolean> loadInBackground()
    {
        LoaderResult<Boolean> result = new LoaderResult<Boolean>();
        Boolean booleanValue = null;

        try
        {
            booleanValue = retrieveBoolean();
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(booleanValue);

        return result;
    }
    
    protected abstract boolean retrieveBoolean();
}