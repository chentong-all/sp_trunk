package com.ayue.sp.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.db.po.*;
import com.ayue.sp.tools.pay.wx.WXPay;
import com.ayue.sp.tools.pay.wx.WXPayConstants;
import com.ayue.sp.tools.pay.wx.WXPayUtil;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.dao.UserCashoutMapper;
import com.ayue.sp.db.dao.UserConvertMapper;
import com.ayue.sp.db.dao.UserRechargeMapper;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.ayue.sp.tools.pay.WxUtil;
import com.ayue.sp.tools.pay.wx.WXPay;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * 2020年9月4日
 *
 * @author ayue
 */
@Service
public class PayService {
        @Autowired
        private UserCashoutMapper userCashoutMapper;
        @Autowired
        private UserConvertMapper userConvertMapper;
        @Autowired
        private UserRechargeMapper userRechargeMapper;
        @Autowired
        private IdGeneratorTools idGeneratorTools;
        @Value("${third.callback.net_url}")
        private String notifyUrl;

        public UserRecharge addUserRecharge(int userId, int moneyCount, String outTradeNo) {
                UserRecharge userRecharge = new UserRecharge();
                userRecharge.setOutTradeNo(outTradeNo);
                userRecharge.setUserId(userId);
                userRecharge.setMoneyCount(moneyCount);
                userRecharge.setStatus(IConstants.RECHARGE_STATUS_0);
                userRechargeMapper.insert(userRecharge);
                return userRecharge;
        }

        public void addUserCashout(int userId, int moneyCount, String partnerTradeNo, boolean isSucess) {
                UserCashout userCashout = new UserCashout();
                userCashout.setPartnerTradeNo(partnerTradeNo);
                userCashout.setUserId(userId);
                userCashout.setMoneyCount(moneyCount);
                userCashout.setIsSuccess(isSucess);
                userCashoutMapper.insert(userCashout);
        }

        public void addUserConvert(int userId, int ticketCount, boolean isSuccess) {
                UserConvert userConvert = new UserConvert();
                userConvert.setId(idGeneratorTools.getUserConvertId());
                userConvert.setUserId(userId);
                userConvert.setTicketCount(ticketCount);
                userConvert.setIsSuccess(isSuccess);
                userConvertMapper.insert(userConvert);
        }

        public List<UserRecharge> getUserRechargesByUserId(int userId) {
                UserRechargeExample example = new UserRechargeExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userRechargeMapper.selectByExample(example);
        }
        public List<UserConvert> getUserConvert(int userId){
                UserConvertExample example = new UserConvertExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userConvertMapper.selectByExample(example);
        }

        public List<UserRecharge> getUserRecharges(int page) {
                UserRechargeExample example = new UserRechargeExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserRecharge>(userRechargeMapper.selectByExample(example)).getList();
        }
        public List<UserRecharge> getUserRechargeByUserIds(List<Integer> userIds,int page){
                UserRechargeExample example = new UserRechargeExample();
                example.createCriteria().andUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserRecharge>(userRechargeMapper.selectByExample(example)).getList();

        }
        public List<UserRecharge> getUserRechargeList(List<Integer> userIds){
                UserRechargeExample example = new UserRechargeExample();
                example.createCriteria().andUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                return new PageInfo<UserRecharge>(userRechargeMapper.selectByExample(example)).getList();

        }
        public Integer getUserRechargeCount(){
                return userRechargeMapper.getUserRechargeCount();
        }

        public List<UserCashout> getUserCashoutsByUserId(int userId) {
                UserCashoutExample example = new UserCashoutExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userCashoutMapper.selectByExample(example);
        }

        public List<UserCashout> getUserCashouts(int page) {
                UserCashoutExample example = new UserCashoutExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserCashout>(userCashoutMapper.selectByExample(example)).getList();
        }
        public List<UserCashout> getUserCashoutByUserIds(List<Integer> userIds,int page){
                UserCashoutExample example = new UserCashoutExample();
                example.createCriteria().andUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserCashout>(userCashoutMapper.selectByExample(example)).getList();

        }
        public List<UserCashout> getUserCashoutList(List<Integer> userIds){
                UserCashoutExample example = new UserCashoutExample();
                example.createCriteria().andUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                return new PageInfo<UserCashout>(userCashoutMapper.selectByExample(example)).getList();

        }
        public Integer getUserCashoutCount(){
                return userCashoutMapper.getUserCashoutCount();
        }

