package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.ayue.sp.db.po.UserConvert;
import com.ayue.sp.tools.pay.wx.HttpRequest;
import com.ayue.sp.tools.pay.wx.WXPayUtil;
import com.ayue.sp.tools.pay.wx.WXPayXmlUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.po.User;
import com.ayue.sp.db.po.UserCashout;
import com.ayue.sp.db.po.UserRecharge;
import com.ayue.sp.service.PayService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.pay.WxUtil;


/**
 * 2020年9月4日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/pay")
public class PayHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private PayService payService;

        // 用户下单
        @ResponseBody
        @RequestMapping(value = "/wxPay", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public Map<String, String> wxPay(int userId, int totalFee, String openId, HttpServletRequest request) {
                logger.info("pay/wxPay request userId:" + userId + " ;totalFee:" + totalFee);
                try {
                        String lastXml = "";
                        Map<String, String> hashMap = new HashMap<>();
                        Map<String, String> result = new HashMap<>();
                        String outTradeNo = "SP-" + System.currentTimeMillis();
                        String xml = payService.WXRechargeParamGenerate(outTradeNo, totalFee,openId,request);
                        // 发送http请求到微信服务端，获取返回的参数
                        String res = WxUtil.httpsRequest(IConstants.WX_PAYURL, "POST", xml);
                        Map<String, String> data = WxUtil.doXMLParse(res);
                        logger.info("微信返回的回调结果是：：：：：：：" + data);
                        payService.addUserRecharge(userId, totalFee, outTradeNo);
                        String returnCode = data.get("return_code");
                        if (returnCode.equals("SUCCESS")) {
                                String resultCode = data.get("result_code");
                                if (resultCode.equals("SUCCESS")) {
                                        try {
                                                User user = userService.getUser(openId);
                                                if (user == null) {
                                                        payService.updateRechargeFail(outTradeNo);
                                                }
                                                UserRecharge userRecharge = payService.getUserRechargeByOutTradeNo(outTradeNo);
                                                if (userRecharge == null) {
                                                        payService.updateRechargeFail(outTradeNo);
                                                }
                                                if (userRecharge.getStatus() == IConstants.RECHARGE_STATUS_0) {
                                                        int ticketCount = userRecharge.getMoneyCount() * 10;
                                                        user.setTicket(user.getTicket() + ticketCount);
                                                        user.setPayCount(user.getPayCount() + userRecharge.getMoneyCount());
                                                        userService.updateUser(user);
                                                        payService.updateRechargeSuccess(outTradeNo);
                                                }
                                        } finally {
                                        }
                                }else {
                                        lastXml = WxUtil.returnXML("FAIL");
                                        result.put("isSuccess",lastXml);
                                        return result;
                                }
                        } else {
                                lastXml = WxUtil.returnXML("FAIL");
                                result.put("isSuccess",lastXml);
                                return result;
                        }
                        hashMap.put("appId", data.get("appid"));
                        hashMap.put("timeStamp", WXPayUtil.getCurrentTimestamp()+"");
                        hashMap.put("nonceStr", data.get("nonce_str"));
                        hashMap.put("signType", "MD5");
                        hashMap.put("package", "prepay_id=" +data.get("prepay_id"));
                        hashMap.put("paySign", WXPayUtil.generateSignature(hashMap,IConstants.WX_PARTNERKEY));
                        hashMap.put("outTradeNo", outTradeNo);
                        return hashMap;
                } catch (Exception e) {
                        logger.info("wxPay has some error" + e);
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return null;
        }

        //微信订单
        @ResponseBody
        @RequestMapping(value = "/wxOrder", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public String wxOrder(String outTradeNo) {
                logger.info("pay/wxOrder outTradeNo "+outTradeNo);
                JSONObject result = new JSONObject();
                try {
                        if (outTradeNo!="" && outTradeNo!=null) {
                                UserRecharge userRecharge = payService.getUserRechargeByOutTradeNo(outTradeNo);
                                User user = userService.getUser(userRecharge.getUserId());
                                int ticketCount = userRecharge.getMoneyCount() * 10;
                                user.setTicket(user.getTicket() - ticketCount);
                                user.setPayCount(user.getPayCount() - userRecharge.getMoneyCount());
                                userService.updateUser(user);
                                payService.updateStatus(outTradeNo);
                                result.put("isSuccess",true);
                        }else {
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                } catch (Exception e) {
                        logger.error("wxCallback has some error:" + e);
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 用户充值回调
        @ResponseBody
        @RequestMapping(value = "/wxCallback", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public String wxCallback(HttpServletRequest request) {
                logger.info("pay/wxCallback request "+request);
                String lastXml = "";
                try {
                        logger.info("1------ "+1);
                    ServletInputStream inputStream = request.getInputStream();
                        logger.info("2------ "+inputStream);
                    String xmlString = WXPayUtil.InputStream2String(inputStream);
                    //String xmlString = WxUtil.getXmlString(request);
                        logger.info("微信返回的回调结果是：：：：：：：" + xmlString);
                        if (!"".equals(xmlString)) {
                                // 先解析返回的数据
                                Map<String, String> dataMap = WxUtil.xmlToMap(xmlString);
                                String returnCode = dataMap.get("return_code");
                                if (returnCode.equals("SUCCESS")) {
                                        String resultCode = dataMap.get("result_code");
                                        if (resultCode.equals("SUCCESS")) {
                                                String outTradeNo = dataMap.get("out_trade_no");
                                                UserRecharge userRecharge = payService.getUserRechargeByOutTradeNo(outTradeNo);
                                                userService.lockUser(userRecharge.getUserId());
                                                try {
                                                        User user = userService.getUser(userRecharge.getOutTradeNo());
                                                        if (user == null) {
                                                                payService.updateRechargeFail(outTradeNo);
                                                        }
                                                        userRecharge = payService.getUserRechargeByOutTradeNo(outTradeNo);
                                                        if (userRecharge == null) {
                                                                payService.updateRechargeFail(outTradeNo);
                                                        }
                                                        if (userRecharge.getStatus() == IConstants.RECHARGE_STATUS_0) {
                                                                int ticketCount = userRecharge.getMoneyCount() * 10;
                                                                user.setTicket(user.getTicket() + ticketCount);
                                                                user.setPayCount(user.getPayCount() + userRecharge.getMoneyCount());
                                                                userService.updateUser(user);
                                                                payService.updateRechargeSuccess(outTradeNo);
                                                        }
                                                        lastXml = WxUtil.returnXML("SUCCESS");
                                                } finally {
                                                        userService.unlockUser(userRecharge.getUserId());
                                                }
                                        } else {
                                                lastXml = WxUtil.returnXML("FAIL");
                                        }
                                } else {
                                        lastXml = WxUtil.returnXML("FAIL");
                                }
                        }
                } catch (Exception e) {
                        logger.error("wxCallback has some error:" + e);
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return lastXml;
        }

        // 用户取消充值
        @ResponseBody
        @RequestMapping(value = "/cancelPay", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cancelPay(String outTradeNo) {
                logger.info("pay/outTradeNo request outTradeNo:" + outTradeNo );
                JSONObject result = new JSONObject();
                try {
                        UserRecharge userRecharge = payService.getUserRecharge(outTradeNo);
                        if (userRecharge==null){
                                result.put("msg","订单号错误");
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        User user = userService.getUser(userRecharge.getOutTradeNo());
                        int ticketCount = userRecharge.getMoneyCount() * 10;
                        user.setTicket(user.getTicket() - ticketCount);
                        user.setPayCount(user.getPayCount() - userRecharge.getMoneyCount());
                        userService.updateUser(user);
                        payService.updateRechargeFail(outTradeNo);
                        result.put("msg","取消充值");
                        result.put("isSuccess",true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 用户提现回调
        @ResponseBody
        @RequestMapping(value = "/wxCashout", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String wxCashout(String openId, int totalFee) {
                logger.info("pay/wxCashout request openId:" + openId + " ;totalFee:" + totalFee);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(openId);
                        userService.lockUser(user.getId());
                        try {
                                String partnerTradeNo = "SP" + System.currentTimeMillis();
                                user = userService.getUser(user.getId());
                                if (user == null) {
                                        result.put("userMsg","用户不存在");
                                        result.put("isSuccess", false);
                                        return result.toJSONString();
                                }
                                if (user.getMoney() < totalFee*100) {
                                        result.put("totalFee","金额不足");
                                        result.put("isSuccess", false);
                                        payService.addUserCashout(user.getId(), totalFee, partnerTradeNo, false);
                                        return result.toJSONString();
                                }
                                try {
                                        String xml = payService.WXCashoutParamGenerate(openId, partnerTradeNo, totalFee);
                                        // 发送http请求到微信服务端，获取返回的参数
                                        String res = WxUtil.doRefund(IConstants.WX_CASHOUTURL,  xml);
                                        logger.info("res:"+res);
                                        Map<String, String> data = WxUtil.doXMLParse(res);
                                        String resultCode = data.get("result_code");
                                        if (resultCode.equals("SUCCESS")) {
                                                user.setMoney(user.getMoney() - totalFee * 100);
                                                userService.updateUser(user);
                                                payService.addUserCashout(user.getId(), totalFee, partnerTradeNo, true);
                                                result.put("isSuccess", true);
                                                return result.toJSONString();
                                        } else {
                                                String errCodeDes = data.get("err_code_des");
                                                logger.error("user cashout has some error：" + errCodeDes.replace("付款","提现"));
                                                result.put("msg",errCodeDes);
                                                result.put("isSuccess", false);
                                                return result.toJSONString();
                                        }
                                } catch (Exception e) {
                                        logger.error("user cashout has some error：" + e);
                                        result.put("msg",e);
                                        result.put("isSuccess", false);
                                }
                        } finally {
                                userService.unlockUser(user.getId());
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户票数兑换余额
        @ResponseBody
        @RequestMapping(value = "/convert", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String convert(int userId, int ticket) {
                logger.info("pay/convert request userId:" + userId + " ;ticket:" + ticket);
                JSONObject result = new JSONObject();
                try {
                        userService.lockUser(userId);
                        try {
                                User user = userService.getUser(userId);
                                if (user == null) {
                                        result.put("isSuccess", false);
                                        result.put("msg", "用户不存在");
                                        return result.toJSONString();
                                }
                                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                                double money=0.00;
                                if (user.getLevel()==0){
                                       money= (double)ticket * IConstants.CONVERT_RATE_0;
                                }
                                if (user.getLevel()==1){
                                       money= (double)ticket * IConstants.CONVERT_RATE_1;
                                }
                                if (user.getLevel()==2){
                                       money= (double)ticket * IConstants.CONVERT_RATE_2;
                                }
                                if (user.getLevel()==3){
                                       money= (double)ticket * IConstants.CONVERT_RATE_3;
                                }
                                String s = decimalFormat.format(money);
                                String[] split = s.split("\\.");
                                String m=split[0]+split[1];
                                Integer moneys = Integer.valueOf(m);
                                user.setMoney(user.getMoney() + moneys);
                                user.setObtainTicket(user.getObtainTicket()-ticket);
                                userService.updateUser(user);
                                payService.addUserConvert(userId, ticket, true);
                        } finally {
                                userService.unlockUser(userId);
                                payService.addUserConvert(userId, ticket, false);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户兑换记录
        @ResponseBody
        @RequestMapping(value = "/getConvertHistory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getConvertHistory(int userId) {
                logger.info("pay/getConvertHistory request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        JSONArray convertInfoList = new JSONArray();
                        List<UserConvert> userConverts = payService.getUserConvert(userId);
                        int allTicket = 0;
                        if (!Formula.isEmptyCollection(userConverts)) {
                                for (UserConvert userConvert : userConverts) {
                                        JSONObject rechargeInfo = new JSONObject();
                                        rechargeInfo.put("time", userConvert.getCreateTime());
                                        rechargeInfo.put("isSuccess", userConvert.getIsSuccess());
                                        rechargeInfo.put("ticket", userConvert.getTicketCount());
                                        convertInfoList.add(rechargeInfo);
                                        allTicket += userConvert.getTicketCount();
                                }
                        }
                        result.put("allTicket", allTicket);
                        result.put("convertInfoList", convertInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 用户充值记录
        @ResponseBody
        @RequestMapping(value = "/getRechargeHistory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getRechargeHistory(int userId) {
                logger.info("pay/getRechargeHistory request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        JSONArray rechargeInfoList = new JSONArray();
                        List<UserRecharge> userRecharges = payService.getUserRechargesByUserId(userId);
                        int allMoney = 0;
                        if (!Formula.isEmptyCollection(userRecharges)) {
                                for (UserRecharge userRecharge : userRecharges) {
                                        JSONObject rechargeInfo = new JSONObject();
                                        rechargeInfo.put("time", userRecharge.getCreateTime());
                                        rechargeInfo.put("isSuccess", userRecharge.getStatus() == IConstants.RECHARGE_STATUS_1);
                                        rechargeInfo.put("money", userRecharge.getMoneyCount());
                                        rechargeInfoList.add(rechargeInfo);
                                        allMoney += userRecharge.getMoneyCount();
                                }
                        }
                        result.put("allMoney", allMoney);
                        result.put("rechargeInfoList", rechargeInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户账单记录
        @ResponseBody
        @RequestMapping(value = "/getBillHistory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getBillHistory(int userId) {
                logger.info("pay/getBillHistory request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        JSONArray rechargeInfoList = new JSONArray();
                        List<UserRecharge> userRecharges = payService.getUserRechargesByUserId(userId);
                        int allRechargeMoney = 0;
                        if (!Formula.isEmptyCollection(userRecharges)) {
                                for (UserRecharge userRecharge : userRecharges) {
                                        JSONObject rechargeInfo = new JSONObject();
                                        if (userRecharge.getStatus()==1) {
                                                rechargeInfo.put("time", userRecharge.getCreateTime());
                                                rechargeInfo.put("isSuccess", userRecharge.getStatus() == IConstants.RECHARGE_STATUS_1);
                                                rechargeInfo.put("money", userRecharge.getMoneyCount());
                                                rechargeInfoList.add(rechargeInfo);
                                                allRechargeMoney += userRecharge.getMoneyCount();
                                        }
                                }
                        }
                        result.put("allRechargeMoney", allRechargeMoney);
                        result.put("rechargeInfoList", rechargeInfoList);
                        int allCashoutMoney = 0;
                        JSONArray cashoutInfoList = new JSONArray();
                        List<UserCashout> userCashouts = payService.getUserCashoutsByUserId(userId);
                        if (!Formula.isEmptyCollection(userCashouts)) {
                                for (UserCashout userCashout : userCashouts) {
                                        JSONObject cashoutInfo = new JSONObject();
                                        if (userCashout.getIsSuccess()==true){
                                                cashoutInfo.put("time", userCashout.getCreateTime());
                                                cashoutInfo.put("isSuccess", userCashout.getIsSuccess());
                                                cashoutInfo.put("money", userCashout.getMoneyCount());
                                                cashoutInfoList.add(cashoutInfo);
                                                allCashoutMoney += userCashout.getMoneyCount();
                                        }
                                }
                        }
                        result.put("allCashoutMoney", allCashoutMoney);
                        result.put("cashoutInfoList", cashoutInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 用户提现记录
        @ResponseBody
        @RequestMapping(value = "/getCashoutHistory", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getCashoutHistory(int userId) {
                logger.info("pay/getCashoutHistory request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        JSONArray cashoutInfoList = new JSONArray();
                        List<UserCashout> userCashouts = payService.getUserCashoutsByUserId(userId);
                        int allMoney = 0;
                        if (!Formula.isEmptyCollection(userCashouts)) {
                                for (UserCashout userCashout : userCashouts) {
                                        JSONObject cashoutInfo = new JSONObject();
                                        cashoutInfo.put("time", userCashout.getCreateTime());
                                        cashoutInfo.put("isSuccess", userCashout.getIsSuccess());
                                        cashoutInfo.put("money", userCashout.getMoneyCount());
                                        cashoutInfoList.add(cashoutInfo);
                                        allMoney += userCashout.getMoneyCount();
                                }
                        }
                        result.put("allMoney", allMoney);
                        result.put("cashoutInfoList", cashoutInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }


}
