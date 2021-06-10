package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.po.*;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.pay.WxUtil;
import com.ayue.sp.tools.task.TimerTaskHandler;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.service.ChatService;
import com.ayue.sp.service.QAService;
import com.ayue.sp.service.RankService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月29日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/user")
public class UserHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private QAService qaService;
        @Autowired
        private ChatService chatService;
        @Autowired
        private OnlineUserTool onlineUserTool;
        @Autowired
        private RankService rankService;

        // 获取用户信息
        @ResponseBody
        @RequestMapping(value = "/getUserInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserInfo(int targetUserId, int userId) {
                logger.info("user/getUserInfo request targetUserId:" + targetUserId + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User targetUser = userService.getUser(targetUserId);
                        if (targetUser == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        Teacher teacher = userService.getTeacher(targetUserId);
                        if (teacher!=null) {
                                result.put("teacherTicket", teacher.getTicket());
                                result.put("dayTicket",teacher.getDayTicket());
                                result.put("day",teacher.getDay());
                        }else {
                                result.put("teacherTicket", 0);
                                result.put("dayTicket",0);
                                result.put("day",0);
                        }
                        UserAttention userAttention = userService.getUserAttention(userId, targetUserId);
                        result.put("id", targetUser.getId());
                        result.put("appId", targetUser.getOpenid());
                        result.put("professional", targetUser.getProfessional());
                        result.put("proficient", targetUser.getProficient());
                        result.put("gender", targetUser.getGender());
                        result.put("name", targetUser.getName());
                        result.put("avatar", targetUser.getAvatar());
                        result.put("notice", targetUser.getNotice());
                        result.put("profile", targetUser.getProfile());
                        result.put("labels", targetUser.getLabels());
                        result.put("historyRank", targetUser.getHistoryRank());
                        result.put("historyTicket", targetUser.getHistoryTicket());
                        result.put("day_ticket", targetUser.getDayTicket());
                        result.put("dayRank", rankService.getUserDayRank(targetUserId));
                        result.put("week_ticket", targetUser.getWeekTicket());
                        result.put("weekRank", targetUser.getWeekTicket()==0?0:rankService.getUserWeekRank(targetUserId));
                        result.put("all_ticket", targetUser.getAllTicket());
                        result.put("allRank", rankService.getUserAllRank(targetUserId));
                        result.put("ticket", targetUser.getTicket());
                        result.put("obtainTicket", targetUser.getObtainTicket());
                        result.put("money", targetUser.getMoney() * 0.01);
                        result.put("pay_count", targetUser.getPayCount());
                        result.put("like_count", targetUser.getLikeCount());
                        result.put("forward_count", targetUser.getForwardCount());
                        result.put("isSystem", targetUser.getIsSystem());
                        result.put("cityAddress", targetUser.getCityAddress());
                        result.put("create_time", targetUser.getCreateTime());
                        result.put("attestationUrl", targetUser.getAttestationUrl());
                        result.put("level", targetUser.getLevel());
                        result.put("city", targetUser.getCity());
                        result.put("unit", targetUser.getUnit());
                        result.put("isTeacher", targetUser.getIdentity()== 0 ? false:true);
                        result.put("isAttention", userAttention == null ? false : true);
                        result.put("isOnline", onlineUserTool.getOnlineUser(targetUserId) == null ? false : true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue);
        }

        // 获取用户问题
        @ResponseBody
        @RequestMapping(value = "/getUserQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserQuestion(int userId, int page) {
                logger.info("user/getUserQuestion request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getQuestionsByUserId(userId, page);
                        JSONArray userQuestionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject userQuestionInfo = new JSONObject();
                                userQuestionInfo.put("id", question.getId());
                                userQuestionInfo.put("title", question.getSummary());
                                userQuestionInfo.put("readCount", question.getReadCount());
                                userQuestionInfo.put("createTime", question.getCreateTime());
                                userQuestionInfoList.add(userQuestionInfo);
                        }
                        result.put("userQuestionInfoList", userQuestionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取用户快问
        @ResponseBody
        @RequestMapping(value = "/getUserFastQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserFastQuestion(int userId, int page) {
                logger.info("user/getUserFastQuestion request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getFastQuestionsByUserId(userId, page);
                        JSONArray userQuestionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject userQuestionInfo = new JSONObject();
                                userQuestionInfo.put("id", question.getId());
                                userQuestionInfo.put("title", question.getSummary());
                                userQuestionInfo.put("readCount", question.getReadCount());
                                userQuestionInfo.put("createTime", question.getCreateTime());
                                userQuestionInfo.put("pictureUrl", question.getPictureUrl());
                                userQuestionInfoList.add(userQuestionInfo);
                        }

                        result.put("userQuestionInfoList", userQuestionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户回答
        @ResponseBody
        @RequestMapping(value = "/getUserAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserAnswer(int userId, int page) {
                logger.info("user/getUserAnswer request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Answer> answers = qaService.getAnswersByUserId(userId, page);
                        JSONArray userAnswerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject userAnswerInfo = new JSONObject();
                                userAnswerInfo.put("id", answer.getId());
                                userAnswerInfo.put("questionId", answer.getQuestionId());
                                userAnswerInfo.put("content", answer.getContent());
                                userAnswerInfo.put("agreeCount", answer.getAgreeCount());
                                userAnswerInfo.put("commentCount", answer.getCommentCount());
                                userAnswerInfo.put("forwardCount", answer.getForwardCount());
                                userAnswerInfo.put("clickCount", answer.getClickCount());
                                userAnswerInfo.put("ticketCount", answer.getTicketCount());
                                userAnswerInfo.put("cityAddress", answer.getCityAddress());
                                userAnswerInfo.put("createTime", answer.getCreateTime());
                                userAnswerInfo.put("adopt", answer.getAdopt()== 0 ? false:true);
                                userAnswerInfoList.add(userAnswerInfo);
                        }

                        result.put("userAnswerInfoList", userAnswerInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户评论
        @ResponseBody
        @RequestMapping(value = "/getUserComment", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserComment(int userId, int page) {
                logger.info("user/getUserComment request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Comment> comments = qaService.getCommentsByUserId(userId, page);
                        List<Integer> answerIds = Formula.listDistinct(comments, c -> c.getAnswerId());
                        Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                        JSONArray userCommentInfoList = new JSONArray();
                        for (Comment comment : comments) {
                                JSONObject userCommentInfo = new JSONObject();
                                userCommentInfo.put("questionId",answerMap.get(comment.getAnswerId()).getQuestionId());
                                userCommentInfo.put("id", comment.getId());
                                userCommentInfo.put("answerId", comment.getAnswerId());
                                userCommentInfo.put("content", comment.getContent());
                                userCommentInfo.put("agreeCount", comment.getAgreeCount());
                                userCommentInfo.put("replyCount", comment.getReplyCount());
                                userCommentInfo.put("createTime", comment.getCreateTime());
                                userCommentInfoList.add(userCommentInfo);
                        }

                        result.put("userCommentInfoList", userCommentInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户回复
        @ResponseBody
        @RequestMapping(value = "/getUserReply", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserReply(int userId, int page) {
                logger.info("user/getUserReply request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Reply> replys = qaService.getReplyByUserId(userId, page);
                        List<Integer> commentIds = Formula.listDistinct(replys, r -> r.getCommentId());
                        List<Comment> comments = qaService.getComments(commentIds);
                        Map<Integer, Comment> commentMap = qaService.getCommentMap(commentIds);
                        List<Integer> answerIds = Formula.listDistinct(comments, c -> c.getAnswerId());
                        Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                        JSONArray userReplyInfoList = new JSONArray();
                        for (Reply reply : replys) {
                                JSONObject userReplyInfo = new JSONObject();
                                Integer answerId = commentMap.get(reply.getCommentId()).getAnswerId();
                                userReplyInfo.put("questionId",answerMap.get(answerId).getQuestionId());
                                userReplyInfo.put("id", reply.getId());
                                userReplyInfo.put("content", reply.getContent());
                                userReplyInfo.put("agreeCount", reply.getAgreeCount());
                                userReplyInfo.put("commentId", reply.getCommentId());
                                userReplyInfo.put("createTime", reply.getCreateTime());
                                userReplyInfoList.add(userReplyInfo);
                        }
                        result.put("userReplyInfoList", userReplyInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        /*// 获取用户历史排行
        @ResponseBody
        @RequestMapping(value = "/getUserHistoryRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserHistoryRank(int userId) {
                logger.info("user/getUserHistoryRank request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        result.put("historyRank", user.getHistoryRank());
                        result.put("historyTicker", user.getHistoryTicket());
                        result.put("createTime", user.getCreateTime());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }*/

        // 举报用户
        @ResponseBody
        @RequestMapping(value = "/tipoffUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String tipoffUser(int userId, int targetUserId, byte type, String description, String pictureUrl) {
                logger.info("user/tipoffUser request userId:" + userId + " ;targetUserId：" + targetUserId + " ;type:" + type + " ;description:" + description + " ;pictureUrl:" + pictureUrl);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        User targetUser = userService.getUser(targetUserId);
                        if (targetUser == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        UserTipoffs userTipoffs = userService.getUserTipoff(targetUserId, userId ,type);
                        if (userTipoffs == null) {
                                userService.addUserTipoffs(targetUserId, userId, type, description, pictureUrl);
                        }else{
                                result.put("msg","已经举报用户");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 给用户投票
        @ResponseBody
        @RequestMapping(value = "/vote", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String vote(int userId, int targetUserId, int ticket, int answerId, int status) {
                logger.info("user/vote request userId:" + userId + " ;targetUserId：" + targetUserId + " ;ticket:" + ticket);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> lockUserIds = new LinkedList<Integer>();
                        User users = userService.getUser(userId);
                        if (users==null){
                                result.put("isSuccess",false);
                                result.put("msg","userId不存在");
                                return result.toJSONString();
                        }
                        User usersTarget = userService.getUser(targetUserId);
                        if (usersTarget==null){
                                result.put("isSuccess",false);
                                result.put("msg","targetUserId不存在");
                                return result.toJSONString();
                        }
                        if (userId==targetUserId){
                                result.put("isSuccess",false);
                                result.put("msg","不能给自己投票");
                                return result.toJSONString();
                        }
                        if (status==1) {
                                qaService.addAnswerCharge(answerId,userId);
                        }
                        lockUserIds.add(userId);
                        lockUserIds.add(targetUserId);
                        userService.lockUsers(lockUserIds);
                        try {
                                Map<Integer, User> userMap = userService.getUserMap(lockUserIds);
                                User user = userMap.get(userId);
                                if (user.getTicket() < ticket) {
                                        result.put("msg","没有票了");
                                        result.put("isSuccess", false);
                                        return result.toJSONString();
                                }
                                User targetUser = userMap.get(targetUserId);
                                if (answerId>0) {
                                        qaService.updateAnswerTicket(targetUserId, ticket, answerId);
                                }
                                userService.addUserTicket(targetUser, ticket);
                                userService.reduceUserTicket(user, ticket);
                                userService.addUserVote(userId, targetUserId, ticket, answerId);
                                chatService.addUserNews2(targetUserId, users.getName(),usersTarget.getName(),ticket,userId);
                        } finally {
                                userService.unlockUsers(lockUserIds);
                        }
                        OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(targetUserId);
                        if (onlineTargetUser==null){
                                WxUtil.wxMessage(usersTarget.getOpenid(), IConstants.MESSAGE_TYPE_TICKET,usersTarget.getName());
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_9);
                        qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_9);
                        result.put("isEnergyBeanSuccess", true);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 采纳回答
        @ResponseBody
        @RequestMapping(value = "/adopt", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String adopt(int userId, int targetUserId, int ticket, int answerId) {
                logger.info("user/adopt request userId:" + userId + " ;targetUserId：" + targetUserId + " ;ticket:" + ticket+" ;answerId:" + answerId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> lockUserIds = new LinkedList<Integer>();
                        User users = userService.getUser(userId);
                        if (users==null){
                                result.put("isSuccess",false);
                                result.put("msg","userId不存在");
                                return result.toJSONString();
                        }
                        User usersTarget = userService.getUser(targetUserId);
                        if (usersTarget==null){
                                result.put("isSuccess",false);
                                result.put("msg","targetUserId不存在");
                                return result.toJSONString();
                        }
                        if (userId==targetUserId){
                                result.put("isSuccess",false);
                                result.put("msg","自己不能采纳自己");
                                return result.toJSONString();
                        }
                        lockUserIds.add(userId);
                        lockUserIds.add(targetUserId);
                        userService.lockUsers(lockUserIds);
                        try {
                                Map<Integer, User> userMap = userService.getUserMap(lockUserIds);
                                Answer answer = qaService.getAnswer(answerId);
                                User targetUser = userMap.get(targetUserId);
                                Integer questionId = qaService.getAnswer(answerId).getQuestionId();
                                userService.addUserTicket(targetUser, ticket);
                                userService.addUserVote(userId, targetUserId, ticket, answerId);
                                if (ticket!=0) {
                                        chatService.addUserNews2(targetUserId, users.getName(), usersTarget.getName(), ticket, userId);
                                }
                                if (questionId!=null){
                                        qaService.updateQuestionTicket(questionId);
                                        qaService.updateAnswerAdopt(answerId,ticket);
                                        chatService.addUserNews1(targetUserId,answer.getContent(),answer.getQuestionId(),answer.getId(),users.getName(), usersTarget.getName(),userId );
                                }
                        } finally {
                                userService.unlockUsers(lockUserIds);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 关注用户
        @ResponseBody
        @RequestMapping(value = "/attentionUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String attentionUser(int userId, int targetUserId) {
                logger.info("user/attentionUser request userId:" + userId + " ;targetUserId:" + targetUserId);
                JSONObject result = new JSONObject();
                try {
                        if (userId == targetUserId) {
                                result.put("isSuccess", false);
                                result.put("msg", "不能关注自己");
                                return result.toJSONString();
                        }
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        User targetUser = userService.getUser(targetUserId);
                        if (targetUser == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        UserAttention userAttention = userService.getUserAttention(userId, targetUserId);
                        if (userAttention == null) {
                                userService.addUserAttention(userId, targetUserId);
                                chatService.addUserNews6(targetUserId, user.getName(),targetUser.getName(),userId);
                        }else{
                                result.put("msg", "用户已关注");
                                result.put("isSuccess", false);
                                userService.deleteByPrimaryKey(userId, targetUserId);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户关注用户
        @ResponseBody
        @RequestMapping(value = "/getAttentionUsers", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getAttentionUsers(int userId) {
                logger.info("user/getAttentionUsers request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<UserAttention> userAttentions = userService.getUserAttentions(userId);
                        JSONArray userAttentionInfoList = new JSONArray();
                        if (!Formula.isEmptyCollection(userAttentions)) {
                                List<Integer> userIds = Formula.listDistinct(userAttentions, u -> u.getAttentionUserId());
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                for (UserAttention userAttention : userAttentions) {
                                        JSONObject userAttentionInfo = new JSONObject();
                                        User user = userMap.get(userAttention.getAttentionUserId());
                                        userAttentionInfo.put("id", user.getId());
                                        userAttentionInfo.put("avatar", user.getAvatar());
                                        userAttentionInfo.put("name", user.getName());
                                        userAttentionInfo.put("level", user.getLevel());
                                        userAttentionInfo.put("labels", user.getLabels());
                                        userAttentionInfo.put("isAttention", true);
                                        userAttentionInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        userAttentionInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                        userAttentionInfoList.add(userAttentionInfo);
                                }
                        }
                        result.put("userAttentionInfoList", userAttentionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户粉丝
        @ResponseBody
        @RequestMapping(value = "/getUserFans", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserFans(int userId) {
                logger.info("user/getUserFans request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<UserAttention> userAttentions = userService.getUserFans(userId);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        JSONArray fansInfoList = new JSONArray();
                        if (!Formula.isEmptyCollection(userAttentions)) {
                                List<Integer> userIds = Formula.listDistinct(userAttentions, u -> u.getUserId());
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                for (UserAttention userAttention : userAttentions) {
                                        JSONObject fansInfo = new JSONObject();
                                        User user = userMap.get(userAttention.getUserId());
                                        fansInfo.put("id", user.getId());
                                        fansInfo.put("avatar", user.getAvatar());
                                        fansInfo.put("name", user.getName());
                                        fansInfo.put("level", user.getLevel());
                                        fansInfo.put("labels", user.getLabels());
                                        fansInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        fansInfo.put("isAttention", userAttentionMap.get(user.getId()) == null ? false : true);
                                        fansInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                        fansInfoList.add(fansInfo);
                                }
                        }

                        result.put("fansInfoList", fansInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户投票历史
        @ResponseBody
        @RequestMapping(value = "/getUserVotes", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserVotes(int userId, long startTime, long endTime) {
                logger.info("user/getUserVotes request userId：" + userId + ";startTime:" + new Date(startTime) + " ;endTime:" + new Date(endTime));
                JSONObject result = new JSONObject();
                try {
                        List<UserVote> userVotes = userService.getUserVote(userId, new Date(startTime), new Date(endTime));
                        JSONArray voteInfoList = new JSONArray();
                        result.put("dayVotedTicketCount", userService.getUser(userId).getDayVoteCount());
                        result.put("canUsedTicketCount", userService.getUser(userId).getTicket());
                        if (!Formula.isEmptyCollection(userVotes)) {
                                List<Integer> userIds = Formula.listDistinct(userVotes, u -> u.getTargetUserId());
                                userIds.add(userId);
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                List<Integer> answerIds = Formula.listDistinct(userVotes, u -> u.getAnswerId() == null ? -1 : u.getAnswerId());
                                Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                                for (UserVote userVote : userVotes) {
                                        User targetUser = userMap.get(userVote.getTargetUserId());
                                        Answer answer = answerMap.get(userVote.getAnswerId());
                                        JSONObject voteInfo = new JSONObject();
                                        voteInfo.put("userId", targetUser.getId());
                                        voteInfo.put("userName", targetUser.getName());
                                        if (userVote.getAnswerId() != null) {
                                                voteInfo.put("answerId", userVote.getAnswerId());
                                                voteInfo.put("answerContent", answer.getContent());
                                        }
                                        voteInfo.put("voteTime", userVote.getCreateTime());
                                        voteInfo.put("count", userVote.getCount());
                                        voteInfoList.add(voteInfo);
                                }
                        }
                        result.put("voteInfoList", voteInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户得票历史
        @ResponseBody
        @RequestMapping(value = "/getUserGainVotes", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserGainVotes(int userId, long startTime, long endTime) {
                logger.info("user/getUserGainVotes request userId：" + userId + ";startTime:" + new Date(startTime) + " ;endTime:" + new Date(endTime));
                JSONObject result = new JSONObject();
                try {
                        List<UserVote> userVotes = userService.getUserVoteByTargetUserId(userId, new Date(startTime), new Date(endTime));
                        JSONArray voteInfoList = new JSONArray();
                        if (!Formula.isEmptyCollection(userVotes)) {
                                List<Integer> userIds = Formula.listDistinct(userVotes, u -> u.getUserId());
                                userIds.add(userId);
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                User user = userMap.get(userId);
                                List<Integer> answerIds = Formula.listDistinct(userVotes, u -> u.getAnswerId() == null ? -1 : u.getAnswerId());
                                Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                                result.put("dayVotedTicketCount", user.getDayVoteCount());
                                result.put("canUsedTicketCount", user.getTicket());
                                for (UserVote userVote : userVotes) {
                                        User voteUser = userMap.get(userVote.getUserId());
                                        Answer answer = answerMap.get(userVote.getAnswerId());
                                        JSONObject voteInfo = new JSONObject();
                                        if (voteUser!=null){
                                                voteInfo.put("userId", voteUser.getId());
                                                voteInfo.put("userName", voteUser.getName());
                                        }else {
                                                voteInfo.put("userName", "抽奖获得");

                                        }
                                        if (userVote.getAnswerId() != null) {
                                                voteInfo.put("answerId", userVote.getAnswerId());
                                                voteInfo.put("answerContent", answer.getContent());
                                        }
                                        voteInfo.put("voteTime", userVote.getCreateTime());
                                        voteInfo.put("count", userVote.getCount());
                                        voteInfoList.add(voteInfo);
                                }
                        }
                        result.put("voteInfoList", voteInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 修改个人简介
        @ResponseBody
        @RequestMapping(value = "/changeUserInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String changeUserInfo(int userId, String avatarUrl, String name, String notice, byte gender, String city) {
                logger.info("user/changeUserInfo request userId：" + userId + " ;avatarUrl:" + avatarUrl + " ;name:" + name + " ;notice:" +notice+ " ;gender:" +gender+ " ;city:"+city);
                JSONObject result = new JSONObject();
                try {
                        userService.lockUser(userId);
                        try {
                                User user = userService.getUser(userId);
                                if (!Formula.isEmptyString(avatarUrl)) {
                                        user.setAvatar(avatarUrl);
                                }
                                if (!Formula.isEmptyString(name)) {
                                        user.setName(name);
                                }
                                if (!Formula.isEmptyString(notice)) {
                                        user.setNotice(notice);
                                }
                                if (gender==0){
                                    user.setGender(gender);
                                }else {
                                    user.setGender(gender);
                                }
                                if (!Formula.isEmptyString(city)) {
                                        user.setCity(city);
                                }
                                userService.updateUser(user);
                        } finally {
                                userService.unlockUser(userId);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 认证成为老师
        @ResponseBody
        @RequestMapping(value = "/certifiedTeacher", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String certifiedTeacher(int userId, String professional, String unit, String labels, String profile,String attestationUrl) {
                logger.info("user/certifiedTeacher request userId：" + userId + " ;professional:" + professional + " ;unit:" + unit + " ;labels:" +labels+ " ;profile:" +profile+ " ;attestationUrl:" +attestationUrl);
                JSONObject result = new JSONObject();
                try {
                        userService.lockUser(userId);
                        try {
                                User user = userService.getUser(userId);
                                if (!Formula.isEmptyString(professional)) {
                                        user.setProfessional(professional);
                                }
                                if (!Formula.isEmptyString(unit)) {
                                        user.setUnit(unit);
                                }
                                if (!Formula.isEmptyString(labels)) {
                                        user.setLabels(labels);
                                }
                                if (!Formula.isEmptyString(profile)) {
                                        user.setProfile(profile);
                                }
                                if (!Formula.isEmptyString(attestationUrl)) {
                                        user.setAttestationUrl(attestationUrl);
                                }
                                user.setTeacher(1);
                                userService.updateUser(user);
                        } finally {
                                userService.unlockUser(userId);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
    // 审核认证查询
    @ResponseBody
    @RequestMapping(value = "/getAuditTeacherInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String auditTeacherInfo(int page) {
        logger.info("user/getAuditTeacherInfo request page：" + page);
        JSONObject result = new JSONObject();
        try {
                List<User> userLists = userService.getUserLists(page);
                if (userLists.size()==0){
                        result.put("isSuccess",false);
                        result.put("msg","没有认证的老师");
                        return result.toJSONString();
                }
                JSONArray userInfoList = new JSONArray();
                for (User user:userLists) {
                        JSONObject userInfo = new JSONObject();
                        userInfo.put("userId",user.getId());
                        userInfo.put("userName",user.getName());
                        userInfo.put("createTime",user.getCreateTime());
                        userInfo.put("labels",user.getLabels());
                        userInfo.put("profile",user.getProfile());
                        userInfo.put("unit",user.getUnit());
                        userInfo.put("auditTeacher",user.getIdentity()==0 ? false:true);
                        userInfoList.add(userInfo);
                }
                result.put("userInfoList",userInfoList);
                result.put("isSuccess",true);
                result.put("count",userService.getUserLists().size());
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }

        return result.toJSONString();
    }
        // 审核认证
        @ResponseBody
        @RequestMapping(value = "/auditTeacher", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String updateTeacher(int userId) {
                logger.info("user/updateTeacher request userId：" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user.getIdentity()==0){
                                user.setIdentity(1);
                                user.setTeacher(1);
                                String title="系统消息";
                                String content="恭喜您，通过老师认证审核，平台特意给您赠送十张票，票可以排名，还可以提现！";
                                chatService.addUserNews7(userId,title,content);
                                userService.addUserVote(0,userId,10);
                        }else {
                                user.setIdentity(0);
                                user.setTeacher(0);
                        }
                        userService.updateUser(user);
                        User userInfo = userService.getUser(userId);
                        if (userInfo.getIdentity()==1) {
                                userService.addUserTicket(userInfo, 10);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 查找老师
        @ResponseBody
        @RequestMapping(value = "/getTeacher", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getTeacher(int page, int userId) {
                logger.info("user/getTeacher request page：" + page+" ;userId:"+userId);
                JSONObject result = new JSONObject();
                try {
                        List<Teacher> teachers = userService.getTeacher();
                        Map<Integer, Teacher> teacherMap = Formula.list2map(teachers, t -> t.getUserId());
                        List<User> userTeacherList = userService.getUserTeacherList(page);
                        List<Integer> users = Formula.listDistinct(userTeacherList, u -> u.getId());
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        List<TeacherRecord> teacherRecord = userService.getTeacherRecord(users,userId);
                        Map<Integer, TeacherRecord> teacherRecordMap = Formula.list2map(teacherRecord, t -> t.getTeacherId());
                        JSONArray objects = new JSONArray();
                        for (User user:userTeacherList) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("targetUserId", user.getId());
                                if (teacherMap.get(user.getId())!=null) {
                                        userInfo.put("teacherTicket", teacherMap.get(user.getId()).getTicket());
                                        userInfo.put("dayTeacherTicket", teacherMap.get(user.getId()).getDayTicket());
                                        userInfo.put("day", teacherMap.get(user.getId()).getDay());
                                }
                                userInfo.put("isConsulting",teacherRecordMap.get(user.getId())==null ? false:true);
                                userInfo.put("name", user.getName());
                                userInfo.put("userId", user.getId());
                                userInfo.put("avatar", user.getAvatar());
                                userInfo.put("labels", user.getLabels());
                                userInfo.put("allTicket", user.getAllTicket());
                                userInfo.put("likeCount", user.getLikeCount());
                                userInfo.put("level", user.getLevel());
                                userInfo.put("city", user.getCity());
                                userInfo.put("professional", user.getProfessional());
                                userInfo.put("unit", user.getUnit());
                                userInfo.put("profile", user.getProfile());
                                userInfo.put("isAttention", userAttentionMap.get(user.getId())==null?false:true);
                                userInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                objects.add(userInfo);
                        }
                        result.put("isSuccess", true);
                        result.put("userInfo", objects);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
        }
        // 老师添加咨询价格
        @ResponseBody
        @RequestMapping(value = "/teacherPrice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String teacherPrice(int userId, int ticket, int dayTicket, int day) {
                logger.info("user/teacherPrice request userId：" + userId+ " ;ticket:" +ticket);
                JSONObject result = new JSONObject();
                try {
                        Teacher teacher = userService.getTeacher(userId);
                        if (teacher==null){
                                userService.addTeacher(userId,ticket,dayTicket,day);
                        }else {
                                userService.ticketPrice(teacher,ticket,dayTicket,day);
                        }
                        userService.addTeacherRecord(userId, dayTicket, day);
                        result.put("isSuccess",true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
        }

    // 用户咨询
    @ResponseBody
    @RequestMapping(value = "/addUserPrice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String andUserPrice(int userId, int targetUserId, int ticket, int day) {
        logger.info("user/andUserPrice request userId：" + userId+ " ;ticket:" +ticket+ " ;targetUserId:"+targetUserId+ " ;day:" +day);
        JSONObject result = new JSONObject();
        try {
                OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                if (onlineUser == null) {
                        onlineUser = onlineUserTool.initOnlineUser(userId);
                }
                List<TeacherRecord> teacherRecordByTime = userService.getTeacherRecordByTime(userId,targetUserId);
                if (teacherRecordByTime.size()>0){
                        result.put("isSuccess",false);
                        result.put("msg","您已提交咨询，老师在三天以内会回复您，请耐心等待");
                        return result.toJSONString();
                }
                User user = userService.getUser(userId);
                user.setTicket(user.getTicket()-ticket);
                userService.updateUser(user);
                TeacherRecord teacherRecord = userService.addTeacherRecord(userId, targetUserId, ticket, day);
                OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(targetUserId);
                if (onlineTargetUser==null){
                        WxUtil.wxMessage(user.getOpenid(),IConstants.MESSAGE_TYPE_PRICE,user.getName());
                }
                result.put("createTime",teacherRecord.getCreateTime());
                result.put("isSuccess",true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
    }
    // 老师咨询是否通过
    @ResponseBody
    @RequestMapping(value = "/teacherConsulting", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String teacherConsulting(int userId, int targetUserId, int status, long createTime, String acceptContent) {
        logger.info("user/teacherConsulting request userId：" + userId+ " ;status:" +status+ " ;targetUserId:"+targetUserId+ " ;createTime:" +createTime+ " ;acceptContent:" +acceptContent);
        JSONObject result = new JSONObject();
        try {
                OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                if (onlineUser == null) {
                        onlineUser = onlineUserTool.initOnlineUser(userId);
                }
                User user = userService.getUser(userId);
                User targetUser = userService.getUser(targetUserId);
                TeacherRecord teacherRecord=userService.getTeacherRecord(targetUserId,userId,new Date(createTime));
                userService.addUserVote(userId,targetUserId,teacherRecord.getTicket());
                if (status==0){
                    teacherRecord.setAcceptContent(acceptContent);
                    teacherRecord.setIsAccept(0);
                    userService.updateTeacherRecore(teacherRecord);
                    user.setTicket(user.getTicket()+teacherRecord.getTicket());
                    userService.updateUser(user);
                        OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(targetUserId);
                        if (onlineTargetUser==null){
                                WxUtil.wxMessage(user.getOpenid(),IConstants.MESSAGE_TYPE_ACCEPT,user.getName());
                        }
                }else {
                    teacherRecord.setIsAccept(1);
                    teacherRecord.setIsChoose(1);
                    userService.updateTeacherRecore(teacherRecord);
                    user.setAllTicket(user.getAllTicket()+teacherRecord.getTicket());
                    user.setObtainTicket(user.getObtainTicket()+teacherRecord.getTicket());
                    userService.updateUser(user);
                    result.put("time",userService.getTeacherRecord(targetUserId,userId,new Date(createTime)).getUpdateTime());
                }
                userService.addUserTicket(user, teacherRecord.getTicket());
                userService.addUserVote(targetUserId, userId, teacherRecord.getTicket(), 0);
                chatService.addUserNews2(userId, user.getName(),targetUser.getName(),teacherRecord.getTicket(),userId);
                result.put("isSuccess",true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
    }
    // 用户咨询时提的问题
    @ResponseBody
    @RequestMapping(value = "/teacherQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String teacherQuestion(int userId, int targetUserId, int sex, int age, String content, long createTime) {
        logger.info("user/teacherQuestion request userId：" + userId+ " ;sex:" +sex+ " ;targetUserId:"+targetUserId+ " ;age:" +age+ " ;content:" +content);
        JSONObject result = new JSONObject();
        try {
                TeacherRecord teacherRecord=userService.getTeacherRecord(userId,targetUserId,new Date(createTime));
                teacherRecord.setTeacherSex(sex);
                teacherRecord.setTeacherAge(age);
                teacherRecord.setTeacherContent(content);
                userService.updateTeacherRecore(teacherRecord);
                result.put("isSuccess",true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
    }
    // 用户咨询后的评价
    @ResponseBody
    @RequestMapping(value = "/teacherComment", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String teacherComment(int userId, int targetUserId, long updateTime, int comment) {
        logger.info("user/teacherComment request userId：" + userId+ " ;targetUserId:"+targetUserId+ " ;updateTime:" +updateTime+ " ;comment:" +comment);
        JSONObject result = new JSONObject();
        try {
                TeacherRecord teacherRecord=userService.getTeacherRecords(userId,targetUserId,new Date(updateTime));
                teacherRecord.setComment(comment);
                userService.updateTeacherRecords(teacherRecord);
                Teacher teacher = userService.getTeacher(targetUserId);
                teacher.setComment(teacher.getComment()+comment);
                userService.updateTeacher(teacher);
                result.put("isSuccess",true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
    }



}
