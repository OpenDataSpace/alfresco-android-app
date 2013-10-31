/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of Alfresco Mobile for Android.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.opendataspace.android.app.accounts.networks;

import java.util.ArrayList;
import java.util.List;






import org.opendataspace.android.asynchronous.AbstractBaseLoader;
import org.opendataspace.android.asynchronous.LoaderResult;
import org.opendataspace.android.cmisapi.session.CloudNetwork;
import org.opendataspace.android.cmisapi.session.CloudSession;
import org.opendataspace.android.cmisapi.session.impl.CloudNetworkImpl;
import org.opendataspace.android.cmisapi.utils.messages.Messagesl18n;

import android.content.Context;

/**
 * Displays a fragment list of Cloud Networks.
 * 
 * @author Jean Marie Pascal
 */
public class CloudNetworksLoader extends AbstractBaseLoader<LoaderResult<List<CloudNetwork>>>
{
    /** Unique CloudNetworksLoader identifier. */
    public static final int ID = CloudNetworksLoader.class.hashCode();

    private CloudSession cloudSession;

    public CloudNetworksLoader(Context context, CloudSession session)
    {
        super(context);
        this.cloudSession = session;
    }

    @SuppressWarnings("deprecation")
	@Override
    public LoaderResult<List<CloudNetwork>> loadInBackground()
    {
        if (cloudSession == null) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "CloudSession")); }

        LoaderResult<List<CloudNetwork>> result = new LoaderResult<List<CloudNetwork>>();
        List<CloudNetwork> networks = new ArrayList<CloudNetwork>();

        try
        {
            List<CloudNetwork> tmpNetworks = cloudSession.getNetworks();
            networks = new ArrayList<CloudNetwork>(tmpNetworks.size());
            for (CloudNetwork cloudNetwork : tmpNetworks)
            {
                if (((CloudNetworkImpl) cloudNetwork).isEnabled())
                {
                    networks.add(cloudNetwork);
                }
            }
        }
        catch (Exception e)
        {
            result.setException(e);
        }

        result.setData(networks);

        return result;
    }
}