        public String WXRechargeParamGenerate(String outTradeNo, int totalFee, String openId, HttpServletRequest request) throws Exception {
                Map<String, String> param = new LinkedHashMap<String, String>();
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
                param.put("appid", IConstants.APP_ID);
                param.put("body", "xundao");// 商品描述==商品或支付单简要描述
                param.put("mch_id", IConstants.MCH_ID);// 微信支付商户号==登陆微信支付后台，即可看到
                param.put("nonce_str", WxUtil.generateNonceStr());// 随机字符串
                param.put("notify_url", notifyUrl);// 回调通知地址
                param.put("openid", openId);// openid
                param.put("out_trade_no", outTradeNo);// 商户订单号
                param.put("spbill_create_ip", ip);// 终端ip
                if (totalFee!=0) {
                        param.put("total_fee", totalFee*100 + "");// 金额
                }
                param.put("trade_type", "JSAPI");// 交易类型,JSAPI--公众号支付、NATIVE--原生扫码支付、APP--app支付、MWEB--H5
                String sign = WxUtil.generateSignature(param, IConstants.WX_PARTNERKEY);
                param.put("sign", sign);// 签名==官方给的签名算法
                return WxUtil.GetMapToXML(param);
        }
        public String WXOrder(String outTradeNo) throws Exception {
                Map<String, String> param = new LinkedHashMap<String, String>();
                param.put("appid", IConstants.APP_ID);
                param.put("mch_id", IConstants.MCH_ID);// 微信支付商户号==登陆微信支付后台，即可看到
                param.put("nonce_str", WxUtil.generateNonceStr());// 随机字符串
                param.put("out_trade_no", outTradeNo);// 商户订单号
                String sign = WxUtil.generateSignature(param, IConstants.WX_PARTNERKEY);
                param.put("sign", sign);// 签名==官方给的签名算法
                return WxUtil.GetMapToXML(param);
        }

        public String WXCashoutParamGenerate(String openId, String partnerTradeNo, int totalFee) throws Exception {
                Map<String, String> param = new HashMap<String, String>();
                param.put("mch_appid", IConstants.APP_ID);
                param.put("mchid", IConstants.MCH_ID);// 微信支付商户号==登陆微信支付后台，即可看到
                param.put("nonce_str", WxUtil.generateNonceStr());// 随机字符串
                param.put("partner_trade_no", partnerTradeNo);// 订单号
                param.put("openid", openId);// 商户订单号
                param.put("check_name", "NO_CHECK");
                param.put("amount", totalFee*100 + "");// 金额
                param.put("desc", "用户提现");
                param.put("spbill_create_ip", WxUtil.getLocalIP());
                String sign = WxUtil.generateSignature(param, IConstants.WX_PARTNERKEY);
                param.put("sign", sign);// 签名==官方给的签名算法
                return WxUtil.GetMapToXML(param);
        }

        public UserRecharge getUserRecharge(String outTradeNo) {
                return userRechargeMapper.selectByPrimaryKey(outTradeNo);
        }

        public UserRecharge getUserRechargeByOutTradeNo(String outTradeNo) {
                UserRechargeExample example = new UserRechargeExample();
                example.createCriteria().andOutTradeNoEqualTo(outTradeNo);
                List<UserRecharge> userRecharges = userRechargeMapper.selectByExample(example);
                if (Formula.isEmptyCollection(userRecharges)) {
                        return null;
                }
                return userRecharges.get(0);
        }

        public void updateRechargeSuccess(String outTradeNo) {
                userRechargeMapper.updateByStatus(outTradeNo);
        }
        public void updateStatus(String outTradeNo){
            userRechargeMapper.updateStatus(outTradeNo);
        }

        public void updateRechargeFail(String outTradeNo) {
                userRechargeMapper.updateStatus(outTradeNo);
        }

        public int getRechargeCount() {
                List<UserRecharge> userRecharges = userRechargeMapper.selectByExample(null);
                int ticket=0;
                for (UserRecharge u:userRecharges) {
                        if (u.getStatus()==1){
                                ticket+=u.getMoneyCount();
                        }
                }
                return ticket;
        }

        public int getTodayRechargeCount() {
                UserRechargeExample example = new UserRechargeExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                List<UserRecharge> userRecharges = userRechargeMapper.selectByExample(example);
                int rechargeCount = 0;
                for (UserRecharge userRecharge : userRecharges) {
                        if (userRecharge.getStatus()==1) {
                                rechargeCount += userRecharge.getMoneyCount();
                        }
                }
                return rechargeCount;
        }

        public int getCashoutCount() {
                List<UserCashout> userCashouts = userCashoutMapper.selectByExample(null);
                int money=0;
                for (UserCashout u:userCashouts) {
                        money+=u.getMoneyCount();
                }
                return money;
        }

        public int getTodayCashoutCount() {
                UserCashoutExample example = new UserCashoutExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                List<UserCashout> userCashouts = userCashoutMapper.selectByExample(example);
                int cashoutCount = 0;
                for (UserCashout userCashout : userCashouts) {
                        cashoutCount += userCashout.getMoneyCount();
                }
                return cashoutCount;
        }
}
