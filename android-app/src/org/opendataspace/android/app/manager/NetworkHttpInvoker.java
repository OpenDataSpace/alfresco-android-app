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
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Output;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.opendataspace.android.app.R;
import org.opendataspace.android.app.activity.BaseActivity;
import org.opendataspace.android.app.preferences.GeneralPreferences;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.squareup.okhttp.OkHttpClient;

public class NetworkHttpInvoker extends org.alfresco.mobile.android.api.network.NetworkHttpInvoker
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
            showDialogCertif(certs);
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
        if(GeneralPreferences.getCertificatePref(BaseActivity.getCurrentContext()) == 0)
        {
            return null;
        }

        final HttpURLConnection con = httpClient.open(url);
        final String cookie =  getCookie(url.toString());
        if(cookie != null){
            con.addRequestProperty("Cookie", cookie);
        }

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

    @Override
    protected Response invoke(UrlBuilder url, String method, String contentType, Map<String, String> headers,
            Output writer, BindingSession session, BigInteger offset, BigInteger length)
    {
        final Response res = super.invoke(url, method, contentType, headers, writer, session, offset, length);

        final Map<String, List<String>> map = res.getHeaders();
        if(map == null)
        {
            return res;
        }

        if(!map.containsKey("set-cookie"))
        {
            return res;
        }

        List<String> list = map.get("set-cookie");
        if(list == null || list.size() == 0)
        {
            return res;
        }

        setCookie(url.toString(),list);

        return res;
    }

    public static void onSetSertf(){
        mCheck = true;
    }

    private static void showDialogCertif(final java.security.cert.X509Certificate[] certs){

        if(GeneralPreferences.getCertificatePref(BaseActivity.getCurrentContext()) == 1)
        {
            return;
        }

        String info = "";
        for (java.security.cert.X509Certificate sert:certs){
            info = info.concat(getCertificateInfo(sert));
        }

        ActionManager.actionDisplayCertifDialog(info);

        while(!mCheck){
            //waiting
        }
        mCheck = false;
    }

    private static String getCertificateInfo(java.security.cert.X509Certificate sert){

        Context c = BaseActivity.getCurrentContext();
        String title_certificate  = c == null ? "Certificate information" : c.getResources().getString(R.string.title_certificate);
        String serial_num_certificate  = c == null ? "Serial number:" : c.getResources().getString(R.string.serial_num_certificate);
        String issuer_certificate  = c == null ? "Issuer distinguished name:" : c.getResources().getString(R.string.issuer_certificate);

        String out = "<br><small>" + title_certificate + "</small>";

        String serial = String.valueOf(sert.getSerialNumber());

        Principal principal = sert.getIssuerDN();
        String issuerDn = principal.getName();
        if(issuerDn.contains("CN=") && issuerDn.length() > 2)
        {
            issuerDn = issuerDn.substring(3);
        }

        if(serial != null){
            out = out.concat("<br><small>" +serial_num_certificate+serial+"</small>");
        }
        if(issuerDn != null){
            out = out.concat(("<br><small>"+issuer_certificate+issuerDn+"</small>"));
        }

        return out;
    }

    private void setCookie(final String url, List<String> values){

        if(url == null || values == null)
        {
            return;
        }

        final CookieManager manager = CookieManager.getInstance();
        if(manager == null)
        {
            return;
        }

        String result = "";

        for(String val: values){
            if(val != null){
                if(result.length() > 0)
                {
                    result = result.concat(";");
                }
                result = result.concat(val);
            }
        }

        manager.setCookie(url, result);
        CookieSyncManager.getInstance().sync();
    }

    private String getCookie(final String url){

        final CookieManager manager = CookieManager.getInstance();
        if(manager == null)
        {
            return null;
        }

        final String cookie = manager.getCookie(url);

        return cookie;
    }
}
