package com.ayue.sp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * 2020年8月27日
 *
 * @author ayue
 */
@Service
public class AddressService {
        private Logger logger = Logger.getLogger(getClass());

        public String getCityAdress(HttpServletRequest request) {
                String ip = request.getHeader("x-forwarded-for");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                        ip = request.getRemoteAddr();
                }
                // ip = "223.70.163.0";// TODO
                try {
                        JSONObject jo = JSONObject.parseObject(this.sendGET(ip));
                        String cityName = ((String) jo.get("city")).trim();
                        return cityName;
                } catch (Exception e) {
                        logger.info("ip:" + ip);
                        logger.info("get address has some error");
                        e.printStackTrace();
                }
                return "中国";
        }

        public String sendGET(String ip) {

                String tpy = "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;
                // 访问返回结果
                String result = "";
                // 读取访问结果
                BufferedReader read = null;

                try {
                        // 创建url
                        URL realurl = new URL(tpy);
                        // 打开连接
                        URLConnection connection = realurl.openConnection();
                        // 设置通用的请求属性
                        connection.setRequestProperty("accept", "*/*");
                        connection.setRequestProperty("connection", "Keep-Alive");
                        connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
                        // 建立连接
                        connection.connect();

                        // 获取所有响应头字段
                        Map<String, List<String>> map = connection.getHeaderFields();

                        // 遍历所有的响应头字段，获取到cookies等
                        for (String key : map.keySet()) {
                                // System.out.println(key + "--->" +
                                // map.get(key));
                        }

                        // 定义 BufferedReader输入流来读取URL的响应
                        read = new BufferedReader(new InputStreamReader(connection.getInputStream(), "gbk"));
                        String line;// 循环读取
                        while ((line = read.readLine()) != null) {
                                result += line;
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                } finally {
                        if (read != null) {// 关闭流
                                try {
                                        read.close();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }
                        }
                }
                return result;
        }
}
