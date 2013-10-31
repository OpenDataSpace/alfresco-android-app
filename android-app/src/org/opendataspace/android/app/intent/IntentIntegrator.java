/*******************************************************************************
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.opendataspace.android.app.intent;

public interface IntentIntegrator extends PublicIntent
{
    String ALFRESCO_SCHEME_SHORT = "alfresco";

    // ///////////////////////////////////////////////////////////////////////////
    // HELP GUIDE
    // ///////////////////////////////////////////////////////////////////////////
    String HELP_GUIDE = "view_help_guide";

    // ///////////////////////////////////////////////////////////////////////////
    // SIGNUP PROCESS
    // ///////////////////////////////////////////////////////////////////////////
    String CLOUD_SIGNUP = "sign_up_cloud";

    String CLOUD_SIGNUP_I = "sign_up_cloud_i";

    String ACTION_CHECK_SIGNUP = "org.opendataspace.android.app.intent.ACTION_CHECK_SIGNUP";

    // ///////////////////////////////////////////////////////////////////////////
    // OAUTH MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////
    String ACTION_USER_AUTHENTICATION = "org.opendataspace.android.app.intent.ACTION_USER_AUTHENTICATION";

    String CATEGORY_OAUTH = "org.opendataspace.android.app.intent.CATEGORY_OAUTH";

    String CATEGORY_OAUTH_REFRESH = "org.opendataspace.android.app.intent.CATEGORY_OAUTH_REFRESH";

    // ///////////////////////////////////////////////////////////////////////////
    // OPERATIONS MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // ACTION
    String ACTION_DISPLAY_OPERATIONS = "org.opendataspace.android.app.intent.ACTION_DISPLAY_OPERATIONS";

    String ACTION_OPERATION_STOP = "org.opendataspace.android.app.intent.ACTION_OPERATION_STOP";

    String ACTION_OPERATIONS_STOP = "org.opendataspace.android.app.intent.ACTION_OPERATIONS_STOP";

    String ACTION_OPERATIONS_CANCEL = "org.opendataspace.android.app.intent.ACTION_OPERATIONS_CANCEL";

    String ACTION_OPERATION_PAUSE = "org.opendataspace.android.app.intent.ACTION_OPERATION_PAUSE";

    // BROADCAST
    String ACTION_OPERATION_COMPLETED = "org.opendataspace.android.app.intent.ACTION_OPERATION_COMPLETE";

    String ACTION_OPERATIONS_COMPLETED = "org.opendataspace.android.app.intent.ACTION_OPERATIONS_COMPLETE";
    
    // BROADCAST
    String EXTRA_OPERATIONS_TYPE = "org.opendataspace.android.app.intent.EXTRA_OPERATIONS_TYPE";

    // ///////////////////////////////////////////////////////////////////////////
    // ACCOUNT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // ACTION
    /** Load or reuse a session for the specified account. */
    String ACTION_LOAD_ACCOUNT = "org.opendataspace.android.app.intent.ACTION_LOAD_ACCOUNT";

    /** Create a new session for the specified account. */
    String ACTION_RELOAD_ACCOUNT = "org.opendataspace.android.app.intent.ACTION_RELOAD_ACCOUNT";

    String ACTION_CREATE_ACCOUNT = "org.opendataspace.android.app.intent.ACTION_CREATE_ACCOUNT";

    // BROADCAST
    /** The specified account is inactive. */
    String ACTION_ACCOUNT_INACTIVE = "org.opendataspace.android.app.intent.ACTION_ACCOUNT_INACTIVE";

    String ACTION_LOAD_ACCOUNT_ERROR = "org.opendataspace.android.app.intent.ACTION_LOAD_ACCOUNT_ERROR";

    String ACTION_LOAD_ACCOUNT_STARTED = "org.opendataspace.android.app.intent.ACTION_LOAD_ACCOUNT_STARTED";

    String ACTION_LOAD_ACCOUNT_COMPLETED = "org.opendataspace.android.app.intent.ACTION_LOAD_ACCOUNT_COMPLETED";

    String ACTION_CREATE_ACCOUNT_STARTED = "org.opendataspace.android.app.intent.ACTION_CREATE_ACCOUNT_STARTED";

    String ACTION_CREATE_ACCOUNT_COMPLETED = "org.opendataspace.android.app.intent.ACTION_CREATE_ACCOUNT_COMPLETED";

    String ACTION_DELETE_ACCOUNT_STARTED = "org.opendataspace.android.app.intent.ACTION_DELETE_ACCOUNT_STARTED";

    String ACTION_DELETE_ACCOUNT_COMPLETED = "org.opendataspace.android.app.intent.ACTION_DELETE_ACCOUNT_COMPLETED";

    // EXTRA
    String EXTRA_ACCOUNT_ID = "org.opendataspace.android.app.intent.EXTRA_ACCOUNT_ID";

    String EXTRA_OAUTH_DATA = "org.opendataspace.android.app.intent.EXTRA_OAUTH_DATA";

    String EXTRA_CREATE_REQUEST = "org.opendataspace.android.app.intent.EXTRA_CREATE_REQUEST";

    String EXTRA_NETWORK_ID = "org.opendataspace.android.app.intent.EXTRA_NETWORK_ID";

    // ///////////////////////////////////////////////////////////////////////////
    // DISPLAY DIALOG
    // ///////////////////////////////////////////////////////////////////////////
    // ACTION
    /** Display dialog with extra bundle */
    String ACTION_DISPLAY_DIALOG = "org.opendataspace.android.app.intent.ACTION_DISPLAY_DIALOG";

    String ACTION_DISPLAY_ERROR = "org.opendataspace.android.app.intent.DISPLAY_ERROR";

    String EXTRA_ERROR_DATA = "org.opendataspace.android.app.intent.EXTRA_ERROR_DATA";

    // ///////////////////////////////////////////////////////////////////////////
    // CONTENT MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // BROADCAST
    String ACTION_UPDATE_STARTED = "org.opendataspace.android.app.intent.ACTION_UPDATE_STARTED";

    String ACTION_UPDATE_COMPLETED = "org.opendataspace.android.app.intent.ACTION_UPDATE_COMPLETED";

    String ACTION_UPLOAD_STARTED = "org.opendataspace.android.app.intent.ACTION_UPLOAD_STARTED";

    String ACTION_UPLOAD_COMPLETED = "org.opendataspace.android.app.intent.ACTION_UPLOAD_COMPLETED";

    String ACTION_DOWNLOAD_COMPLETED = "org.opendataspace.android.app.intent.ACTION_DOWNLOAD_COMPLETED";

    String ACTION_DELETE_COMPLETED = "org.opendataspace.android.app.intent.ACTION_DELETE_COMPLETED";

    String ACTION_CREATE_FOLDER_STARTED = "org.opendataspace.android.app.intent.ACTION_CREATE_FOLDER_STARTED";

    String ACTION_CREATE_FOLDER_COMPLETED = "org.opendataspace.android.app.intent.ACTION_CREATE_FOLDER_COMPLETED";

    String ACTION_RETRIEVE_NAME_COMPLETED = "org.opendataspace.android.app.intent.ACTION_RETRIEVE_NAME_COMPLETED";

    // EXTRA
    String EXTRA_UPDATED_NODE = "org.opendataspace.android.app.intent.EXTRA_UPDATED_NODE";

    String EXTRA_CREATED_FOLDER = "org.opendataspace.android.app.intent.EXTRA_CREATED_FOLDER";

    String EXTRA_DOCUMENT_NAME = "org.opendataspace.android.app.intent.EXTRA_DOCUMENT_NAME";

    // ///////////////////////////////////////////////////////////////////////////
    // COLLABORATION MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // BROADCAST
    String ACTION_FAVORITE_COMPLETED = "org.opendataspace.android.app.intent.ACTION_FAVORITE_COMPLETED";

    String ACTION_LIKE_COMPLETED = "org.opendataspace.android.app.intent.ACTION_LIKE_COMPLETED";

    // EXTRA
    String EXTRA_FAVORITE = "org.opendataspace.android.app.intent.EXTRA_FAVORITE";

    String EXTRA_LIKE = "org.opendataspace.android.app.intent.EXTRA_LIKE";

    // ///////////////////////////////////////////////////////////////////////////
    // FILES MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // ACTION
    String ACTION_PICK_FILE = "org.opendataspace.android.app.intent.ACTION_PICK_FILE";

    // EXTRA
    String EXTRA_UPDATED_FILE = "org.opendataspace.android.app.intent.EXTRA_UPDATED_FILE";

    String EXTRA_LIBRARY = "org.opendataspace.android.app.intent.EXTRA_LIBRARY";

    // ///////////////////////////////////////////////////////////////////////////
    // SYNC MANAGEMENT
    // ///////////////////////////////////////////////////////////////////////////

    // ACTION
    String ACTION_SYNCHRO_STOP = "org.opendataspace.android.app.intent.ACTION_SYNCHRO_STOP";

    String ACTION_SYNCHROS_STOP = "org.opendataspace.android.app.intent.ACTION_SYNCHROS_STOP";

    String ACTION_SYNCHROS_CANCEL = "org.opendataspace.android.app.intent.ACTION_SYNCHROS_CANCEL";

    String ACTION_SYNCHRO_DISPLAY = "org.opendataspace.android.app.intent.ACTION_SYNCHRO_DISPLAY";

    String ACTION_SYNCHRO_PAUSE = "org.opendataspace.android.app.intent.ACTION_SYNCHRO_PAUSE";

    // BROADCAST
    String ACTION_SYNCHRO_COMPLETED = "org.opendataspace.android.app.intent.ACTION_SYNCHRO_COMPLETED";

    String ACTION_SYNCHROS_COMPLETED = "org.opendataspace.android.app.intent.ACTION_SYNCHROS_COMPLETED";

    String ACTION_SYNC_SCAN_COMPLETED = "org.opendataspace.android.app.intent.ACTION_SYNC_SCAN_COMPLETED";

    // ///////////////////////////////////////////////////////////////////////////
    // SETTINGS
    // ///////////////////////////////////////////////////////////////////////////
    String ACTION_DISPLAY_SETTINGS = "org.opendataspace.android.app.intent.ACTION_DISPLAY_SETTINGS";

    // ///////////////////////////////////////////////////////////////////////////
    // ENCRYPTION
    // ///////////////////////////////////////////////////////////////////////////
    // BROADCAST
    String ACTION_ENCRYPT_COMPLETED = "org.opendataspace.android.app.intent.ACTION_ENCRYPT_COMPLETED";

    String ACTION_DECRYPT_COMPLETED = "org.opendataspace.android.app.intent.ACTION_DECRYPT_COMPLETED";
    
    String ACTION_ENCRYPT_ALL_COMPLETED = "org.opendataspace.android.app.intent.ACTION_ENCRYPT_ALL_COMPLETED";

    String ACTION_DECRYPT_ALL_COMPLETED = "org.opendataspace.android.app.intent.ACTION_DECRYPT_ALL_COMPLETED";

    // EXTRA
    String EXTRA_INTENT_ACTION = "org.opendataspace.android.app.intent.EXTRA_INTENT_ACTION";
    
    // ///////////////////////////////////////////////////////////////////////////
    // DATA CLEANER
    // ///////////////////////////////////////////////////////////////////////////
    // ACTION
    String ACTION_CLEAN_SHARE_FILE = "org.opendataspace.android.app.intent.ACTION_CLEAN_SHARE_FILE";

}
