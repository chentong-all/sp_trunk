package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.Collator;
import java.util.*;

import com.ayue.sp.db.po.*;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.pay.Alphabet;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
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
import com.ayue.sp.service.QAService;
import com.ayue.sp.service.SubjectService;
import com.ayue.sp.service.UserService;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年9月1日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/subject")
public class SubjectHandler {
        private Logger logger = Logger.getLogger(getClass());

        @Autowired
        private SubjectService subjectService;
        @Autowired
        private UserService userService;
        @Autowired
        private QAService qaService;
        @Autowired
        private OnlineUserTool onlineUserTool;

        // 创建专题
        @ResponseBody
        @RequestMapping(value = "/createSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createSubject(byte type, String name, String avatar, String description, int userId) {
                logger.info("subject/createSubject request type:" + type + " ;name:" + name + " ;avatar;" + avatar + " ;description:" + description + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        Subject subject = subjectService.createSubject(type, name, avatar, description, userId);
                        subjectService.addSubjectMember(subject.getId(), userId);
                        result.put("subjectId", subject.getId());
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 关注专题
        @ResponseBody
        @RequestMapping(value = "/createUser2Subject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createUser2Subject(int subjectId, int userId) {
                logger.info("subject/createUser2Subject request subjectId:" + subjectId +  " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        User2subject user2subject = subjectService.getUser2subject(userId, subjectId);
                        if (user2subject!=null){
                                result.put("isSuccess",false);
                                result.put("msg","您已关注专题");
                                return result.toJSONString();
                        }
                        subjectService.addSubjectMember(subjectId, userId);
                        subjectService.updateSubject(subjectId);
                        result.put("subjectId", subjectId);
                        result.put("userId", userId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 取消关注专题
        @ResponseBody
        @RequestMapping(value = "/delUser2Subject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String delUser2Subject(int subjectId, int userId) {
                logger.info("subject/createUser2Subject request subjectId:" + subjectId +  " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        User2subject user2subject = subjectService.getUser2subject(userId, subjectId);
                        if (user2subject==null){
                                result.put("isSuccess",false);
                                result.put("msg","您没有关注用户");
                                return result.toJSONString();
                        }
                        subjectService.delSubjectMember(subjectId, userId);
                        subjectService.updateSubjects(subjectId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题详情
        @ResponseBody
        @RequestMapping(value = "/getSubjectDetail", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectDetail(int subjectId, int userId, int userPage) {
                logger.info("subject/getSubjectDetail request subjectId:" + subjectId + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                result.put("msg","专题不存在，请创建");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        List<User2subject> user2subjects = subjectService.getUser2subjectsBySubjectId(subjectId, userPage);
                        List<Integer> userIds = Formula.listDistinct(user2subjects, u -> u.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        result.put("subjectId", subject.getId());
                        result.put("subjectAvatar", subject.getAvatar());
                        result.put("subjectName", subject.getName());
                        result.put("subjectMemberCount", subject.getMemberCount());
                        result.put("subjectDescription", subject.getDescription());
                        JSONArray subjectMemberInfoList = new JSONArray();
                        for (User2subject user2subject : user2subjects) {
                                User user = userMap.get(user2subject.getUserId());
                                JSONObject subjectMemberInfo = new JSONObject();
                                subjectMemberInfo.put("isAdmini", user.getId().equals(subject.getAdminiUserId()) ? true : false);
                                subjectMemberInfo.put("userId", user.getId());
                                subjectMemberInfo.put("userName", user.getName());
                                subjectMemberInfo.put("userAvata", user.getAvatar());
                                subjectMemberInfo.put("userLevel", user.getLevel());
                                subjectMemberInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                subjectMemberInfo.put("userLabels", user.getLabels());
                                subjectMemberInfo.put("userTicket", user.getAllTicket());
                                UserAttention userAttention = userAttentionMap.get(user.getId());
                                subjectMemberInfo.put("isAttention", userAttention == null ? false : true);
                                subjectMemberInfo.put("isOnline", onlineUserTool.getOnlineUser(user.getId()) == null ? false : true);
                                subjectMemberInfoList.add(subjectMemberInfo);
                        }
                        result.put("subjectMemberInfoList", subjectMemberInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题列表
        @ResponseBody
        @RequestMapping(value = "/getSubjectList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectList(byte type, int userId) {
                logger.info("subject/getSubjectList request: type:" + type + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("msg","您不在线");
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        List<Subject> subjects = subjectService.getSubjectInfo(type);
                        List<User2subject> user2subjects = subjectService.getUser2subjectsByUserId(userId);
                        Map<Integer, User2subject> user2subjectMap = Formula.list2map(user2subjects, u -> u.getSubjectId());
                        JSONArray subjectInfoList = new JSONArray();
                        for (Subject subject : subjects) {
                               JSONObject subjectInfo = new JSONObject();
                               subjectInfo.put("subjectId", subject.getId());
                               subjectInfo.put("subjectName", subject.getName());
                               subjectInfo.put("subjectDescription", subject.getDescription());
                               subjectInfo.put("subjectAvatar", subject.getAvatar());
                               subjectInfo.put("subjectIsSystem", subject.getIsSystem());
                               subjectInfo.put("subjectMemberCount", subject.getMemberCount());
                               User2subject user2subject = user2subjectMap.get(subject.getId());
                               subjectInfo.put("isAttention", user2subject == null ? false : true);
                               subjectInfoList.add(subjectInfo);
                        }
                        result.put("subjectInfoList", subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 获取专题信息
        @ResponseBody
        @RequestMapping(value = "/getSubjectInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectInfo(int subjectId, int userId) {
                logger.info("subject/getSubjectInfo request subjectId:" + subjectId + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                result.put("msg","专题不存在，请创建");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        User2subject user2subject = subjectService.getUser2subject(userId, subjectId);
                        result.put("subjectId", subject.getId());
                        result.put("subjectName", subject.getName());
                        result.put("subjectDescription", subject.getDescription());
                        result.put("subjectAvatar", subject.getAvatar());
                        result.put("subjectMemberCount", subject.getMemberCount());
                        result.put("subjectIsAttention", user2subject == null ? false : true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题动态问答
        @ResponseBody
        @RequestMapping(value = "/getSubjectDynamicQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectDynamicQAs(int page, int subjectId) {
                logger.info("qa/getSubjectDynamicQAs request page:" + page + " ;subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        List<Question> questions = qaService.getDynamicQuestions(page, subjectId);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> lastAnswers = qaService.getLastAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> lastAnswerMap = Formula.list2map(lastAnswers, l -> l.getQuestionId());
                        List<Integer> userIds = Formula.listDistinct(lastAnswers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray dynamicQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                Answer answer = lastAnswerMap.get(question.getId());
                                if (answer == null) {
                                        continue;
                                }
                                if (question.getType() == IConstants.QUESTION_TYPE_1 || question.getStatus()==1) {
                                        continue;
                                }
                                User user = userMap.get(answer.getUserId());
                                JSONObject dynamicQAInfo = new JSONObject();
                                dynamicQAInfo.put("questionId", question.getId());
                                dynamicQAInfo.put("questionTitle", question.getSummary());
                                dynamicQAInfo.put("userId", user.getId());
                                dynamicQAInfo.put("userAvatar", user.getAvatar());
                                dynamicQAInfo.put("userName", user.getName());
                                dynamicQAInfo.put("isAnonymous", question.getIsAnonymous());
                                dynamicQAInfo.put("userLevel", user.getLevel());
                                dynamicQAInfo.put("isTeacher", user.getIdentity()==0?false:true);
                                dynamicQAInfo.put("userLabels", user.getLabels());
                                dynamicQAInfo.put("answerId", answer.getId());
                                dynamicQAInfo.put("answerContent", answer.getContent());
                                dynamicQAInfo.put("answerPictures", answer.getPictureUrl());
                                dynamicQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                dynamicQAInfo.put("answerCommentCount", answer.getCommentCount());
                                dynamicQAInfo.put("answerCreateTime", answer.getCreateTime());
                                dynamicQAInfo.put("answerCityAdress", answer.getCityAddress());
                                dynamicQAInfo.put("rewardTicket", question.getRewardTicket());
                                List<Integer> subjectIdList = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectIdList.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectIdList.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectIdList.add(regionSubjectId);
                                }
                                if (subjectIdList.size()>0){
                                        dynamicQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectIdList).get(0).getName());
                                        dynamicQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectIdList).get(0).getId());
                                }
                                dynamicQAInfoList.add(dynamicQAInfo);
                        }
                        result.put("dynamicQAInfoList", dynamicQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题悬赏问答
        @ResponseBody
        @RequestMapping(value = "/getSubjectRewardQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectRewardQAs(int page, int subjectId) {
                logger.info("qa/getSubjectRewardQAs request page:" + page + " ;subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        List<Question> questions = qaService.getRewardQuestions(page, subjectId);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> answers = qaService.getMaxClickAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> answerMap = Formula.list2map(answers, a -> a.getQuestionId());
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray rewardQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                if (question.getType() == IConstants.QUESTION_TYPE_1 || question.getStatus()==1) {
                                        continue;
                                }
                                JSONObject rewardQAInfo = new JSONObject();
                                rewardQAInfo.put("questionId", question.getId());
                                rewardQAInfo.put("questionTitle", question.getSummary());
                                rewardQAInfo.put("rewardTicket", question.getRewardTicket());
                                List<Integer> subjectIdList = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectIdList.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectIdList.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectIdList.add(regionSubjectId);
                                }
                                if (subjectIdList.size()>0){
                                        rewardQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectIdList).get(0).getName());
                                        rewardQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectIdList).get(0).getId());
                                }
                                Answer answer = answerMap.get(question.getId());
                                if (answer != null) {
                                        User user = userMap.get(answer.getUserId());
                                        rewardQAInfo.put("userId", user.getId());
                                        rewardQAInfo.put("userAvatar", user.getAvatar());
                                        rewardQAInfo.put("userName", user.getName());
                                        rewardQAInfo.put("isAnonymous", question.getIsAnonymous());
                                        rewardQAInfo.put("userLevel", user.getLevel());
                                        rewardQAInfo.put("userLabels", user.getLabels());
                                        rewardQAInfo.put("answerId", answer.getId());
                                        rewardQAInfo.put("answerContent", answer.getContent());
                                        rewardQAInfo.put("answerPictures", answer.getPictureUrl());
                                        rewardQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                        rewardQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        rewardQAInfo.put("answerCreateTime", answer.getCreateTime());
                                        rewardQAInfo.put("answerCityAdress", answer.getCityAddress());
                                }
                                rewardQAInfoList.add(rewardQAInfo);
                        }
                        result.put("rewardQAInfoList", rewardQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题最新问答
        @ResponseBody
        @RequestMapping(value = "/getSubjectNewestQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectNewestQAs(int page, int subjectId) {
                logger.info("qa/getSubjectNewestQAs request page:" + page + " ;subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                        List<Question> questions = qaService.getNewestQuestions(page, subjectId);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Integer> questionUserIds = Formula.listDistinct(questions, q -> q.getUserId());
                        Map<Integer, User> questionUserMap = userService.getUserMap(questionUserIds);
                        List<Answer> answers = qaService.getMaxClickAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> answerMap = Formula.list2map(answers, a -> a.getQuestionId());
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray newestQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                if (question.getType() == IConstants.QUESTION_TYPE_1 || question.getStatus()==1) {
                                        continue;
                                }
                                JSONObject newestQAInfo = new JSONObject();
                                User userList = questionUserMap.get(question.getUserId());
                                newestQAInfo.put("questionUserName", userList.getName());
                                newestQAInfo.put("questionUserLevel", userList.getLevel());
                                newestQAInfo.put("questionUserLabels", userList.getLabels());
                                newestQAInfo.put("questionPics", userList.getAttestationUrl());
                                newestQAInfo.put("questionUserId", userList.getId());
                                newestQAInfo.put("isTeacher", userList.getIdentity()==0 ? false:true);
                                newestQAInfo.put("questionUserAvatar", userList.getAvatar());
                                newestQAInfo.put("questionisAnonymous", question.getIsAnonymous());
                                newestQAInfo.put("questionId", question.getId());
                                newestQAInfo.put("questionTitle", question.getSummary());
                                newestQAInfo.put("rewardTicket", question.getRewardTicket());
                                List<Integer> subjectIdList = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectIdList.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectIdList.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectIdList.add(regionSubjectId);
                                }
                                if (subjectIdList.size()>0){
                                        newestQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectIdList).get(0).getName());
                                        newestQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectIdList).get(0).getId());
                                }
                                Answer answer = answerMap.get(question.getId());
                                if (answer != null) {
                                        User user = userMap.get(answer.getUserId());
                                        newestQAInfo.put("userId", user.getId());
                                        newestQAInfo.put("userAvatar", user.getAvatar());
                                        newestQAInfo.put("userName", user.getName());
                                        newestQAInfo.put("isAnonymous", question.getIsAnonymous());
                                        newestQAInfo.put("userLevel", user.getLevel());
                                        newestQAInfo.put("userLabels", user.getLabels());
                                        newestQAInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        newestQAInfo.put("answerId", answer.getId());
                                        newestQAInfo.put("answerContent", answer.getContent());
                                        newestQAInfo.put("answerPictures", answer.getPictureUrl());
                                        newestQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                        newestQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        newestQAInfo.put("answerCreateTime", answer.getCreateTime());
                                        newestQAInfo.put("answerCityAdress", answer.getCityAddress());
                                }
                                newestQAInfoList.add(newestQAInfo);
                        }
                        result.put("newestQAInfoList", newestQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取专题快问
        @ResponseBody
        @RequestMapping(value = "/getSubjectFastQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectFastQAs(int userId, int page, int subjectId) {
                logger.info("qa/getSubjectFastQAs request userId" + userId + " page:" + page + " ;subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.getSubject(subjectId);
                        if (subject == null) {
                                result.put("isSuccess",false);
                                result.put("msg","专题不存在，请创建");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        List<Question> questions = qaService.getFastQuestions(page, subjectId);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> lastAnswers = qaService.getLastAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> lastAnswerMap = Formula.list2map(lastAnswers, l -> l.getQuestionId());
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(lastAnswers, a -> a.getUserId()));
                        userIds.addAll(Formula.listDistinct(questions, a -> a.getUserId()));
                        Map<Integer, List<Answer>> answerMap = qaService.getAnswerMapByQuestionIds(questionIds);
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        Map<Integer, UserAnswerAgree> userAnswerAgreeMap = qaService.getUserAnswerAgreeMap(userId);
                        JSONArray fastQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject fastQAInfo = new JSONObject();
                                User user=null;
                                Answer answer = lastAnswerMap.get(question.getId());
                                if (answer != null) {
                                        user = userMap.get(answer.getUserId());
                                        fastQAInfo.put("userId", user.getId());
                                        fastQAInfo.put("userAvatar", user.getAvatar());
                                        fastQAInfo.put("userName", user.getName());
                                        fastQAInfo.put("answerId", answer.getId());
                                        fastQAInfo.put("answerContent", answer.getContent());
                                        fastQAInfo.put("answerPictures", answer.getPictureUrl());
                                        fastQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                        fastQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        fastQAInfo.put("answerCreateTime", answer.getCreateTime());
                                        fastQAInfo.put("answerCityAdress", answer.getCityAddress());
                                        fastQAInfo.put("answerGetTicketCount", answer.getTicketCount());
                                        fastQAInfo.put("isAnswerAttention", userAttentionMap.get(answer.getUserId()) == null ? false : true);
                                        fastQAInfo.put("isAnswerLike", userAnswerAgreeMap.get(answer.getId()) == null ? false : true);
                                        fastQAInfo.put("userLevel", user.getLevel());
                                        fastQAInfo.put("userLabels", user.getLabels());
                                }else {
                                        user=null;
                                }
                                if (question.getType() == IConstants.QUESTION_TYPE_0 || question.getStatus()==1) {
                                        continue;
                                }
                                User questionUser = userMap.get(question.getUserId());
                                fastQAInfo.put("questionId", question.getId());
                                fastQAInfo.put("questionTitle", question.getSummary());
                                fastQAInfo.put("questionUserId", questionUser.getId());
                                fastQAInfo.put("questionUserAvatar", questionUser.getAvatar());
                                fastQAInfo.put("questionUserName", questionUser.getName());
                                fastQAInfo.put("questionUserLevel", questionUser.getLevel());
                                fastQAInfo.put("questionUserLabels", questionUser.getLabels());
                                fastQAInfo.put("isAnonymous", question.getIsAnonymous());
                                fastQAInfo.put("questionPictures", question.getPictureUrl());
                                fastQAInfo.put("answerCount", answerMap.get(question.getId()) == null ? 0 : answerMap.get(question.getId()).size());
                                fastQAInfo.put("questionCreateTime", question.getCreateTime());
                                fastQAInfo.put("isQuestionAttention", userAttentionMap.get(question.getUserId()) == null ? false : true);
                                fastQAInfo.put("isQuestionLike", false);
                                fastQAInfo.put("questionAgreeCount", 1);
                                List<Integer> subjectIdList = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectIdList.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectIdList.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectIdList.add(regionSubjectId);
                                }
                                if (subjectIdList.size()>0){
                                        fastQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectIdList).get(0).getName());
                                        fastQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectIdList).get(0).getId());
                                }
                                fastQAInfoList.add(fastQAInfo);
                        }
                        result.put("fastQAInfoList", fastQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户创建的专题
        @ResponseBody
        @RequestMapping(value = "/getUserCreateSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserCreateSubject(int userId) {
                logger.info("subject/getUserCreateSubject request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        JSONArray subjectInfoList = new JSONArray();
                        List<Subject> subjects = subjectService.getSubjectByUserId(userId);
                        if (!Formula.isEmptyCollection(subjects)) {
                                List<Integer> subjectIds = Formula.listDistinct(subjects, s -> s.getId());
                                List<Integer> questionIds = new LinkedList<Integer>();
                                questionIds.addAll(qaService.getMaxIdByOtherSubjectId(subjectIds));
                                questionIds.addAll(qaService.getMaxIdByCrowdSubjectId(subjectIds));
                                questionIds.addAll(qaService.getMaxIdByRegionSubjectId(subjectIds));
                                Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                                Map<Integer, Question> subjectQuestionMap = new HashMap<Integer, Question>();
                                for (Question question : questionMap.values()) {
                                        if (question.getCrowdSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getCrowdSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getCrowdSubjectId()).getId()) {
                                                                subjectQuestionMap.put(question.getCrowdSubjectId(), question);
                                                        }
                                                } else {
                                                        subjectQuestionMap.put(question.getCrowdSubjectId(), question);
                                                }
                                        } else if (question.getRegionSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getRegionSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getRegionSubjectId()).getId()){
                                                                subjectQuestionMap.put(question.getRegionSubjectId(), question);}
                                                } else {
                                                        subjectQuestionMap.put(question.getRegionSubjectId(), question);
                                                }
                                        } else if (question.getOtherSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getOtherSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getOtherSubjectId()).getId()){
                                                                subjectQuestionMap.put(question.getOtherSubjectId(), question);}
                                                } else {
                                                        subjectQuestionMap.put(question.getOtherSubjectId(), question);
                                                }
                                        }
                                }
                                for (Subject subject : subjects) {
                                        Question question = subjectQuestionMap.get(subject.getId());
                                        JSONObject subjectInfo = new JSONObject();
                                        subjectInfo.put("subjectId", subject.getId());
                                        subjectInfo.put("subjectName", subject.getName());
                                        subjectInfo.put("subjectAvatar", subject.getAvatar());
                                        if (question != null) {
                                                subjectInfo.put("questionId", question.getId());
                                                subjectInfo.put("questtionTitle", question.getSummary());
                                        }
                                        subjectInfoList.add(subjectInfo);
                                }
                        }
                        result.put("subjectInfoList", subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户加入的专题
        @ResponseBody
        @RequestMapping(value = "/getUserJoinSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserJoinSubject(int userId) {
                logger.info("subject/getUserJoinSubject request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        OnlineUser onlineUser = onlineUserTool.getOnlineUser(userId);
                        if (onlineUser == null) {
                                onlineUser = onlineUserTool.initOnlineUser(userId);
                        }
                        JSONArray subjectInfoList = new JSONArray();
                        List<User2subject> user2subjects = subjectService.getUser2subjectsByUserId(userId);
                        if (!Formula.isEmptyCollection(user2subjects)) {
                                List<Integer> subjectIds = Formula.listDistinct(user2subjects, s -> s.getSubjectId());
                                List<Subject> subjects = subjectService.getSubjects(subjectIds);
                                List<Integer> questionIds = new LinkedList<Integer>();
                                questionIds.addAll(qaService.getMaxIdByOtherSubjectId(subjectIds));
                                questionIds.addAll(qaService.getMaxIdByCrowdSubjectId(subjectIds));
                                questionIds.addAll(qaService.getMaxIdByRegionSubjectId(subjectIds));
                                Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                                Map<Integer, Question> subjectQuestionMap = new HashMap<Integer, Question>();
                                for (Question question : questionMap.values()) {
                                        if (question.getCrowdSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getCrowdSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getCrowdSubjectId()).getId()){
                                                                subjectQuestionMap.put(question.getCrowdSubjectId(), question);}
                                                } else {
                                                        subjectQuestionMap.put(question.getCrowdSubjectId(), question);
                                                }
                                        } else if (question.getRegionSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getRegionSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getRegionSubjectId()).getId()){
                                                                subjectQuestionMap.put(question.getRegionSubjectId(), question);}
                                                } else {
                                                        subjectQuestionMap.put(question.getRegionSubjectId(), question);
                                                }
                                        } else if (question.getOtherSubjectId() != null) {
                                                if (subjectQuestionMap.get(question.getOtherSubjectId()) != null) {
                                                        if (question.getId() > subjectQuestionMap.get(question.getOtherSubjectId()).getId()){
                                                                subjectQuestionMap.put(question.getOtherSubjectId(), question);}
                                                } else {
                                                        subjectQuestionMap.put(question.getOtherSubjectId(), question);
                                                }
                                        }
                                }
                                for (Subject subject : subjects) {
                                        Question question = subjectQuestionMap.get(subject.getId());
                                        JSONObject subjectInfo = new JSONObject();
                                        subjectInfo.put("subjectId", subject.getId());
                                        subjectInfo.put("subjectName", subject.getName());
                                        subjectInfo.put("subjectAvatar", subject.getAvatar());
                                        if (question != null) {
                                                subjectInfo.put("questionId", question.getId());
                                                subjectInfo.put("questtionTitle", question.getSummary());
                                        }
                                        subjectInfoList.add(subjectInfo);
                                }
                        }
                        result.put("subjectInfoList", subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 专题推荐
        @ResponseBody
        @RequestMapping(value = "/recommendSbuject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String recommendSbuject(int userId) {
                logger.info("subject/recommendSbuject request userId：" + userId );
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        Map<Integer, User2subject> user2subjectMap = subjectService.getUser2subjectMapByUserId(userId);
                        JSONArray subjectInfoList = new JSONArray();
                        /*if (!Formula.isEmptyMap(user2subjectMap)) {
                                List<Integer> subjectIds = Formula.listDistinct(user2subjectMap.values(), u -> u.getSubjectId());
                                List<Subject> subjects = new LinkedList<Subject>();
                                subjects.addAll(subjectService.getSubjects(subjectIds));
                                subjects.addAll(subjectService.getSubjectByUserId(userId));
                                int type0Count = 0;
                                int type1Count = 0;
                                for (Subject subject : subjects) {
                                        if (subject.getType() == IConstants.SUBJECT_TYPE_0) {
                                                type0Count++;
                                        }
                                        if (subject.getType() == IConstants.SUBJECT_TYPE_1) {
                                                type1Count++;
                                        }
                                }*/
                                /*byte type = IConstants.SUBJECT_TYPE_0;
                                if (type0Count < type1Count) {
                                        type = IConstants.SUBJECT_TYPE_1;
                                }*/
                                List<Subject> recommendSubjects = subjectService.getSubjectByPage();
                                for (Subject subject : recommendSubjects) {
                                        if (subject.getAdminiUserId() == userId) {
                                                continue;
                                        }
                                        User2subject user2subject = user2subjectMap.get(subject.getId());
                                        JSONObject subjectInfo = new JSONObject();
                                        subjectInfo.put("subjectId", subject.getId());
                                        subjectInfo.put("subjectName", subject.getName());
                                        subjectInfo.put("subjectAvatar", subject.getAvatar());
                                        subjectInfo.put("subjectDescription", subject.getDescription());
                                        subjectInfo.put("isAttention", user2subject == null ? false : true);
                                        subjectInfoList.add(subjectInfo);
                                }
                        /*}*/
                        result.put("subjectInfoList", subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 专题类型信息
        @ResponseBody
        @RequestMapping(value = "/getSubjectType", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectType(byte subjectType) {
                logger.info("subject/getSubjectType request subjectType:" + subjectType );
                JSONObject result = new JSONObject();
                try {
                        JSONArray subjectInfoList = new JSONArray();
                        List<Subject> subjectTypeList = subjectService.getSubjectType(subjectType);
                        for (Subject subject : subjectTypeList) {
                                JSONObject subjectInfo = new JSONObject();
                                subjectInfo.put("subjectId", subject.getId());
                                subjectInfo.put("subjectName", subject.getName());
                                subjectInfoList.add(subjectInfo);
                        }
                        result.put("subject",subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 地区专题信息
        @ResponseBody
        @RequestMapping(value = "/getSubjectCity", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getSubjectCity() {
                logger.info("subject/getSubjectCity request " );
                JSONObject result = new JSONObject();
                try {
                        List<City> cityByRely = subjectService.getCityByRely();
                        result.put("city",cityByRely);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
}
