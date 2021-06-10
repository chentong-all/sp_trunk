package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.po.*;
import com.ayue.sp.service.QAService;
import com.ayue.sp.tools.pay.WxUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.db.cache.vo.MsgCVO;
import com.ayue.sp.service.ChatService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月24日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/chat")
public class ChatHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private ChatService chatService;
        @Autowired
        private OnlineUserTool onlineUserTool;
        @Autowired
        private UserService userService;
        @Autowired
        private QAService qaService;

        // 发送聊天信息
        @ResponseBody
        @RequestMapping(value = "/sendMsg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String sendMsg(int userId, String chatId, String textContent, byte[] voiceContent, String img) throws Exception {
                logger.info("chat/sendMsg  request userId:" + userId + " ;chatId:" + chatId + " ;textContent:" + textContent + " ;voiceContent:" + voiceContent);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = chatService.getChatMembers(chatId);
                        List<Integer> blackUserIds = chatService.getBlackList(userId);
                        User userInfo = userService.getUser(userId);
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        for (Integer id : userIds) {
                                if (blackUserIds.contains(id)){
                                        result.put("msg","你已拉黑用户");
                                        result.put("isSuccess", false);
                                        return result.toJSONString();
                                }
                                User user = userService.getUser(id);
                                if (user == null) {
                                        result.put("msg","用户不存在");
                                        result.put("isSuccess", false);
                                        return result.toJSONString();
                                }
                                if (userId!=id){
                                        OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(id);
                                        if (onlineTargetUser==null){
                                                WxUtil.wxMessage(user.getOpenid(),IConstants.MESSAGE_TYPE_CHAT,userInfo.getName());
                                        }
                                }
                        }
                        List<String> allUserChats = chatService.getUserChatIds(userId);
                        boolean isNewChat = true;
                        for (String s : allUserChats) {
                                if (s.equals(chatId)) {
                                        isNewChat = false;
                                        break;
                                }
                        }
                        for (int id : userIds) {
                                if (userId != id) {
                                        chatService.incrUnreadChat(id, chatId);
                                        userService.addTeacherRecord(userId,id,textContent);;
                                }
                                if (isNewChat) {
                                        chatService.addUserChat(id, chatId);
                                        chatService.addUserChat(userId, chatId);
                                }
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_8);
                        if (energyBeans.size()<=4){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_8);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        chatService.sendChat(userId, chatId, textContent, voiceContent, img);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取聊天信息
        @ResponseBody
        @RequestMapping(value = "/getMsg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getMsg(int userId, int targetUserId, int userCount, int targetCount) {
                logger.info("chat/getMsg request userId:" + userId + " ;targetUserId:" + targetUserId + ";userCount:"+";targetCount:" +targetCount);
                JSONObject result = new JSONObject();
                try {
                        List<TeacherRecord> teacherRecordById = userService.getTeacherRecordById(userId, targetUserId);
                        Map<Integer, TeacherRecord> teacherRecordInfo = Formula.list2map(teacherRecordById, t -> t.getTeacherId());
                        JSONArray msgInfoList = new JSONArray();
                        JSONArray userList = new JSONArray();
                        String chatId=userId+":"+targetUserId;
                        String chatIds=targetUserId+":"+userId;
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        if (onlineUser == null) {
                                result.put("msg", "您已掉线，请重新登录");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        } else {
                                result.put("msgtime", onlineUser.getLastRequestTime());
                                List<MsgCVO> msgCVos = chatService.getChatMsgCVo(chatId, onlineUser.getChatReadCursor(chatId));
                                if (msgCVos.size() > 0) {
                                        for (int i = 0; i < msgCVos.size() - userCount; i++) {
                                                JSONObject msgInfo = new JSONObject();
                                                msgInfo.put("sendUserId", msgCVos.get(i).getUserId());
                                                msgInfo.put("textContent", msgCVos.get(i).getTextContent());
                                                msgInfo.put("voiceContent", msgCVos.get(i).getVoiceContent());
                                                msgInfo.put("img", msgCVos.get(i).getImg());
                                                msgInfo.put("sendTime", msgCVos.get(i).getSendTime());
                                                TeacherRecord teacherRecord = teacherRecordInfo.get(msgCVos.get(i).getUserId());
                                                if (teacherRecord!=null){
                                                        if (teacherRecord.getIsChoose()==0){
                                                                msgInfo.put("userCreateTime",teacherRecord.getUpdateTime());
                                                                msgInfo.put("isFinally",false);
                                                        }else {
                                                                msgInfo.put("teacherCreateTime",teacherRecord.getUpdateTime());
                                                                long time = teacherRecord.getUpdateTime().getTime();
                                                                int day=(int)(System.currentTimeMillis()-time)/86400000;
                                                                if (day>teacherRecord.getDay()){
                                                                        msgInfo.put("isFinally",true);
                                                                }
                                                        }
                                                        msgInfo.put("status",teacherRecord.getIsAccept());
                                                        msgInfo.put("comment",teacherRecord.getComment());
                                                        msgInfo.put("isTeacher",teacherRecord.getTeacherId()==msgCVos.get(i).getUserId()?true:false);
                                                        msgInfo.put("ticket",teacherRecord.getTicket());
                                                        msgInfo.put("day",teacherRecord.getDay());
                                                        msgInfo.put("isChoose",teacherRecord.getIsChoose()==0?false:true);
                                                        msgInfo.put("sex",teacherRecord.getTeacherSex());
                                                        msgInfo.put("age",teacherRecord.getTeacherAge());
                                                        msgInfo.put("content",teacherRecord.getTeacherContent());
                                                }
                                                msgInfoList.add(msgInfo);
                                        }
                                        result.put("msgInfoList", msgInfoList);
                                }

                        }
                        List<MsgCVO> msgCVoss = chatService.getChatMsgCVo(chatIds, onlineUser.getChatReadCursor(chatIds));
                        if (msgCVoss.size() > 0) {
                                for (int i = 0; i < msgCVoss.size() - targetCount; i++) {
                                        JSONObject msgInfos = new JSONObject();
                                        msgInfos.put("sendUserId", msgCVoss.get(i).getUserId());
                                        msgInfos.put("textContent", msgCVoss.get(i).getTextContent());
                                        msgInfos.put("img", msgCVoss.get(i).getImg());
                                        msgInfos.put("voiceContent", msgCVoss.get(i).getVoiceContent());
                                        msgInfos.put("sendTime", msgCVoss.get(i).getSendTime());
                                        TeacherRecord teacherRecord = teacherRecordInfo.get(msgCVoss.get(i).getUserId());
                                        if (teacherRecord!=null){
                                                if (teacherRecord.getIsChoose()==0){
                                                        msgInfos.put("userCreateTime",teacherRecord.getUpdateTime());
                                                        msgInfos.put("isFinally",false);
                                                }else {
                                                        msgInfos.put("teacherCreateTime",teacherRecord.getUpdateTime());
                                                        long time = teacherRecord.getUpdateTime().getTime();
                                                        int day=(int)(System.currentTimeMillis()-time)/86400000;
                                                        if (day>teacherRecord.getDay()){
                                                                msgInfos.put("isFinally",true);
                                                        }
                                                }
                                                msgInfos.put("status",teacherRecord.getIsAccept());
                                                msgInfos.put("comment",teacherRecord.getComment());
                                                msgInfos.put("isTeacher",teacherRecord.getTeacherId()==msgCVoss.get(i).getUserId()?true:false);
                                                msgInfos.put("ticket",teacherRecord.getTicket());
                                                msgInfos.put("day",teacherRecord.getDay());
                                                msgInfos.put("isChoose",teacherRecord.getIsChoose()==0?false:true);
                                                msgInfos.put("sex",teacherRecord.getTeacherSex());
                                                msgInfos.put("age",teacherRecord.getTeacherAge());
                                                msgInfos.put("content",teacherRecord.getTeacherContent());
                                        }
                                        userList.add(msgInfos);
                                }
                                result.put("userList", userList);
                        }
                        OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(targetUserId);
                        if (onlineTargetUser == null) {
                                result.put("msg", "对方不在线");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }

                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 将未读聊天改为已读
        @ResponseBody
        @RequestMapping(value = "/readChat", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String readChat(int userId, int targetUserId) {
                logger.info("chat/readChat request userId:" + userId + " ;targetUserId:" + targetUserId);
                JSONObject result = new JSONObject();
                try {
                        String chatIds=targetUserId+":"+userId;
                        chatService.clearUserUnreadChat(userId, chatIds);
                        result.put("isSuccess",true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 在线问他
        @ResponseBody
        @RequestMapping(value = "/getUserMsg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserMsg(int userId) {
                logger.info("chat/getUserMsg request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("msg","用户不存在");
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        String labels = user.getLabels();
                        List<Integer> l=userService.getUserLables(labels);
                        l.remove(user.getId());
                        String splits="";
                        String splitss="";
                        String chatId="";
                        List<Integer> id=null;
                        Random random = new Random();
                        if (l.size() == 0) {
                                String[] split = labels.split(",");
                                for (int i = 0; i < split.length - 1; i++) {
                                        splits += splits == "" ? split[i] : "," + split[i];
                                }
                                id = userService.getUserLables(splits);
                                if (id.size() == 0) {
                                        for (int i = 0; i < split.length - 2; i++) {
                                                splitss += splitss == "" ? split[i] : "," + split[i];
                                        }
                                        id = userService.getUserLables(splitss);
                                }
                                if (id.size() != 0) {
                                        int randomId = random.nextInt(id.size());
                                        Integer integer = id.get(randomId);
                                        chatId = userId + ":" + integer;
                                }else {
                                        result.put("isSuccess",false);
                                        result.put("msg","你的问题过于独特");
                                        return result.toJSONString();
                                }
                        } else {
                                int randomId = random.nextInt(l.size());
                                Integer integer = l.get(randomId);
                                chatId = userId + ":" + integer;
                        }
                        result.put("isSuccess",true);
                        result.put("userId",userId);
                        result.put("chatId",chatId);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取聊天列表
        @ResponseBody
        @RequestMapping(value = "/getChatList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getChatList(int userId) {
                logger.info("chat/getChatList request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(chatService.getAllChatUserList(userId));
                        userIds.add(userId);
                        HashSet<Integer> integers = new HashSet<>(userIds);
                        userIds.removeAll(integers);
                        userIds.addAll(integers);
                        List<TeacherRecord> teacherRecordList = new ArrayList<>();
                        List<TeacherRecord> teacherRecordByList = userService.getTeacherRecord(userId, userIds);
                        Map<Integer, TeacherRecord> teacherRecordMapList ;
                        teacherRecordMapList = Formula.list2map(teacherRecordByList, t -> t.getTeacherId());
                        teacherRecordMapList = Formula.list2map(teacherRecordByList, t -> t.getUserId());

                        List<TeacherRecord> teacherRecord = userService.getTeacherRecordByIds(userIds, userId);
                        teacherRecordList.addAll(teacherRecord);
                        Map<Integer, TeacherRecord> teacherRecordMap = Formula.list2map(teacherRecordList, t -> t.getTeacherId());
                        Map<Integer, User> userMap = Formula.list2map(userService.getUsers(userIds), u -> u.getId());
                        User user = userMap.get(userId);
                        if (user == null) {
                                return result.toJSONString();
                        }
                        List<Integer> blackUserIds = chatService.getBlackList(userId);
                        JSONArray chatInfoList = new JSONArray();
                        int countChat=0;
                        int countTeacher=0;
                        for (int memberId : userIds) {
                            if (userId == memberId) {
                                continue;
                            }
                            if (blackUserIds.contains(memberId)) {
                                continue;
                            }
                            User member = userMap.get(memberId);
                            if (member == null) {
                                continue;
                            }
                            String chatIds = chatService.getChatId(userId, memberId);
                            String chatId = chatService.getChatId(memberId, userId);
                            MsgCVO msgCVo = chatService.getChatLastMsgCVo(chatIds);
                            MsgCVO msgCVos = chatService.getChatLastMsgCVo(chatId);
                            if (msgCVo!=null && msgCVos==null) {
                                JSONObject chatInfo = new JSONObject();
                                chatInfo.put("targetUserId", member.getId());
                                chatInfo.put("targetUserAvatar", member.getAvatar());
                                chatInfo.put("targetUserName", member.getName());
                                chatInfo.put("targetUserLevel", member.getLevel());
                                chatInfo.put("isTeacher", member.getIdentity()==0 ? false:true);
                                chatInfo.put("msgContent", msgCVo.getTextContent() == null ? msgCVo.getVoiceContent() : msgCVo.getTextContent());
                                chatInfo.put("sendTime", msgCVo.getSendTime());
                                chatInfo.put("msgImg", msgCVo.getImg());
                                chatInfo.put("isConsulting", teacherRecordMapList.get(memberId)==null ? false:true);
                                chatInfo.put("isTeacherRecord", teacherRecordMap.get(memberId)==null?false:true);
                                chatInfoList.add(chatInfo);
                            }
                            if(msgCVos!=null){
                                    JSONObject chatInfo = new JSONObject();
                                    chatInfo.put("targetUserId", member.getId());
                                    chatInfo.put("targetUserAvatar", member.getAvatar());
                                    chatInfo.put("targetUserName", member.getName());
                                    chatInfo.put("targetUserLevel", member.getLevel());
                                    chatInfo.put("isTeacher", member.getIdentity()==0 ? false:true);
                                    chatInfo.put("msgContent", msgCVos.getTextContent() == null ? msgCVos.getVoiceContent() : msgCVos.getTextContent());
                                    chatInfo.put("sendTime", msgCVos.getSendTime());
                                    chatInfo.put("msgImg", msgCVos.getImg());
                                    chatInfo.put("isConsulting", teacherRecordMapList.get(memberId)==null ? false:true);
                                    chatInfo.put("unreadCount",chatService.getUnreadChatCount(userId, chatId));
                                    //chatInfo.put("unreadCount",chatService.getUnreadChatCount(memberId, chatId));
                                    chatInfo.put("isTeacherRecord", teacherRecordMap.get(memberId)==null ? false:true);
                                chatInfoList.add(chatInfo);
                                if (teacherRecordMap.get(memberId)==null){
                                        countChat+=chatService.getUnreadChatCount(userId, chatId);
                                }else {
                                        countTeacher+=chatService.getUnreadChatCount(userId, chatId);

                                }
                            }
                        }
                        result.put("chatInfoList", chatInfoList);
                        result.put("teacherRecordCount", countTeacher);
                        result.put("chatCount", countChat);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取消息列表
        @ResponseBody
        @RequestMapping(value = "/getUserNews", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserNews(int userId, int page) {
                logger.info("chat/getUserNews  request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        JSONArray userNewsCount = new JSONArray();
                        List<UserNews> userNew = chatService.getUserNew(userId);
                        List<Byte> bytes = Formula.listDistinct(userNew, u -> u.getType());
                        for (Byte type:bytes) {
                                JSONObject userNewInfo = new JSONObject();
                                userNewInfo.put("type",type);
                                userNewInfo.put("all",chatService.getUserNew(userId,type,false).size());
                                userNewInfo.put("num",chatService.getUserNew(userId,type,true).size());
                                userNewsCount.add(userNewInfo);
                        }
                        result.put("userNewsCount",userNewsCount);
                        result.put("userNewCount", chatService.getUserNew(userId).size());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 获取类型消息
        @ResponseBody
        @RequestMapping(value = "/getUserNewsByType", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserNewsType(int userId, int page, byte type) {
                logger.info("chat/getUserNewsType  request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        List<UserNews> userNewsList = chatService.getUserNews(userId, page, type);
                        JSONArray userNewInfoList = new JSONArray();
                        for (UserNews userNews : userNewsList) {
                                JSONObject userNewInfo = new JSONObject();
                                userNewInfo.put("newsId", userNews.getId());
                                userNewInfo.put("newsType", userNews.getType());
                                userNewInfo.put("newsTitle", userNews.getTitle());
                                userNewInfo.put("newsContent", userNews.getContent());
                                userNewInfo.put("newsParam", userNews.getParam());
                                userNewInfo.put("newsIsReaded", userNews.getIsRead());
                                userNewInfo.put("newsCreateTime", userNews.getCreateTime());
                                userNewInfoList.add(userNewInfo);
                        }
                        result.put("userNewInfoList", userNewInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        //将未读改为已读
        @ResponseBody
        @RequestMapping(value = "/updateChatStatus", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String updateChatStatus(Integer[] chatId){
            logger.info("chat/updateChatStatus request chatId:" + chatId);
            JSONObject result = new JSONObject();
            try {
                    List<UserNews> userNews=chatService.getChat(chatId);
                    if (userNews.size()==0){
                            result.put("isSuccess", false);
                            result.put("msg", "没有未读消息");
                            return result.toJSONString();
                    }
                    chatService.updateChatStatus(chatId);
                    result.put("isSuccess", true);
                    return result.toJSONString();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw, true));
                logger.error(sw.toString());
            }
            return result.toJSONString();
        }
        //删除已读消息
        @ResponseBody
        @RequestMapping(value = "/delReadChat", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String delReadChat(Integer[] chatId){
                logger.info("chat/delReadChat request chatId:" + chatId);
                JSONObject result = new JSONObject();
                try {
                        List<UserNews> userNews=chatService.getChat(chatId);
                        if (userNews.size()==0){
                                result.put("isSuccess", false);
                                result.put("msg", "没有消息");
                                return result.toJSONString();
                        }
                        chatService.delReadChat(chatId);
                        result.put("isSuccess", true);
                        return result.toJSONString();
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取消息数量
        @ResponseBody
        @RequestMapping(value = "/getUserNewsCount", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserNewsCount(int userId) {
                logger.info("chat/getUserNewsCount request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                 result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        int newsCount = chatService.countUnReadNews(userId);
                        Map<String, Integer> unreadChatCountMap = chatService.getUnreadChatCount(userId);
                        int unreadChatCount = 0;
                        if (!Formula.isEmptyMap(unreadChatCountMap)) {
                                for (int unreadCountChat : unreadChatCountMap.values()) {
                                        unreadChatCount += unreadCountChat;
                                }
                        }
                        result.put("newsCount", newsCount);
                        result.put("unreadChatCount", unreadChatCount);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 拉黑对方
        @ResponseBody
        @RequestMapping(value = "/addChatBlackList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String addChatBlackList(int userId, int targetUserId) {
                logger.info("chat/addChatBlackList request userId:" + userId + " ;targetUserId：" + targetUserId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        User targetUser = userService.getUser(targetUserId);
                        if (targetUser == null) {
                                result.put("isSuccess", false);
                                result.put("msg","对方不存在");
                                return result.toJSONString();
                        }
                        chatService.addUserBlack(userId, targetUserId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 取消拉黑对方
        @ResponseBody
        @RequestMapping(value = "/delChatBlackList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String delChatBlackList(int userId, int targetUserId) {
                logger.info("chat/delChatBlackList request userId:" + userId + " ;targetUserId：" + targetUserId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        User targetUser = userService.getUser(targetUserId);
                        if (targetUser == null) {
                                result.put("isSuccess", false);
                                result.put("msg","对方不存在");
                                return result.toJSONString();
                        }
                        chatService.removeBlackList(userId, targetUserId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 聊天推荐
        @ResponseBody
        @RequestMapping(value = "/recommendChat", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String recommendChat(int userId) {
                logger.info("chat/recommendChat request userId:" + userId );
                JSONObject result = new JSONObject();
                try {
                        List<User> users = new LinkedList<User>();
                        List<User> userList = new LinkedList<User>();
                        User user = userService.getUser(userId);
                        List<User> allUser = userService.getAllUser();
                        for (int i = 0; i <allUser.size() ; i++) {
                                if (allUser.get(i).getId() == userId){
                                        allUser.remove(i);
                                }
                        }
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        for (int i = 0; i < 5; i++) {
                                Random random = new Random();
                                int j = random.nextInt(allUser.size());
                                User user1 = allUser.get(j);
                                userList.add(user1);
                                allUser.remove(j);
                        }
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        JSONArray userInfoList = new JSONArray();
                        for (User u : userList) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("userId", u.getId());
                                userInfo.put("userName", u.getName());
                                userInfo.put("userAvatar", u.getAvatar());
                                userInfo.put("userLevel", u.getLevel());
                                userInfo.put("userLabels", u.getLabels());
                                userInfo.put("isTeacher", u.getIdentity()==0 ? false:true);
                                UserAttention userAttention = userAttentionMap.get(u.getId());
                                userInfo.put("isAttention", userAttention == null ? false : true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(u.getId()) == null ? false : true);
                                userInfoList.add(userInfo);
                        }
                        result.put("userInfoList", userInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
}
