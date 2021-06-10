package com.ayue.sp.tools.pay;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.alibaba.druid.sql.parser.Token;
import com.alibaba.fastjson.JSONException;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.service.QAService;
import com.ayue.sp.tools.pay.wx.WXPay;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sun.net.www.http.HttpClient;

/**
 * 2020年9月12日
 *
 * @author ayue
 */
public class WxUtil {
        private Logger logger = Logger.getLogger(getClass());

        /**
         * 获取随机字符串 Nonce Str
         *
         * @return String 随机字符串
         */
        public static String generateNonceStr() {
                return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
        }

        /**
         * 生成签名. 注意，若含有sign_type字段，必须和signType参数保持一致。
         *
         * @param data
         *                待签名数据
         * @param key
         *                API密钥
         * @param
         *
         * @return 签名
         */
        public static String generateSignature(final Map<String, String> data, String key) throws Exception {
                Set<String> keySet = data.keySet();
                String[] keyArray = keySet.toArray(new String[keySet.size()]);
                Arrays.sort(keyArray);
                StringBuilder sb = new StringBuilder();
                for (String k : keyArray) {
                        if (data.get(k).trim().length() > 0) { // 参数值为空，则不参与签名
                                sb.append(k).append("=").append(data.get(k).trim()).append("&");
                        }
                }
                sb.append("key=").append(key);
                return MD5(sb.toString()).toUpperCase();
        }

