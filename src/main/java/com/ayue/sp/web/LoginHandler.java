package com.ayue.sp.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.po.UserAfter;
import com.ayue.sp.service.LoginService;
import com.ayue.sp.service.RankService;
import com.ayue.sp.tools.pay.TokenUtils;
import com.ayue.sp.tools.pay.WeixinCheckoutUtil;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.po.User;
import com.ayue.sp.service.AddressService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.AuthUtil;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月19日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/login")
public class LoginHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private RankZSetCDao rankZSetCDao;
        @Autowired
        private LoginService loginService;
        @Autowired
        private OnlineUserTool onlineUserTool;
        @Autowired
        private AddressService addressService;
        @Value("${wx.login.callback.net_url}")
        private String redirectUri;

        // 用户微信授权
        @ResponseBody
        @RequestMapping(value = "/wxToken",method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public String wxToken(String signature, String timestamp, String nonce, String echostr) {
                logger.info("login/wx request signature:" + signature +"timestamp:" + timestamp+ "nonce:"+ nonce +"echostr:"+echostr);
                try {
                        boolean b = WeixinCheckoutUtil.checkSignature(signature, timestamp, nonce);
                        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
                        if (signature != null && WeixinCheckoutUtil.checkSignature(signature, timestamp, nonce)) {
                                return echostr;
                        }
                }catch (Exception e){
                        e.printStackTrace();
                }

                return "错误";
        }
        // 用户微信登录信息
        @ResponseBody
        @RequestMapping(value = "/wxLogin", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public void wxLogin(HttpServletResponse response) throws IOException {
                logger.info("login/wxLogin request login"+"redirectUri:"+redirectUri );
                try {
                        // 这里是回调的url
                        String redirect_uri = URLEncoder.encode(redirectUri, "UTF-8");
                        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?"
                                + "appid="+IConstants.APP_ID
                                + "&redirect_uri="+redirect_uri
                                + "&response_type=code"
                                + "&scope="+"snsapi_base"
                                + "&state=STATE#wechat_redirect";
                        response.sendRedirect(url);

                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
        }
        // 用户微信登录回调
        @ResponseBody
        @RequestMapping(value = "/wxCallback", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public String wxCallback(HttpServletRequest request, HttpServletResponse response,String code) throws ClientProtocolException, IOException {
                logger.info("login/wxCallback request Callback code:"+code);
                JSONObject result = new JSONObject();
                try {
                        // 获取回调地址中的code
                        //String code = request.getParameter("code");
                        // 拼接url
                        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + IConstants.APP_ID
                                + "&secret=" + IConstants.WX_APPSECRET
                                + "&code="+code
                                + "&grant_type=authorization_code";
                        net.sf.json.JSONObject jsonObject = AuthUtil.doGetJson(url);
                        //判断是否获取到openId
                        if (!jsonObject.has("openid")){
                                jsonObject.put("openid", "");
                        }
                        // 1.获取微信用户的openid
                        String openid = jsonObject.getString("openid");
                        if (!jsonObject.has("access_token")){
                                jsonObject.put("access_token", "");
                        }
                        // 2.获取获取access_token
                        String access_token = jsonObject.getString("access_token");
                        // 3.获取微信用户信息
                        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid + "&lang=zh_CN";
                        net.sf.json.JSONObject userInfo = AuthUtil.doGetJson(infoUrl);
                        result.put("userInfo",userInfo);
                        logger.info("wxCallback:"+userInfo);
                        return result.toJSONString();
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return "get userInfo has some problem";

        }

        // 用户注册
        @ResponseBody
        @RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String register(String openId, String name, String avatar, HttpServletRequest request) {
                logger.info("login/register request openId:" + openId + " ;name:" + name + " ;avatar:" + avatar);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(openId);
                        if (user == null) {
                                String city = addressService.getCityAdress(request);
                                user = userService.initUser(openId, name, avatar, city);
                                rankZSetCDao.addWeekTicket(user.getId(),0);
                                userService.addUserLoginRecord(user.getId());
                        }
                        result.put("userId", user.getId());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户登录
        @ResponseBody
        @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String login(String openId, HttpServletRequest request) {
                logger.info("login/login request openId:" + openId);
                JSONObject result = new JSONObject();
                try {
                        if (Formula.isEmptyString(openId)) {
                                return result.toJSONString();
                        }
                        User user = userService.getUser(openId);
                        if (user == null) {
                                result.put("isNewUser", true);
                                logger.info("isNewUser" + true);
                        } else {
                                if (user.getStatus()==0){
                                        result.put("isNewUser", false);
                                        result.put("userId",user.getId());
                                        result.put("openid",user.getOpenid());
                                        userService.updateUserHistory(user);
                                        String city = addressService.getCityAdress(request);
                                        userService.updateUserCity(user.getId(), city);
                                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(user.getId());
                                        if (onlineUser == null) {
                                                onlineUser = onlineUserTool.initOnlineUser(user.getId());
                                        }
                                        userService.addUserLoginRecord(user.getId());
                                        onlineUser.updateLastRequestTime();
                                        result.put("userId", onlineUserTool.getOnlineUser(user.getId()).getUserId());
                                        result.put("sendTime", onlineUserTool.getOnlineUser(user.getId()).getLastRequestTime());
                                        result.put("isOnline", onlineUser==null?false:true);
                                }else {
                                        result.put("userStatus",1);
                                        return result.toJSONString();
                                }
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        /*// 心跳协议
        @ResponseBody
        @RequestMapping(value = "/heartbeat", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String heartbeat(int userId) {
                logger.info("login/heartbeat request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        onlineUser.updateLastRequestTime();
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }*/

        // 提下线协议
        @ResponseBody
        @RequestMapping(value = "/kickOnline", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String kickOnline(int userId) {
                logger.info("login/kickOnline request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser != null) {
                                onlineUserTool.removeOnlineUser(userId);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 后台登录
        @ResponseBody
        @RequestMapping(value = "userLogin",method = RequestMethod.POST,produces ="application/json;charset=UTF-8" )
        public String userLogin(String userName,String password){
                logger.info("login/userLogin request userName:" + userName +"password:"+password);
                JSONObject result = new JSONObject();
                try {
                        UserAfter user=userService.getUserLogin(userName,password);
                        if (user==null){
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        if (!user.getPassword().equals(password)){
                                result.put("isSuccess", false);
                                result.put("msg", "密码不对");
                                return result.toJSONString();
                        }
                        String token = TokenUtils.token(user.getId(), password);
                        loginService.addUserToken(user.getId().toString(),token);
                        result.put("isSuccess", true);
                        result.put("token",token);
                        return result.toJSONString();
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        //token验证
        @ResponseBody
        @RequestMapping(value = "/token",method = RequestMethod.GET,produces ="application/json;charset=UTF-8" )
        public String token(String token){
                logger.info("login/kickOnline request token:" + token );
                JSONObject result = new JSONObject();
                try {
                        Integer s = TokenUtils.parseJwt(token);
                        if (s==null){
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        User user=userService.getUser(s);
                        if (user==null){
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        result.put("isSuccess", true);
                        result.put("id",user.getId());
                        return result.toJSONString();
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }



}
