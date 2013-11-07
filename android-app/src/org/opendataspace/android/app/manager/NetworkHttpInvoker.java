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
package org.opendataspace.android.app.manager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.preferences.GeneralPreferences;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

public class NetworkHttpInvoker extends org.opendataspace.android.cmisapi.network.NetworkHttpInvoker
{

    private static Boolean mCheck = false;
    private final OkHttpClient httpClient;

    public NetworkHttpInvoker()
    {
        httpClient = new OkHttpClient();
    }

    private final static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            showDialogSertif(certs);
        }
    } };

    private final static HostnameVerifier hostVerifier = new HostnameVerifier() {

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return true;
        }
    };


    @Override
    protected HttpURLConnection getHttpURLConnection(final URL url) throws IOException
    {
        //Log.e("", "Status "+GeneralPreferences.getSertificatePref(BaseActivity.getCurrentContext()));

        if(GeneralPreferences.getSertificatePref(BaseActivity.getCurrentContext()) == 0)
            return null;

        // TODO: kolam replace with real certificate and host name verifier
        final HttpURLConnection con = httpClient.open(url);
        Log.e("", "URL  "+url.toString());
        if (con instanceof HttpsURLConnection) {
            SSLContext sc;

            try {
                final HttpsURLConnection scon = (HttpsURLConnection) con;
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

    public static void onSetSertf(){
        mCheck = true;
    }

    private static void showDialogSertif(final java.security.cert.X509Certificate[] certs){

        if(GeneralPreferences.getSertificatePref(BaseActivity.getCurrentContext()) == 1)
            return;

        String info = "";
        for (final java.security.cert.X509Certificate sert:certs)
            info = info.concat(getSertificateInfo(sert));

        ActionManager.actionDisplaySertifDialog(info);

        while(!mCheck){
            //waiting
        }
        mCheck = false;
    }

    private static String getSertificateInfo(final java.security.cert.X509Certificate sert){

        String out = "<br><small>Certificate information</small>";
        final String serial = String.valueOf(sert.getSerialNumber());

        final Principal principal = sert.getIssuerDN();
        String issuerDn = principal.getName();
        if(issuerDn.contains("CN=") && issuerDn.length() > 2)
            issuerDn = issuerDn.substring(3);

        if(serial != null)
            out = out.concat("<br><small>Serial number: "+serial+"</small>");
        if(issuerDn != null)
            out = out.concat("<br><small>Issuer distinguished name: "+issuerDn+"</small>");

        return out;
    }
}
