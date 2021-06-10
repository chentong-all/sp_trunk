package com.ayue.sp.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;



/**
 * 2020年11月10日
 *
 * @author ayue
 */
public class AuthUtil {
        private String charset = "utf-8";
          private Integer connectTimeout = null;
          private Integer socketTimeout = null;
          private String proxyHost = null;
          private Integer proxyPort = null;

        public  String doGet(String url) throws Exception {

                             URL localURL = new URL(url);

                             URLConnection connection = openConnection(localURL);
                             HttpURLConnection httpURLConnection = (HttpURLConnection)connection;

                             httpURLConnection.setRequestProperty("Accept-Charset", charset);
                             httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                             InputStream inputStream = null;
                             InputStreamReader inputStreamReader = null;
                             BufferedReader reader = null;
                             StringBuffer resultBuffer = new StringBuffer();
                             String tempLine = null;

                             if (httpURLConnection.getResponseCode() >= 300) {
                                         throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
                                     }

                             try {
                                         inputStream = httpURLConnection.getInputStream();
                                         inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
                                         reader = new BufferedReader(inputStreamReader);

                                         while ((tempLine = reader.readLine()) != null) {
                                                     resultBuffer.append(tempLine);
                                                 }

                                     } finally {

                                         if (reader != null) {
                                                     reader.close();
                                                 }

                                         if (inputStreamReader != null) {
                                                     inputStreamReader.close();
                                                 }

                                         if (inputStream != null) {
                                                     inputStream.close();
                                                 }

                                     }

                             return resultBuffer.toString();
                         }
        private URLConnection openConnection(URL localURL) throws IOException {
                             URLConnection connection;
                             if (proxyHost != null && proxyPort != null) {
                                         Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                                         connection = localURL.openConnection(proxy);
                                     } else {
                                         connection = localURL.openConnection();
                                     }
                             return connection;
                         }
        public static JSONObject doGetJson(String url) throws ClientProtocolException, IOException {
                JSONObject jsonObject = null;
                @SuppressWarnings("deprecation")
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                        String result = EntityUtils.toString(entity, "UTF-8");
                        jsonObject = JSONObject.fromObject(result);

                }
                httpGet.releaseConnection();
                return jsonObject;
        }
        /*public static JSONObject doGetJson(String url) throws ClientProtocolException, IOException {
                JSONObject jsonObject = null;
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                        String result = EntityUtils.toString(entity, "UTF-8");
                        jsonObject = JSONObject.fromObject(result);

                }
                httpGet.releaseConnection();
                return jsonObject;
        }*/
}
