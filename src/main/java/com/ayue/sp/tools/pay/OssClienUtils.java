package com.ayue.sp.tools.pay;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import com.aliyun.oss.ClientConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class OssClienUtils {

    private Logger logger = Logger.getLogger(getClass());
    //static Log log = LogFactory.getLog(OssClienUtils.class);
    // endpoint以北京为例，其它region请按实际情况填写
    private String endpoint = "http://oss-cn-zhangjiakou.aliyuncs.com";
    // accessKey和accessKeySecret 为购买阿里云服务时官方提供
    private String accessKeyId = "LTAIiPpEHiYNJPRO";
    private String accessKeySecret = "NPR12FpJDLnDXlBSy5fwCNL7cgjmg9";
    //空间
    private String bucketName = "yidaokudisk1";

    //文件存储目录    (上传时在key前面加上目录 默认创建)
    private String date = "four/img/";



    private OSSClient ossClient;

    public OssClienUtils() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setSupportCname(false);
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret,conf);
    }

    public void init() {
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    public void destory() {
        ossClient.shutdown();
    }

    public void uploadImg2Oss(String url) throws Exception {
        File fileOnServer = new File(url);
        FileInputStream fin;
        try {
            fin = new FileInputStream(fileOnServer);
            String[] split = url.split("/");
            this.uploadFile2OSS(fin, split[split.length - 1]);
        } catch (FileNotFoundException e) {
            throw new Exception("图片上传失败1");
        }
    }

    public String uploadImg2Oss(MultipartFile file) throws Exception {
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new Exception("上传图片大小不能超过10M！");
        }
        String originalFilename = file.getOriginalFilename();
        logger.info("originalFilename:"+originalFilename);
        String substring = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        Random random = new Random();
        String name = random.nextInt(10000) + System.currentTimeMillis() + substring;
        /*try {*/
            InputStream inputStream = file.getInputStream();
            this.uploadFile2OSS(inputStream, name);
            return name;
        /*} catch (Exception e) {
            e.printStackTrace();
            throw new Exception("图片上传失败2");
        }*/
    }
    public String getImgUrl(String fileUrl) {
        System.out.println("fileUrl="+fileUrl);
        if (!StringUtils.isEmpty(fileUrl)) {
            String[] split = fileUrl.split("/");
            return this.getUrl(this.date + split[split.length - 1]);
        }
        return null;
    }
    public String uploadFile2OSS(InputStream instream, String fileName) {
        String ret = "";
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            PutObjectResult putResult = ossClient.putObject(bucketName, date + fileName, instream, objectMetadata);
            ret = putResult.getETag();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static String getcontentType(String filenameExtension) {

        if (filenameExtension.equalsIgnoreCase("bmp")) {
            return "image/bmp";
        }
        if (filenameExtension.equalsIgnoreCase("gif")) {
            return "image/gif";
        }
        if (filenameExtension.equalsIgnoreCase("jpeg") || filenameExtension.equalsIgnoreCase("jpg") || filenameExtension.equalsIgnoreCase("png")) {
            return "image/jpeg";
        }
        if (filenameExtension.equalsIgnoreCase("html")) {
            return "text/html";
        }
        if (filenameExtension.equalsIgnoreCase("txt")) {
            return "text/plain";
        }
        if (filenameExtension.equalsIgnoreCase("vsd")) {
            return "application/vnd.visio";
        }
        if (filenameExtension.equalsIgnoreCase("pptx") || filenameExtension.equalsIgnoreCase("ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (filenameExtension.equalsIgnoreCase("docx") || filenameExtension.equalsIgnoreCase("doc")) {
            return "application/msword";
        }
        if (filenameExtension.equalsIgnoreCase("xml")) {
            return "text/xml";
        }
        return "image/jpeg";
    }

    public String getUrl(String key) {

        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);

        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);

        if (url != null) {
            System.out.println("url="+url.toString());
            return url.toString();
        }
        return null;
    }
}