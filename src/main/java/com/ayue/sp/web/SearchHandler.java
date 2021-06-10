package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

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
import com.ayue.sp.db.po.Answer;
import com.ayue.sp.db.po.Question;
import com.ayue.sp.db.po.User;
import com.ayue.sp.db.po.UserAttention;
import com.ayue.sp.service.QAService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月28日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/search")
public class SearchHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private QAService qaService;
        @Autowired
        private OnlineUserTool onlineUserTool;

        // 搜索用户
        @ResponseBody
        @RequestMapping(value = "/user", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchUser(String searchContent2, int page2, int userId) {
                logger.info("search/user request searchContent2:" + searchContent2 + " ;page2:" + page2 + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        if (Formula.isEmptyString(searchContent2)) {
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        List<User> users = userService.searchUsers(searchContent2, page2);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                if (user.getId() == userId) {
                                        continue;
                                }
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("userId", user.getId());
                                userInfo.put("avatar", user.getAvatar());
                                userInfo.put("level", user.getLevel());
                                userInfo.put("name", user.getName());
                                userInfo.put("labels", user.getLabels());
                                userInfo.put("isAttented", userAttentionMap.get(user.getId()) == null ? false : true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
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

        // 获取问题
        @ResponseBody
        @RequestMapping(value = "/qa", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchQA(String searchContent1, int page1) {
                logger.info("search/qa request searchContent1:" + searchContent1 + " ;page1:" + page1);
                JSONObject result = new JSONObject();
                try {
                        if (Formula.isEmptyString(searchContent1)) {
                                return result.toJSONString();
                        }
                        List<Question> questions = qaService.searchQuestions(searchContent1, page1);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> answers = qaService.getMaxClickAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> answerMap = Formula.list2map(answers, a -> a.getQuestionId());
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray qaInfoList = new JSONArray();
                        for (Question question : questions) {
                                Answer answer = answerMap.get(question.getId());
                                if (answer == null) {
                                        continue;
                                }
                                User user = userMap.get(answer.getUserId());
                                JSONObject qaInfo = new JSONObject();
                                qaInfo.put("questionTitle", question.getSummary());
                                qaInfo.put("questiongId", question.getId());
                                qaInfo.put("userId", user.getId());
                                qaInfo.put("userAvatar", user.getAvatar());
                                qaInfo.put("userName", user.getName());
                                qaInfo.put("userLevel", user.getLevel());
                                qaInfo.put("userLables", user.getLabels());
                                qaInfo.put("answerId", answer.getId());
                                qaInfo.put("answerContent", answer.getContent());
                                qaInfo.put("answerPictureUrl", answer.getPictureUrl());
                                qaInfo.put("answerAgreeCount", answer.getAgreeCount());
                                qaInfo.put("answerCommentCount", answer.getCommentCount());
                                qaInfo.put("answerCityAdress", answer.getCityAddress());
                                qaInfo.put("answerCreateTime", answer.getCreateTime());
                                qaInfo.put("ticket", question.getRewardTicket());
                                qaInfoList.add(qaInfo);
                        }
                        result.put("qaInfoList", qaInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
}
