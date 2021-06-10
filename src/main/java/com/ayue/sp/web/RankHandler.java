package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.ayue.sp.tools.online.OnlineUser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.db.po.Question;
import com.ayue.sp.db.po.User;
import com.ayue.sp.db.po.UserAttention;
import com.ayue.sp.service.QAService;
import com.ayue.sp.service.RankService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年9月2日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/rank")
public class RankHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private RankService rankService;
        @Autowired
        private QAService qaService;
        @Autowired
        private OnlineUserTool onlineUserTool;

        // 获取全排行榜
        @ResponseBody
        @RequestMapping(value = "/getAllRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getAllRank(int page, int userId) {
                logger.info("rank/getAllRank request page:" + page + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = rankService.getAllRankUserIds(page);
                        List<User> users = userService.getUsers(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        List<Question> questions = qaService.getMaxReadQuestionByUserIds(userIds);
                        Map<Integer, Question> questionMap = Formula.list2map(questions, q -> q.getUserId());
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("rank", rankService.getUserAllRank(user.getId()));
                                userInfo.put("userId", user.getId());
                                userInfo.put("userName", user.getName());
                                userInfo.put("userAvatar", user.getAvatar());
                                userInfo.put("userProfile", user.getProfile());
                                userInfo.put("userTicket", user.getAllTicket());
                                Question question = questionMap.get(user.getId());
                                if (question!=null) {
                                        userInfo.put("questionId", question.getId());
                                        userInfo.put("qustionTitle", question.getSummary());
                                }
                                UserAttention userAttention = userAttentionMap.get(user.getId());
                                userInfo.put("isAttention", userAttention == null ? false : true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                userInfoList.add(userInfo);
                        }
                        result.put("userInfoList", userInfoList);
                        result.put("allRankCount", userService.getUsers(rankService.getAllRankCount()).size());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取周排行榜
        @ResponseBody
        @RequestMapping(value = "/getWeekRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getWeekRank(int page, int userId) {
                logger.info("rank/getWeekRank request page:" + page + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = rankService.getWeekRankUserIds(page);
                        List<User> users = userService.getUsers(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        List<Question> questions = qaService.getMaxReadQuestionByUserIds(userIds);
                        Map<Integer, Question> questionMap = Formula.list2map(questions, q -> q.getUserId());
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("rank", rankService.getUserWeekRank(user.getId()));
                                userInfo.put("userId", user.getId());
                                userInfo.put("userName", user.getName());
                                userInfo.put("userAvatar", user.getAvatar());
                                userInfo.put("userProfile", user.getProfile());
                                userInfo.put("userTicket", user.getWeekTicket());
                                userInfo.put("userAllTicket", user.getAllTicket());
                                Question question = questionMap.get(user.getId());
                                if (question!=null) {
                                        userInfo.put("questionId", question.getId());
                                        userInfo.put("qustionTitle", question.getSummary());
                                }
                                UserAttention userAttention = userAttentionMap.get(user.getId());
                                userInfo.put("isAttention", userAttention == null ? false : true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                userInfoList.add(userInfo);
                        }
                        result.put("userInfoList", userInfoList);
                        result.put("weekRankCount", userService.getUsers(rankService.getWeekRankCount()).size());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取日排行榜
        @ResponseBody
        @RequestMapping(value = "/getDayRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getDayRank(int page, int userId) {
                logger.info("rank/getDayRank request page:" + page + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = rankService.getDayRankUserIds(page);
                        List<User> users = userService.getUsers(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        List<Question> questions = qaService.getMaxReadQuestionByUserIds(userIds);
                        Map<Integer, Question> questionMap = Formula.list2map(questions, q -> q.getUserId());
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("rank", rankService.getUserDayRank(user.getId()));
                                userInfo.put("userId", user.getId());
                                userInfo.put("userName", user.getName());
                                userInfo.put("userAvatar", user.getAvatar());
                                userInfo.put("userProfile", user.getProfile());
                                userInfo.put("userTicket", user.getDayTicket());
                                userInfo.put("userAllTicket", user.getAllTicket());
                                Question question = questionMap.get(user.getId());
                                if (question!=null) {
                                        userInfo.put("questionId", question.getId());
                                        userInfo.put("qustionTitle", question.getSummary());
                                }
                                UserAttention userAttention = userAttentionMap.get(user.getId());
                                userInfo.put("isAttention", userAttention == null ? false : true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                userInfoList.add(userInfo);
                        }
                        result.put("userInfoList", userInfoList);
                        result.put("dayRankCount", userService.getUsers(rankService.getDayRankCount()).size());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取历史排行榜
        @ResponseBody
        @RequestMapping(value = "/getHistoryRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getHistoryRank(int userId) {
                logger.info("rank/getDayRank request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        new ArrayList<>();
                        String time="";
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(user.getCreateTime());
                        long timeInMillis = calendar.getTimeInMillis();
                        long date = System.currentTimeMillis();
                        Long l=(date-timeInMillis)/(24 * 60 * 60 * 1000 * 7)+1;
                        for (int i = 0; i <l ; i++) {
                                if (timeInMillis<date){
                                        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
                                        if (i==0){
                                                time= calendar.getTimeInMillis()+"";
                                        }else {
                                                time += "," + calendar.getTimeInMillis();
                                        }
                                }
                        }
                        result.put("userCreateTime", time);
                        result.put("historyRank", user.getHistoryRank());
                        result.put("historyTicket", user.getHistoryTicket());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
}
