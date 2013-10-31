/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.opendataspace.android.cmisapi.session;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.opendataspace.android.cmisapi.model.Folder;
import org.opendataspace.android.cmisapi.model.ListingContext;
import org.opendataspace.android.cmisapi.model.RepositoryInfo;
import org.opendataspace.android.cmisapi.services.ServiceRegistry;

import android.os.Parcelable;

/**
 * RepositorySession represents a connection to an on-premise repository as a
 * specific user.
 * 
 * @author Jean Marie Pascal
 */
public interface AlfrescoSession extends Parcelable
{
    // ///////////////////////////////////////////////
    // EXTENSION
    // ///////////////////////////////////////////////
    /**
     * <b>OnPremise ONLY</b> : Define the specific implementation of all
     * services for the session. Must be a full qualified classname. This class
     * must extend
     * {@link org.opendataspace.android.cmisapi.services.ServiceRegistry} <br/>
     * <b>This parameter can't be changed after the session creation</b>.
     */
    String ONPREMISE_SERVICES_CLASSNAME = "org.opendataspace.android.cmisapi.services.onpremise";

    /**
     * <b>Cloud ONLY</b> Define the specific implementation of all services for
     * the session. Must be a full qualified classname. This class must extend
     * {@link org.opendataspace.android.cmisapi.services.ServiceRegistry} <b>This
     * parameter can't be changed after the session creation</b>.
     */
    String CLOUD_SERVICES_CLASSNAME = "org.opendataspace.android.cmisapi.services.cloud";

    /**
     * Define the specific implementation of authenticator for the session. Must
     * be a full qualified classname. This class must extend
     * {@link org.opendataspace.android.cmisapi.session.authentication.AuthenticationProvider}
     * <br/>
     * <b>This parameter can't be changed after the session creation</b>.
     */
    String AUTHENTICATOR_CLASSNAME = "org.opendataspace.android.cmisapi.authenticator.classname";
    
    /**
     * Define the specific implementation of HTTP layer for the session. Must
     * be a full qualified classname. This class must extend
     * {@link org.apache.chemistry.opencmis.client.bindings.spi.http.HttpInvoker}
     * <br/>
     * <b>This parameter can't be changed after the session creation</b>.
     */
    String HTTP_INVOKER_CLASSNAME = "org.opendataspace.android.cmisapi.httpinvoker.classname";

    /**
     * <b>OnPremise ONLY</b> : Allow metadata extraction during file import.
     * Value must be a boolean. Default : false
     */
    String EXTRACT_METADATA = "org.opendataspace.features.extractmetadata";

    /**
     * <b>OnPremise ONLY</b> : Allow thumbnail generation during file import.
     * Value must be a boolean. Default : false
     */
    String CREATE_THUMBNAIL = "org.opendataspace.features.generatethumbnails";

    // ///////////////////////////////////////////////
    // LISTING
    // ///////////////////////////////////////////////
    /**
     * Define the maximum number of items a list can contains by default in all
     * SDK services.<br/>
     * Value must be an Integer > 0. <br/>
     * Default : 50
     */
    String LISTING_MAX_ITEMS = "org.opendataspace.android.cmisapi.listing.maxitems";

    // ///////////////////////////////////////////////
    // CACHE
    // ///////////////////////////////////////////////
    /**
     * Define the path to the cache folder. The cache folder is used to store temporary file.<br/>
     * Value must be String value that represents a valid path inside the device.<br/>
     * Default : "/sdcard/Android/data/org.opendataspace.android.app/cache"
     */
    String CACHE_FOLDER = "org.opendataspace.cache.folder";

    // ///////////////////////////////////////////////
    // METHODS
    // ///////////////////////////////////////////////

    /**
     * Returns RepositoryInformation object representing the repository the
     * session is connected to.
     */
    RepositoryInfo getRepositoryInfo();

    /**
     * Returns the base URL associated to the repository e.g.
     * http://hostname:port/alfresco.
     */
    String getBaseUrl();

    /**
     * Returns the user identifier with which the session was created.
     */
    String getPersonIdentifier();

    /**
     * Returns the current default listing parameters for paging and sorting.
     */
    ListingContext getDefaultListingContext();

    /**
     * Return all services available with this repository.
     * 
     * @return Service Provider associated to the session.
     */
    ServiceRegistry getServiceRegistry();

    /**
     * Returns the root folder of the repository this session is connected to.
     */
    Folder getRootFolder();

    /**
     * Allow to add some extra parameters as settings to modify behaviour of the
     * session. Settings provide session configuration parameters e.g. cache
     * settings, default paging values, ordering etc.
     * 
     * @param key
     * @param value
     */
    void addParameter(String key, Serializable value);

    /**
     * Allow to add some extra parameters as settings to modify behaviour of the
     * session. Settings provide session configuration parameters e.g. cache
     * settings, default paging values, ordering etc.
     * 
     * @param parameters
     */
    void addParameters(Map<String, Serializable> parameters);

    /**
     * Returns the value of a parameter with the given key stored in the
     * session.
     * 
     * @param key
     * @return
     */
    Serializable getParameter(String key);

    /**
     * Removes a parameter stored in the session.
     * 
     * @param key
     */
    void removeParameter(String key);

    /**
     * Returns a list of all the parameter names stored in the sesssion.
     */
    List<String> getParameterKeys();
    
    /**
     * Clears any cached data the session is storing.
     */
    void clear();
}
