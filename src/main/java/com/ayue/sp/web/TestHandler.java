package com.ayue.sp.web;

import com.alibaba.fastjson.JSONArray;
import com.ayue.sp.core.Formula;
import com.ayue.sp.db.cache.vo.MsgCVO;
import com.ayue.sp.db.po.EnergyBean;
import com.ayue.sp.db.po.EnergyBeanExample;
import com.ayue.sp.db.po.UserExample;
import com.ayue.sp.tools.online.OnlineUser;
import com.github.pagehelper.PageInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.po.User;
import com.ayue.sp.service.RankService;
import com.ayue.sp.service.UserService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * 2020年9月3日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/test")
public class TestHandler {

        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private RankService rankService;
        @Autowired
        private RankZSetCDao rankZSetCDao;

        // 添加玩家票数
        @ResponseBody
        @RequestMapping(value = "/addTicket", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String addTicket(int userId, int ticketCount) {
                logger.info("user/addTicket request userId:" + userId + "ticketCount:" + ticketCount);
                User user = userService.getUser(userId);
                userService.addUserTicket(user, ticketCount);
                JSONObject result = new JSONObject();
                result.put("userId", user.getId());
                return result.toJSONString();
        }
        @ResponseBody
        @RequestMapping(value = "/history", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String history() {
                List<User> users = userService.getAllUserByTicket();
                int rank=users.size();
                for (User user : users) {
                        if (user.getHistoryRank() == null) {
                                user.setHistoryRank(rank + " ");
                        } else {
                                user.setHistoryRank(user.getHistoryRank() + "," + rank);
                        }
                        if (user.getHistoryTicket() == null) {
                                user.setHistoryTicket(user.getWeekTicket() + " ");
                        } else {
                                user.setHistoryTicket(user.getHistoryTicket() + "," + user.getWeekTicket());
                        }
                        rank--;
                }
                userService.batchUpdateHistory(users);
                return "";
        }
        @ResponseBody
        @RequestMapping(value = "/rank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public void rank() {
                userService.resetDayTicket();
                rankZSetCDao.resetDayRank();
                userService.updateRankEnergy();
        }
        /*// 获取聊天信息
        @ResponseBody
        @RequestMapping(value = "/getMsg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getMsg(int userId, String chatId) {
                logger.info("chat/getMsg request userId:" + userId + " ;chatId:" + chatId);
                JSONObject result = new JSONObject();
                try {
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                result.put("msg","用户不在线");
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        List<MsgCVO> msgCVos = chatService.getChatMsgCVo(chatId, onlineUser.getChatReadCursor(chatId));
                        onlineUser.setChatReadCursor(chatId, msgCVos.get(0).getSendTime());
                        *//*if (Formula.isEmptyCollection(msgCVos)) {
                                result.put("msg","没有聊天信息");
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }*//*
                        JSONArray msgInfoList = new JSONArray();
                        for (MsgCVO msgCVo : msgCVos) {
                                JSONObject msgInfo = new JSONObject();
                                msgInfo.put("sendUserId", msgCVo.getUserId());
                                msgInfo.put("textContent", msgCVo.getTextContent());
                                msgInfo.put("voiceContent", msgCVo.getVoiceContent());
                                msgInfo.put("sendTime", msgCVo.getSendTime());
                                msgInfoList.add(msgInfo);
                        }
                        chatService.clearUserUnreadChat(userId, chatId);
                        result.put("msgInfoList", msgInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }*/
        //
        // // 重置玩家周票数
        // @ResponseBody
        // @RequestMapping(value = "/resetWeekTicket", method =
        // RequestMethod.POST, produces = "application/json;charset=UTF-8")
        // public String resetWeekTicket() {
        // logger.info("user/resetWeekTicket request ");
        // this.updateUserHistoryRank();
        // userService.resetWeekTicket();
        // rankZSetCDao.resetWeekRank();
        // JSONObject result = new JSONObject();
        // result.put("isSuccess", true);
        // return result.toJSONString();
        // }
        //
        // // 重置玩家日票数
        // @ResponseBody
        // @RequestMapping(value = "/resetDayTicket", method =
        // RequestMethod.POST, produces = "application/json;charset=UTF-8")
        // public String resetDayTicket() {
        // logger.info("user/resetDayTicket request ");
        // userService.resetDayTicket();
        // rankZSetCDao.resetDayRank();
        // JSONObject result = new JSONObject();
        // result.put("isSuccess", true);
        // return result.toJSONString();
        // }
        //
        // // 获取玩家排行
        // @ResponseBody
        // @RequestMapping(value = "/getUserRank", method = RequestMethod.POST,
        // produces = "application/json;charset=UTF-8")
        // public String getUserRank(int userId) {
        // logger.info("user/getUserRank request: usrId：" + userId);
        // JSONObject result = new JSONObject();
        // result.put("rank", rankService.getUserAllRank(userId));
        // return result.toJSONString();
        // }
        //
        // // 更新玩家历史排行
        // private void updateUserHistoryRank() {
        // int maxId = userService.getMaxUserId() + 1000;
        // int index = maxId / 10000 + 1;
        // for (int i = 0; i < index; i++) {
        // List<Integer> userIds = new LinkedList<Integer>();
        // for (int id = i * 10000; id < (i + 1) * 10000; id++) {
        // userIds.add(id);
        // }
        // List<User> users = userService.getUsers(userIds);
        // for (User user : users) {
        // int rank = rankService.getUserWeekRank(user.getId());
        // if (user.getHistoryRank() == null) {
        // user.setHistoryRank(rank + "");
        // } else {
        // user.setHistoryRank(user.getHistoryRank() + "," + rank);
        // }
        // if (user.getHistoryTicket() == null) {
        // user.setHistoryTicket(user.getWeekTicket() + "");
        // } else {
        // user.setHistoryTicket(user.getHistoryTicket() + "," +
        // user.getWeekTicket());
        // }
        // }
        // userService.batchUpdateHistory(users);
        // }
        // }

}
