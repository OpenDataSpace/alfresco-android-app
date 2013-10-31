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
package org.alfresco.mobile.android.application.manager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.squareup.okhttp.OkHttpClient;

public class NetworkHttpInvoker extends org.alfresco.mobile.android.api.network.NetworkHttpInvoker
{

    private OkHttpClient httpClient;
    
    public NetworkHttpInvoker()
    {
        httpClient = new OkHttpClient();
    }
    
    private final static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }

        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }
    } };
    
    private final static HostnameVerifier hostVerifier = new HostnameVerifier() {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
    };
    
    @Override
    protected HttpURLConnection getHttpURLConnection(URL url) throws IOException
    {
    	// TODO: kolam replace with real certificate and host name verifier
    	HttpURLConnection con = httpClient.open(url);
    	
    	if (con instanceof HttpsURLConnection) {
    		SSLContext sc;
    		
	        try {
	        	HttpsURLConnection scon = (HttpsURLConnection) con;
	            sc = SSLContext.getInstance("TLS");
	            sc.init(null, trustAllCerts, new java.security.SecureRandom());
	            scon.setSSLSocketFactory(sc.getSocketFactory());
	            scon.setHostnameVerifier(hostVerifier);
	        } catch (final Exception ex) {
	            ex.printStackTrace();
	        }
    	}
    	
        return con;
    }
    
}