        /**
         * 生成 MD5
         *
         * @param data
         *                待处理数据
         * @return MD5结果
         */
        public static String MD5(String data) throws Exception {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] array = md.digest(data.getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte item : array) {
                        sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
                }
                return sb.toString().toUpperCase();
        }

        // Map转xml数据
        public static String GetMapToXML(Map<String, String> param) {
                StringBuffer sb = new StringBuffer();
                sb.append("<xml>");
                for (Map.Entry<String, String> entry : param.entrySet()) {
                        sb.append("<" + entry.getKey() + ">");
                        sb.append(entry.getValue());
                        sb.append("</" + entry.getKey() + ">");
                }
                sb.append("</xml>");
                return sb.toString();
        }

        /**
         * xml解析
         * 
         * @param strxml
         * @return
         * @throws Exception
         */
        public static Map<String, String> doXMLParse(String strxml) throws Exception {
                Map<String, String> m = new HashMap<String, String>();
                try {
                        strxml = strxml.replaceFirst("encoding=\".*\"", "encoding=\"UTF-8\"");
                        if (null == strxml || "".equals(strxml)) {
                                return null;
                        }
                        InputStream in = new ByteArrayInputStream(strxml.getBytes("UTF-8"));
                        SAXBuilder builder = new SAXBuilder();
                        Document doc = builder.build(in);
                        Element root = doc.getRootElement();
                        List list = root.getChildren();
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                                Element e = (Element) it.next();
                                String k = e.getName();
                                String v = "";
                                List children = e.getChildren();
                                if (children.isEmpty()) {
                                        v = e.getTextNormalize();
                                } else {
                                        v = getChildrenText(children);
                                }

                                m.put(k, v);
                        }

                        // 关闭流
                        in.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return m;

        }

        /**
         * 获取子节点数据
         * 
         * @param children
         * @return
         */
        public static String getChildrenText(List children) {
                StringBuffer sb = new StringBuffer();
                if (!children.isEmpty()) {
                        Iterator it = children.iterator();
                        while (it.hasNext()) {
                                Element e = (Element) it.next();
                                String name = e.getName();
                                String value = e.getTextNormalize();
                                List list = e.getChildren();
                                sb.append("<" + name + ">");
                                if (!list.isEmpty()) {
                                        sb.append(getChildrenText(list));
                                }
                                sb.append(value);
                                sb.append("</" + name + ">");
                        }
                }
                return sb.toString();
        }


        public static JSONObject doGetStr(String url) throws IOException {
                DefaultHttpClient client = new DefaultHttpClient();//获取DefaultHttpClient请求
                HttpGet httpGet = new HttpGet(url);//HttpGet将使用Get方式发送请求URL
                JSONObject jsonObject = null;
                HttpResponse response = client.execute(httpGet);//使用HttpResponse接收client执行httpGet的结果
                HttpEntity entity = response.getEntity();//从response中获取结果，类型为HttpEntity
                if (entity != null) {
                        String result = EntityUtils.toString(entity, "UTF-8");//HttpEntity转为字符串类型
                        jsonObject = JSONObject.fromObject(result);//字符串类型转为JSON类型
                }
                return jsonObject;
        }


        // 发起微信支付请求
        public static String httpsRequest(String requestUrl, String requestMethod, String outputStr) throws IOException {
                // 原文：https://www.cnblogs.com/wang-yaz/p/8632624.html
                // import com.github.wxpay.sdk.WXPay;
                // wxPay.unifiedOrder 这个方法中调用微信统一下单接口
                /** 加载证书 这里证书需要到微信商户平台进行下载 */
                // 证书：apiclient_cert.p12
                 //public OurWxPayConfig() throws Exception{
                 /*InputStream certStream =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");
                 this.certData = IOUtils.toByteArray(certStream);
                 certStream.close();
                 //}
                 OurWxPayConfig ourWxPayConfig = new OurWxPayConfig();
                 WXPay wxPay = new WXPay(ourWxPayConfig);
                 Map<String, String> respData = wxPay.unifiedOrder(data);*/
                try {
                        initCert();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                try {
                        //InputStream certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");

                        URL url = new URL(requestUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
                        // 设置请求方式（GET/POST）
                        conn.setRequestMethod(requestMethod);
                        //conn.setRequestProperty("Content-type", "text/xml");
                        // 当outputStr不为null时向输出流写数据
                        if (null != outputStr) {
                                OutputStream outputStream = conn.getOutputStream();
                                // 注意编码格式
                                outputStream.write(outputStr.getBytes("UTF-8"));
                                outputStream.close();
                        }
                        // 从输入流读取返回内容
                        InputStream inputStream = conn.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String str = null;
                        StringBuffer buffer = new StringBuffer();
                        while ((str = bufferedReader.readLine()) != null) {
                                buffer.append(str);
                        }
                        // 释放资源
                        bufferedReader.close();
                        inputStreamReader.close();
                        inputStream.close();
                        inputStream = null;
                        conn.disconnect();
                        return buffer.toString();
                } catch (ConnectException ce) {
                        ce.printStackTrace();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return null;
        }
        // 发起微信支付请求
        public static JSONObject httpRequest(String requestUrl, String requestMethod, String outputStr) throws IOException {
                // 原文：https://www.cnblogs.com/wang-yaz/p/8632624.html
                // import com.github.wxpay.sdk.WXPay;
                // wxPay.unifiedOrder 这个方法中调用微信统一下单接口
                /** 加载证书 这里证书需要到微信商户平台进行下载 */
                // 证书：apiclient_cert.p12
                //public OurWxPayConfig() throws Exception{
                 /*InputStream certStream =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");
                 this.certData = IOUtils.toByteArray(certStream);
                 certStream.close();
                 //}
                 OurWxPayConfig ourWxPayConfig = new OurWxPayConfig();
                 WXPay wxPay = new WXPay(ourWxPayConfig);
                 Map<String, String> respData = wxPay.unifiedOrder(data);*/
                try {
                        initCert();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                try {
                        //InputStream certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");

                        URL url = new URL(requestUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        conn.setDoOutput(true);
                        conn.setDoInput(true);
                        conn.setUseCaches(false);
                        // 设置请求方式（GET/POST）
                        conn.setRequestMethod(requestMethod);
                        //conn.setRequestProperty("Content-type", "text/xml");
                        // 当outputStr不为null时向输出流写数据
                        if (null != outputStr) {
                                OutputStream outputStream = conn.getOutputStream();
                                // 注意编码格式
                                outputStream.write(outputStr.getBytes("UTF-8"));
                                outputStream.close();
                        }
                        // 从输入流读取返回内容
                        InputStream inputStream = conn.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String str = null;
                        StringBuffer buffer = new StringBuffer();
                        while ((str = bufferedReader.readLine()) != null) {
                                buffer.append(str);
                        }
                        // 释放资源
                        bufferedReader.close();
                        inputStreamReader.close();
                        inputStream.close();
                        inputStream = null;
                        conn.disconnect();
                        return JSONObject.fromObject(buffer.toString());
                } catch (ConnectException ce) {
                        ce.printStackTrace();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return null;
        }

        /**
         * 加载证书
         *
         * @param
         * @param
         * @throws Exception
         */
        private static void initCert() throws Exception {
                InputStream certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");

                // 证书密码，默认为商户ID
                String key = IConstants.MCH_ID;
                // 证书的路径
                String path = String.valueOf(certStream);
                // 指定读取证书格式为PKCS12
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                // 读取本机存放的PKCS12证书文件
                File file = new File(path);
                InputStream in = new FileInputStream(file);
                try {
                        // 指定PKCS12的密码(商户ID)
                        keyStore.load(in, key.toCharArray());
                } finally {
                        in.close();
                }
                SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, key.toCharArray()).build();
                SSLConnectionSocketFactory sslsf =
                        new SSLConnectionSocketFactory(sslcontext, new String[] {"TLSv1"}, null,
                                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                CloseableHttpClient build = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        }
        public static String doRefund(String url,String xmlData) throws Exception {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                InputStream instream = Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12");

                //FileInputStream instream = new FileInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream("cert/apiclient_cert.p12"));//P12文件目录  写证书的项目路径
                try {
                        keyStore.load(instream, IConstants.MCH_ID.toCharArray());//这里写密码..默认是你的MCHID 证书密码
                } finally {
                        instream.close();
                }


                SSLContext sslcontext = SSLContexts.custom()
                        .loadKeyMaterial(keyStore, IConstants.MCH_ID.toCharArray())//这里也是写密码的
                        .build();

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        sslcontext,
                        new String[] { "TLSv1" },
                        null,
                        SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                CloseableHttpClient httpclient = HttpClients.custom()
                        .setSSLSocketFactory(sslsf)
                        .build();
                try {
                        HttpPost httpost = new HttpPost(url); // 设置响应头信息
                        httpost.addHeader("Connection", "keep-alive");
                        httpost.addHeader("Accept", "*/*");
                        httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                        httpost.addHeader("Host", "api.mch.weixin.qq.com");
                        httpost.addHeader("X-Requested-With", "XMLHttpRequest");
                        httpost.addHeader("Cache-Control", "max-age=0");
                        httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
                        httpost.setEntity(new StringEntity(xmlData, "UTF-8"));
                        CloseableHttpResponse response = httpclient.execute(httpost);
                        try {
                                HttpEntity entity = response.getEntity();

                                String returnMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
                                EntityUtils.consume(entity);
                                return returnMessage; //返回后自己解析结果
                        } finally {
                                response.close();
                        }
                } finally {
                        httpclient.close();
                }
        }

        /**
         * XML格式字符串转换为Map
         *
         * @param strXML
         *                XML字符串
         * @return XML数据转换后的Map
         * @throws Exception
         */
        public static Map<String, String> xmlToMap(String strXML) throws Exception {
                Map<String, String> data = new HashMap<String, String>();
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
                org.w3c.dom.Document doc = documentBuilder.parse(stream);
                doc.getDocumentElement().normalize();
                NodeList nodeList = doc.getDocumentElement().getChildNodes();
                for (int idx = 0; idx < nodeList.getLength(); ++idx) {
                        Node node = nodeList.item(idx);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                                data.put(element.getNodeName(), element.getTextContent());
                        }
                }
                try {
                        stream.close();
                } catch (Exception ex) {

                }
                return data;
        }

        /**
         * IO解析获取微信的数据
         * 
         * @param request
         * @return
         */
        public static String getXmlString(HttpServletRequest request) throws Exception {
                BufferedReader reader = null;
                String line = "";
                String xmlString = null;
                try {
                        reader = request.getReader();
                        StringBuffer inputString = new StringBuffer();

                        while ((line = reader.readLine()) != null) {
                                inputString.append(line);
                        }
                        xmlString = inputString.toString();
                } catch (Exception e) {
                        throw new Exception(e);
                }
                return xmlString;
        }

        /**
         * 返回给微信服务端的xml
         * 
         * @param return_code
         * @return
         */
        public static String returnXML(String return_code) {

                return "<xml><return_code><![CDATA["

                                + return_code

                                + "]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        }

        public static String getLocalIP() {
                InetAddress addr = null;
                try {
                        addr = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                        e.printStackTrace();
                }
                byte[] ipAddr = addr.getAddress();
                String ipAddrStr = "";
                for (int i = 0; i < ipAddr.length; i++) {
                        if (i > 0) {
                                ipAddrStr += ".";
                        }
                        ipAddrStr += ipAddr[i] & 0xFF;
                }
                return ipAddrStr;
        }
        /**
         * 2018年10月24日更新
         * String转map
         * @param str
         * @return
         */
        public static Map<String,String> getStringToMap(String str){
                //感谢bojueyou指出的问题
                //判断str是否有值
                if(null == str || "".equals(str)){
                        return null;
                }
                //根据&截取
                String[] strings = str.split(",");
                //设置HashMap长度
                int mapLength = strings.length;
                //判断hashMap的长度是否是2的幂。
                if((strings.length % 2) != 0){
                        mapLength = mapLength+1;
                }

                Map<String,String> map = new HashMap<>();
                //循环加入map集合
                for (int i = 0; i < strings.length; i++) {
                        //截取一组字符串
                        String[] strArray = strings[i].split(":");
                        //strArray[0]为KEY  strArray[1]为值
                        map.put(strArray[0],strArray[1]);
                }
                return map;
        }
        public static String wxMessage(String openId,int type,String userName) {
                com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
                try {
                        QAService qaService = new QAService();
                        String message=qaService.message(openId,type,userName);
                        String replace = IConstants.WX_TOKEN_Message_URL.replace("APPID", IConstants.APP_ID).replace("APPSECRET", IConstants.WX_APPSECRET);
                        String get = WxUtil.httpsRequest(replace, "GET", null);
                        net.sf.json.JSONObject token = net.sf.json.JSONObject.fromObject(get);
                        String access_token = token.getString("access_token");
                        String mapToken = IConstants.WX_Message_URL.replace("ACCESS_TOKEN", access_token);
                        String post = WxUtil.httpsRequest(mapToken, "POST", message);
                        net.sf.json.JSONObject msg = net.sf.json.JSONObject.fromObject(post);
                        result.put("isSuccess",msg.getString("errmsg"));
                        if ("0".equals(msg.getString("errcode"))){
                                result.put("isSuccess",true);
                                return result.toJSONString();
                        }else {
                                result.put("msg",msg.getString("errmsg"));
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                }

                return result.toJSONString();
        }
}
