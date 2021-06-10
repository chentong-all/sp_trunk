package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.po.*;
import com.ayue.sp.service.*;
import com.ayue.sp.tools.TimeUtil;
import com.ayue.sp.tools.online.OnlineUser;
import com.ayue.sp.tools.pay.Sign;
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
import com.ayue.sp.core.IConstants;
import com.ayue.sp.tools.online.OnlineUserTool;

/**
 * 2020年8月27日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/qa")
public class QAHandler {
        private Logger logger = Logger.getLogger(getClass());

        @Autowired
        private QAService qaService;
        @Autowired
        private UserService userService;
        @Autowired
        private AddressService addressService;
        @Autowired
        private SubjectService subjectService;
        @Autowired
        private ChatService chatService;
        @Autowired
        private OnlineUserTool onlineUserTool;
        @Autowired
        private RankService rankService;
        @Autowired
        private RankZSetCDao rankZSetCDao;

        // 获取动态问答
        @ResponseBody
        @RequestMapping(value = "/getDynamicQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getDynamicQAs(int page) {
                logger.info("qa/getDynamicQAs request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = new ArrayList<>();
                        List<Question> questionList = qaService.getDynamicQuestions(page);
                        int questionCountInfo = qaService.getQuestionCountInfo();
                        questions.addAll(questionList);
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
                                if (user!=null){
                                        dynamicQAInfo.put("userId", user.getId());
                                        dynamicQAInfo.put("userAvatar", user.getAvatar());
                                        dynamicQAInfo.put("userName", user.getName());
                                        dynamicQAInfo.put("userLevel", user.getLevel());
                                        dynamicQAInfo.put("userLabels", user.getLabels());
                                        dynamicQAInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                }
                                List<Integer> subjectId = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectId.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectId.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectId.add(regionSubjectId);
                                }
                                if (subjectId.size()>0){
                                        dynamicQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectId).get(0).getName());
                                        dynamicQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectId).get(0).getId());
                                }
                                dynamicQAInfo.put("questionId", question.getId());
                                dynamicQAInfo.put("questionTitle", question.getSummary());
                                dynamicQAInfo.put("isAnonymous", question.getIsAnonymous());
                                dynamicQAInfo.put("answerId", answer.getId());
                                dynamicQAInfo.put("answerContent", answer.getContent());
                                dynamicQAInfo.put("answerPictures", answer.getPictureUrl());
                                dynamicQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                dynamicQAInfo.put("answerCommentCount", answer.getCommentCount());
                                dynamicQAInfo.put("answerCreateTime", answer.getCreateTime());
                                dynamicQAInfo.put("answerCityAdress", answer.getCityAddress());
                                dynamicQAInfo.put("ticket", question.getRewardTicket());
                                dynamicQAInfo.put("isOnline",onlineUserTool.getOnlineUser(user.getId())==null?false:true);
                                dynamicQAInfoList.add(dynamicQAInfo);
                        }
                        result.put("dynamicQAInfoList", dynamicQAInfoList);
                        result.put("questionPage", questionCountInfo/20+1);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取悬赏问答
        @ResponseBody
        @RequestMapping(value = "/getRewardQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getRewardQAs(int page) {
                logger.info("qa/getRewardQAs request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getRewardQuestions(page);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> answers = qaService.getMaxClickAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> answerMap = Formula.list2map(answers, a -> a.getQuestionId());
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray rewardQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                if (question.getType() == IConstants.QUESTION_TYPE_1 || question.getStatus()==1 || question.getRewardTicket()==0) {
                                        continue;
                                }
                                JSONObject rewardQAInfo = new JSONObject();
                                rewardQAInfo.put("questionId", question.getId());
                                rewardQAInfo.put("questionTitle", question.getSummary());
                                rewardQAInfo.put("rewardTicket", question.getRewardTicket());
                                List<Integer> subjectId = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectId.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectId.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectId.add(regionSubjectId);
                                }
                                if (subjectId.size()>0){
                                        rewardQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectId).get(0).getName());
                                        rewardQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectId).get(0).getId());
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
                        logger.error(e);
                        return e.toString();
                }
                return result.toJSONString();
        }

        // 获取最新问答
        @ResponseBody
        @RequestMapping(value = "/getNewestQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getNewestQAs(int page) {
                logger.info("qa/getNewestQAs request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds =null;
                        List<Question> questions = qaService.getNewestQuestions(page);
                        Map<Integer, Question> questionMap = Formula.list2map(questions, q -> q.getId());
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> answers = qaService.getMaxClickAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> answerMap = Formula.list2map(answers, a -> a.getQuestionId());
                        userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        userIds = Formula.listDistinct(questions, q -> q.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray newestQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                if (question.getType() == IConstants.QUESTION_TYPE_1 || question.getStatus()==1) {
                                        continue;
                                }
                                JSONObject newestQAInfo = new JSONObject();
                                newestQAInfo.put("questionId", question.getId());
                                newestQAInfo.put("questionTitle", question.getSummary());
                                newestQAInfo.put("rewardTicket", question.getRewardTicket());
                                List<Integer> subjectId = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectId.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectId.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectId.add(regionSubjectId);
                                }
                                if (subjectId.size()>0){
                                        newestQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectId).get(0).getName());
                                        newestQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectId).get(0).getId());
                                }
                                Answer answer = answerMap.get(question.getId());
                                if (answer != null) {
                                        User user = userMap.get(answer.getUserId());
                                        if (user!=null){
                                            newestQAInfo.put("userId", user.getId());
                                            newestQAInfo.put("userAvatar", user.getAvatar());
                                            newestQAInfo.put("userName", user.getName());
                                            newestQAInfo.put("userLevel", user.getLevel());
                                            newestQAInfo.put("userLabels", user.getLabels());
                                            newestQAInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        }
                                        newestQAInfo.put("isAnonymous", question.getIsAnonymous());
                                        newestQAInfo.put("answerId", answer.getId());
                                        newestQAInfo.put("answerContent", answer.getContent());
                                        newestQAInfo.put("answerPictures", answer.getPictureUrl());
                                        newestQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                        newestQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        newestQAInfo.put("answerCreateTime", answer.getCreateTime());
                                        newestQAInfo.put("answerCityAdress", answer.getCityAddress());
                                }
                                Question questionUser = questionMap.get(question.getId());
                                if (questionUser!=null){
                                        User user = userMap.get(questionUser.getUserId());
                                        newestQAInfo.put("questionUserName",user.getName());
                                        newestQAInfo.put("questionUserLevel",user.getLevel());
                                        newestQAInfo.put("questionUserLabels",user.getLabels());
                                        newestQAInfo.put("isTeacher",user.getIdentity()==0 ? false:true);
                                        newestQAInfo.put("questionPics",questionUser.getPictureUrl());
                                        newestQAInfo.put("questionUserId",user.getId());
                                        newestQAInfo.put("questionUserAvatar",user.getAvatar());
                                        newestQAInfo.put("questionisAnonymous",questionUser.getIsAnonymous());
                                        newestQAInfo.put("isOnline",onlineUserTool.getOnlineUser(user.getId())==null?false:true);
                                }
                                newestQAInfoList.add(newestQAInfo);
                        }
                        result.put("newestQAInfoList", newestQAInfoList);
                } catch (Exception e) {
                        logger.error(e);
                        return e.toString();
                }
                return result.toJSONString();
        }

        // 获取快问
        @ResponseBody
        @RequestMapping(value = "/getFastQAs", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getFastQAs(int userId, int page) {
                logger.info("qa/getFastQAs request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getFastQuestions(page);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Answer> lastAnswers = qaService.getLastAnswerByQuestionIds(questionIds);
                        Map<Integer, Answer> lastAnswerMap = Formula.list2map(lastAnswers, l -> l.getQuestionId());
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(questions, a -> a.getUserId()));
                        userIds.addAll(Formula.listDistinct(lastAnswers, a -> a.getUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        JSONArray fastQAInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject fastQAInfo = new JSONObject();
                                Answer answer = lastAnswerMap.get(question.getId());
                                User user=null;
                                if (answer == null) {
                                        user=null;
                                }else {
                                        user = userMap.get(answer.getUserId());
                                        fastQAInfo.put("answerUserId", user.getId());
                                        fastQAInfo.put("answerUserAvatar", user.getAvatar());
                                        fastQAInfo.put("answerUserName", user.getName());
                                        fastQAInfo.put("answerId", answer.getId());
                                        fastQAInfo.put("answerContent", answer.getContent());
                                        fastQAInfo.put("answerPictures", answer.getPictureUrl());
                                        fastQAInfo.put("answerAgreeCount", answer.getAgreeCount());
                                        fastQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        fastQAInfo.put("answerCreateTime", answer.getCreateTime());
                                        fastQAInfo.put("answerCityAdress", answer.getCityAddress());
                                        fastQAInfo.put("answerTicketCount", answer.getTicketCount());
                                        fastQAInfo.put("answerCommentCount", answer.getCommentCount());
                                        fastQAInfo.put("answerUserIsAttention", userAttentionMap.get(answer.getUserId()) == null ? false : true);
                                        fastQAInfo.put("answerUserLevel", user.getLevel());
                                        UserAnswerAgree userAnswerAgree = qaService.getUserAnswerAgree(userId, answer.getId());
                                        fastQAInfo.put("isLike", userAnswerAgree == null ? false : true);
                                }
                                if (question.getType() == IConstants.QUESTION_TYPE_0 || question.getStatus()==1) {
                                    continue;
                                }
                                User questionUser = userMap.get(question.getUserId());
                                if (questionUser!=null){
                                        fastQAInfo.put("questionUserId", questionUser.getId());
                                        fastQAInfo.put("questionUserAvatar", questionUser.getAvatar());
                                        fastQAInfo.put("questionUserName", questionUser.getName());
                                        fastQAInfo.put("questionUserLevel", questionUser.getLevel());
                                        fastQAInfo.put("questionUserLabels", questionUser.getLabels());
                                }
                                fastQAInfo.put("questionId", question.getId());
                                fastQAInfo.put("questionTitle", question.getSummary());
                                fastQAInfo.put("questionUrl", question.getPictureUrl());
                                fastQAInfo.put("isAnonymous", question.getIsAnonymous());
                                fastQAInfo.put("answerCount", question.getAnswerCount());
                                List<Integer> subjectId = new ArrayList<>();
                                Integer crowdSubjectId = question.getCrowdSubjectId();
                                Integer otherSubjectId = question.getOtherSubjectId();
                                Integer regionSubjectId = question.getRegionSubjectId();
                                if (crowdSubjectId!=null){
                                        subjectId.add(crowdSubjectId);
                                }
                                if (otherSubjectId!=null){
                                        subjectId.add(otherSubjectId);
                                }
                                if (regionSubjectId!=null){
                                        subjectId.add(regionSubjectId);
                                }
                                if (subjectId.size()>0){
                                        fastQAInfo.put("questionSubjectName", subjectService.getSubjects(subjectId).get(0).getName());
                                        fastQAInfo.put("questionSubjectId", subjectService.getSubjects(subjectId).get(0).getId());
                                }
                                fastQAInfo.put("questionsAgreeCount", 1);
                                fastQAInfo.put("isQuestionLike", false);
                                fastQAInfoList.add(fastQAInfo);
                        }
                        result.put("fastQAInfoList", fastQAInfoList);
                } catch (Exception e) {
                        logger.error(e);
                        return e.toString();
                }
                return result.toJSONString();
        }
        // 获取快问的回答
        @ResponseBody
        @RequestMapping(value = "/getFastAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getFastAnswer(int userId, int page,int questionId) {
                logger.info("qa/getFastAnswer request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Answer> answers = qaService.getAnswerByQuestionId(questionId, page);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray fastAnswerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject fastAnswerInfo = new JSONObject();
                                User user = userMap.get(answer.getUserId());
                                fastAnswerInfo.put("answerUserId", user.getId());
                                fastAnswerInfo.put("answerUserAvatar", user.getAvatar());
                                fastAnswerInfo.put("answerUserName", user.getName());
                                fastAnswerInfo.put("answerId", answer.getId());
                                fastAnswerInfo.put("answerContent", answer.getContent());
                                fastAnswerInfo.put("answerPictures", answer.getPictureUrl());
                                fastAnswerInfo.put("answerAgreeCount", answer.getAgreeCount());
                                fastAnswerInfo.put("answerCommentCount", answer.getCommentCount());
                                fastAnswerInfo.put("answerCreateTime", answer.getCreateTime());
                                fastAnswerInfo.put("answerCityAdress", answer.getCityAddress());
                                fastAnswerInfo.put("answerTicketCount", answer.getTicketCount());
                                fastAnswerInfo.put("answerCommentCount", answer.getCommentCount());
                                fastAnswerInfo.put("answerUserIsAttention", userAttentionMap.get(answer.getUserId()) == null ? false : true);
                                fastAnswerInfo.put("answerUserLevel", user.getLevel());
                                UserAnswerAgree userAnswerAgree = qaService.getUserAnswerAgree(userId, answer.getId());
                                fastAnswerInfo.put("isLike", userAnswerAgree == null ? false : true);
                                fastAnswerInfoList.add(fastAnswerInfo);
                        }
                        result.put("total",qaService.getAnswerByQuestion(questionId).size());
                        result.put("fastQAInfoList", fastAnswerInfoList);
                } catch (Exception e) {
                        logger.error(e);
                        return e.toString();
                }
                return result.toJSONString();
        }

        // 获取问题详情
        @ResponseBody
        @RequestMapping(value = "/getQuestionDetail", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getQuestionDetail(int questionId, int userId) {
                logger.info("qa/getQuestionDetail request questionId:" + questionId + " ;userId:" + userId);
                // 获取问题关系链
                JSONObject result = new JSONObject();
                try {
                        //通过id查询question
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                return result.toJSONString();
                        }
                        //通过id查询user表
                        User user = userService.getUser(userId);
                        if (user == null) {
                                return result.toJSONString();
                        }
                        //通过id查询question_link_record表（问题父子关系表）
                        QuestionLinkRecord questionLinkRecord = qaService.getQuestionLinkRecord(questionId);
                        List<Integer> questionIds = new LinkedList<Integer>();
                        if (questionLinkRecord != null) {
                                //通过question_link_record表的father_id查询question_link_record表（问题父子关系表）
                                List<QuestionLinkRecord> questionLinkRecords = qaService.getQuestionLinkRecordByFatherId(questionLinkRecord.getFatherId());
                                //去重将question_link_record表的id保存到questionIds中
                                questionIds.addAll(Formula.listDistinct(questionLinkRecords, q -> q.getId()));
                                //去重将question_link_record表的father_id保存到questionIds中
                                questionIds.add(questionLinkRecord.getFatherId());
                        } else {
                                //通过questionId查询question_link_record表（问题父子关系表）
                                List<QuestionLinkRecord> questionLinkRecords = qaService.getQuestionLinkRecordByFatherId(questionId);
                                //去重将question_link_record表的id保存到questionIds中
                                questionIds.addAll(Formula.listDistinct(questionLinkRecords, q -> q.getId()));
                                //去重将question_link_record表的father_id保存到questionIds中
                                questionIds.add(questionId);
                        }
                        // 获取问题（通过questionIds匹配id查询question表的数据）
                        List<Question> questions = qaService.getQuestions(questionIds);
                        //保存userids
                        List<Integer> userIds = new LinkedList<>();
                        //将question表的userid保存到userids
                        userIds.add(question.getUserId());
                        // 获取回答
                        //通过questionIds匹配questionid查询answer表的数据
                        List<Answer> answers = qaService.getAnswersByQuestionIds(questionIds);
                        //去重后将answer表的userid放到userIds
                        userIds.addAll(Formula.listDistinct(answers, a -> a.getUserId()));
                        //根据answer表的question_id保存为map
                        Map<Integer, List<Answer>> answerMap = Formula.listGrouping(answers, a -> a.getQuestionId());
                        //去重后保存answer表的id保存到answerIds
                        List<Integer> answerIds = Formula.listDistinct(answers, a -> a.getId());
                        // 获取评论
                        List<Comment> comments = qaService.getCommentsByAnswerIds(answerIds);
                        userIds.addAll(Formula.listDistinct(comments, c -> c.getUserId()));
                        Map<Integer, List<Comment>> commentMap = Formula.listGrouping(comments, c -> c.getAnswerId());
                        List<Integer> commentIds = Formula.listDistinct(comments, c -> c.getId());
                        // 获取回复
                        List<Reply> replies = qaService.getRepliesByCommentIds(commentIds);
                        userIds.addAll(Formula.listDistinct(replies, r -> r.getUserId()));
                        userIds.addAll(Formula.listDistinct(replies, r -> r.getTargetUserId()));
                        Map<Integer, List<Reply>> replyMap = Formula.listGrouping(replies, r -> r.getCommentId());
                        // 获取用户关注
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        // 获取用户(key值为id，value为user表)
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        // 添加问题阅读次数
                        qaService.addQuestionReadCount(question);
                        // 添加阅读历史
                        qaService.replaceUserBrowse(userId, questionId);
                        // 获取点赞
                        Map<Integer, UserAnswerAgree> userAnswerAgreeMap = qaService.getUserAnswerAgreeMap(userId);
                        Map<Integer, UserCommentAgree> userCommentAgreeMap = qaService.getUserCommentAgreeMap(userId);
                        Map<Integer, UserReplyAgree> userReplyAgreeMap = qaService.getUserReplyAgreeMap(userId);
                        // 获取收藏
                        Map<Integer, UserQuestionCollection> userQuestionCollectionMap = qaService.getUserQuestionCollectionMap(userId);
                        User questionUser = userMap.get(question.getUserId());
                        UserAttention questionUserAttention = userAttentionMap.get(questionUser.getId());
                        result.put("userId", questionUser.getId());
                        result.put("isTeacher", questionUser.getIdentity()==0 ? false:true);
                        result.put("userName", questionUser.getName());
                        result.put("userAvatar", questionUser.getAvatar());
                        result.put("userLevel", questionUser.getLevel());
                        result.put("userLabels", questionUser.getLabels());
                        result.put("isAttention", questionUserAttention == null ? false : true);
                        result.put("isOnline", onlineUserTool.getOnlineUser(questionUser.getId()) == null ? false : true);
                        JSONArray qaInfoList = new JSONArray();
                        questions.sort((e1, e2) -> {
                                if ((e1.getId() > e2.getId())) {
                                        return 1;
                                } else {
                                        return -1;
                                }
                        });
                        for (int i = 0; i < questions.size(); i++) {
                                Question q = questions.get(i);
                                JSONObject qaInfo = new JSONObject();
                                if (q.getStatus() == 0) {
                                        qaInfo.put("questionNum", i + 1);
                                        qaInfo.put("questionId", q.getId());
                                        qaInfo.put("userGender", q.getGender());
                                        qaInfo.put("userQuestionYear", q.getBirthYear());
                                        qaInfo.put("questionLabels", q.getLabels());
                                        qaInfo.put("questionCreateTime", q.getCreateTime());
                                        qaInfo.put("questionCityAddress", q.getCityAddress());
                                        qaInfo.put("questionTitle", q.getSummary());
                                        qaInfo.put("questionContent", q.getContent());
                                        qaInfo.put("questionPictures", q.getPictureUrl());
                                        qaInfo.put("questionReadCount", q.getReadCount());
                                        qaInfo.put("questionAnswerCount", q.getAnswerCount());
                                        qaInfo.put("questionCollectionCount", q.getCollectionCount());
                                        qaInfo.put("isAnonymous", q.getIsAnonymous());
                                        qaInfo.put("rewardTicker", q.getRewardTicket());
                                        qaInfo.put("isCollected", qaService.getUserQuestionCollection(userId, q.getId()) == null ? false : true);
                                        List<Answer> qaAnswers = answerMap.get(q.getId());
                                        if (!Formula.isEmptyCollection(qaAnswers)) {
                                            JSONArray answerInfoList = new JSONArray();
                                            JSONArray directionSame = new JSONArray();
                                            JSONArray answerAdopt = new JSONArray();
                                                for (Answer answer : qaAnswers) {
                                                        User answerUser = userMap.get(answer.getUserId());
                                                        JSONObject answerInfo = new JSONObject();
                                                        if (answerUser!=null){
                                                                UserAttention answerUserAttention = userAttentionMap.get(answerUser.getId());
                                                                answerInfo.put("answerUserIsAttention", answerUserAttention == null ? false : true);
                                                                answerInfo.put("answerUserId", answerUser.getId());
                                                                answerInfo.put("answerUserName", answerUser.getName());
                                                                answerInfo.put("answerUserAvatar", answerUser.getAvatar());
                                                                answerInfo.put("answerUserLevel", answerUser.getLevel());
                                                                answerInfo.put("answerUserLabels", answerUser.getLabels());
                                                                answerInfo.put("isTeacher", answerUser.getIdentity()==0 ? false:true);
                                                                answerInfo.put("isOnline", onlineUserTool.getOnlineUser(answerUser.getId()) == null ? false : true);
                                                        }
                                                        answerInfo.put("answerId", answer.getId());
                                                        answerInfo.put("answerContent", answer.getContent());
                                                        answerInfo.put("answerPictures", answer.getPictureUrl());
                                                        answerInfo.put("answerCreateTime", answer.getCreateTime());
                                                        answerInfo.put("answerClickCount", answer.getClickCount());
                                                        answerInfo.put("answerTicketCount", answer.getTicketCount());
                                                        answerInfo.put("answerCommentCount", answer.getCommentCount());
                                                        answerInfo.put("answerForwardCount", answer.getForwardCount());
                                                        answerInfo.put("answerAgreeCount", answer.getAgreeCount());
                                                        answerInfo.put("answerDirection", answer.getDirection());
                                                        answerInfo.put("answerTicket", answer.getTicket());
                                                        answerInfo.put("answerCharge", answer.getCharge()==0 ? false:true);
                                                        answerInfo.put("answerIsCharge", qaService.getAnswerCharge(answer.getId(),userId)==null ? false:true);
                                                        answerInfo.put("answerAdopt", answer.getAdopt()==0 ? false:true);
                                                        answerInfo.put("isLike", qaService.getUserAnswerAgree(userId, answer.getId()) == null ? false : true);
                                                        List<Comment> qaComments = commentMap.get(answer.getId());
                                                        if (!Formula.isEmptyCollection(qaComments)) {
                                                                JSONArray commentInfoList = new JSONArray();
                                                                for (Comment comment : qaComments) {
                                                                        JSONObject commentInfo = new JSONObject();
                                                                        User commentUser = userMap.get(comment.getUserId());
                                                                        if (commentUser!=null){
                                                                                UserAttention commentUserAttention = userAttentionMap.get(commentUser.getId());
                                                                                commentInfo.put("commentUserIsAttention", commentUserAttention == null ? false : true);
                                                                                commentInfo.put("commentUserId", commentUser.getId());
                                                                                commentInfo.put("commentUserName", commentUser.getName());
                                                                                commentInfo.put("commentUserAvatar", commentUser.getAvatar());
                                                                                commentInfo.put("commentUserLevel", commentUser.getLevel());
                                                                                commentInfo.put("commentUserLabels", commentUser.getLabels());
                                                                                commentInfo.put("isTeacher", commentUser.getIdentity()==0 ? false:true);
                                                                                commentInfo.put("isOnline", onlineUserTool.getOnlineUser(commentUser.getId()) == null ? false : true);
                                                                        }
                                                                        commentInfo.put("commentId", comment.getId());
                                                                        commentInfo.put("commentContent", comment.getContent());
                                                                        commentInfo.put("commentCreateTime", comment.getCreateTime());
                                                                        commentInfo.put("commentReplyCount", comment.getReplyCount());
                                                                        commentInfo.put("commentAgreeCount", comment.getAgreeCount());
                                                                        commentInfo.put("isLike", qaService.getUserCommentAgree(userId, comment.getId()) == null ? false : true);
                                                                        List<Reply> qaReplies = replyMap.get(comment.getId());
                                                                        if (!Formula.isEmptyCollection(qaReplies)) {
                                                                                JSONArray replyInfoList = new JSONArray();
                                                                                for (Reply reply : qaReplies) {
                                                                                        JSONObject replyInfo = new JSONObject();
                                                                                        User replyUser = userMap.get(reply.getUserId());
                                                                                        User replyTargetUser = userMap.get(reply.getTargetUserId());
                                                                                        if (replyUser!=null){
                                                                                                UserAttention replyUserAttention = userAttentionMap.get(replyUser.getId());
                                                                                                replyInfo.put("replyUserId", replyUser.getId());
                                                                                                replyInfo.put("replyUserName", replyUser.getName());
                                                                                                replyInfo.put("replyUserAvatar", replyUser.getAvatar());
                                                                                                replyInfo.put("replyUserLevel", replyUser.getLevel());
                                                                                                replyInfo.put("replyUserLabels", replyUser.getLabels());
                                                                                                replyInfo.put("isTeacher", replyUser.getIdentity()==0 ? false:true);
                                                                                                replyInfo.put("replyUserIsAttention", replyUserAttention == null ? false : true);
                                                                                                replyInfo.put("isOnline", onlineUserTool.getOnlineUser(replyUser.getId()) == null ? false : true);

                                                                                        }
                                                                                        replyInfo.put("replyTargerUserId", reply.getTargetUserId());
                                                                                        replyInfo.put("replyTargetUserName", replyTargetUser.getName());
                                                                                        replyInfo.put("replyId", reply.getId());
                                                                                        replyInfo.put("replyContent", reply.getContent());
                                                                                        replyInfo.put("replyCreateTime", reply.getCreateTime());
                                                                                        replyInfo.put("replyAgreeCount", reply.getAgreeCount());
                                                                                        replyInfo.put("replyCount", 1);
                                                                                        replyInfo.put("isLike", qaService.getUserReplyAgree(userId, reply.getId()) == null ? false : true);
                                                                                        replyInfoList.add(replyInfo);
                                                                                }
                                                                                commentInfo.put("replyInfoList", replyInfoList);
                                                                        }
                                                                        commentInfoList.add(commentInfo);
                                                                }
                                                                answerInfo.put("commentInfoList", commentInfoList);
                                                        }
                                                        String answerDirections = answerInfo.getString("answerDirection");
                                                        String answerAdopts = answerInfo.getString("answerAdopt");
                                                        if (answerDirections.equals("0")) {
                                                                answerInfoList.add(answerInfo);
                                                        }
                                                        if (answerDirections.equals("1")){
                                                                directionSame.add(answerInfo);
                                                        }
                                                        if (answerAdopts.equals("true")){
                                                                answerAdopt.add(answerInfo);
                                                        }
                                                }
                                            qaInfo.put("answerDirectionSame", directionSame);
                                            qaInfo.put("answerInfoList", answerInfoList);
                                            qaInfo.put("answerAdopt", answerAdopt);
                                        }
                                    qaInfoList.add(qaInfo);
                                }
                        }
                        result.put("qaInfoList", qaInfoList);
                } catch (Exception e) {
                        logger.error(e);
                        return e.toString();
                }
                return JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
        }

        // 创建回答界面
        @ResponseBody
        @RequestMapping(value = "/createAnswerView", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createAnswerView(int questionId, int userId) {
                logger.info("qa/createAnswerView request questionId:" + questionId + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                return result.toJSONString();
                        }
                        User user = userService.getUser(userId);
                        if (user == null) {
                                return result.toJSONString();
                        }
                        User questionUser = userService.getUser(question.getUserId());
                        UserAttention userAttention = userService.getUserAttention(userId, questionUser.getId());
                        result.put("userId", questionUser.getId());
                        result.put("userAvatar", questionUser.getAvatar());
                        result.put("userName", questionUser.getName());
                        result.put("userLevel", questionUser.getLevel());
                        result.put("userLabels", questionUser.getLabels());
                        result.put("isAnonymous", question.getIsAnonymous());
                        result.put("isAttention", userAttention == null ? false : true);
                        result.put("questionId", question.getId());
                        result.put("questionTitle", question.getSummary());
                        result.put("questionContent", question.getContent());
                        result.put("questionPictures", question.getPictureUrl());
                        result.put("isTeacher", questionUser.getIdentity()== 0 ? false:true);
                        result.put("isOnline", onlineUserTool.getOnlineUser(questionUser.getId()) == null ? false : true);
                } catch (Exception e) {
                        logger.error(e);
                        return e.toString();
                }
                return result.toJSONString();
        }

        // 回答问题
        @ResponseBody
        @RequestMapping(value = "/createAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createAnswer(int userId, int questionId, String content, String pictureUrl, HttpServletRequest request, int direction, int ticket, int charge) {
                logger.info("qa/creatAnswer request userId：" + userId + " ;questionId:" + questionId + " ;content:" + content + " ;pictureURL:" + pictureUrl);
                JSONObject result = new JSONObject();
                try {
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                return result.toJSONString();

                        }
                        User user = userService.getUser(userId);
                        if (user == null) {
                                return result.toJSONString();
                        }
                        User userQuestion = userService.getUser(question.getUserId());
                        String cityAddress = addressService.getCityAdress(request);
                        Answer answer = qaService.creatAnswer(userId, questionId, content, pictureUrl, cityAddress, direction, ticket, charge);
                        userService.addUserAnswerCount(user);
                        qaService.lockQuestion(questionId);
                        try {
                                question = qaService.getQuestion(questionId);
                                question.setAnswerCount(question.getAnswerCount() + 1);
                                qaService.updateQuestion(question);
                                userService.updateUser2visitedFinish(questionId, userId);
                                User userInfo = userService.getUser(question.getUserId());
                                chatService.addUserNews0(question.getUserId(), questionId, answer.getId(), question.getSummary(), userInfo.getName(), user.getName(),userId);
                        } finally {
                                qaService.unlockQuestion(questionId);
                        }
                        result.put("answerId", answer.getId());
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_2);
                        if (energyBeans.size()<=4){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_2);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        OnlineUser onlineTargetUser = onlineUserTool.getOnlineUser(question.getUserId());
                        if (onlineTargetUser==null){
                                WxUtil.wxMessage(userQuestion.getOpenid(),IConstants.MESSAGE_TYPE_QUESTION,userQuestion.getName());
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 评论回答
        @ResponseBody
        @RequestMapping(value = "/createComment", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createComment(int userId, int answerId, String content) {
                logger.info("qa/createComment request userId：" + userId + " ;answerId:" + answerId + " ;content:" + content);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        Comment comment = qaService.createComment(userId, answerId, content);
                        userService.addUserCommentCount(user);
                        qaService.addAnswerCommentCount(answerId);
                        Answer answer = qaService.getAnswer(comment.getAnswerId());
                        Question question = qaService.getQuestion(answer.getQuestionId());
                        chatService.addUserNews4(answer.getUserId(), question.getId(), answer.getId(),  comment.getId(), answer.getContent(), user.getName(),userService.getUser(answer.getUserId()).getName(),userId);
                        result.put("commentId", comment.getId());
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_4);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_4);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 回复评论
        @ResponseBody
        @RequestMapping(value = "/createReply", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createReply(int userId, int targetUserId, int commentId, String content) {
                logger.info("qa/createReply request userId：" + userId + " ;targetUserId:" + targetUserId + " ;commentId:" + commentId + " ;content:" + content);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        Reply reply = qaService.createReply(userId, targetUserId, commentId, content);
                        userService.addUserReplyCount(user);
                        qaService.addCommentReplyCount(commentId);
                        Comment comment = qaService.getComment(commentId);
                        Answer answer = qaService.getAnswer(comment.getAnswerId());
                        Question question = qaService.getQuestion(answer.getQuestionId());
                        chatService.addUserNews5(targetUserId, question.getId(), answer.getId(), commentId, reply.getId(), comment.getContent(), user.getName(),userService.getUser(targetUserId).getName(),userId);
                        result.put("replyId", reply.getId());
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_5);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_5);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 创建健康问题
        @ResponseBody
        @RequestMapping(value = "/createHealthyQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createHealthyQuestion(int userId, boolean isAnonymous, String title, String content, String pictureUrl,
                                            boolean isMan, int birthYear, byte relation, int rewardTicket, int subjectId,
                                            String labels, HttpServletRequest request) {
                logger.info("qa/createHealthyQuestion request userId：" + userId + " ;isAnonymous:" + isAnonymous + " ;title:" + title + " ;content:" + content + " ;pictureURL:" + pictureUrl
                                + " ;isMan:" + isMan + " ;birthYear:" + birthYear + " ;relation:" + relation + " ;rewardTicket:" + rewardTicket + " ;subjectId:" + subjectId + ";labels:" + labels);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Subject subject=null;
                        if (subjectId!=0) {
                                subject = subjectService.getSubject(subjectId);
                                if (subject == null) {
                                        result.put("isSuccess", false);
                                        result.put("msg", "专题不存在");
                                        return result.toJSONString();
                                }
                        }
                        String cityAddress = addressService.getCityAdress(request);
                        Question question = qaService.createHealthyQuestion(userId, isAnonymous, title, content, pictureUrl, isMan, birthYear, relation, rewardTicket, subject, labels, cityAddress);
                        userService.addUserQuestionCount(user);
                        userService.updateUserTicket(rewardTicket,userId);
                        User2subject user2subject = subjectService.getUser2subject(userId, subjectId);
                        if (user2subject==null){
                                subjectService.addSubjectMember(subjectId, userId);
                                subjectService.updateSubject(subjectId);
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_1);
                        if (energyBeans.size()<=4){
                            qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_1);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                            result.put("isEnergyBeanSuccess", false);
                        }
                        result.put("questionId", question.getId());
                        result.put("questionTitle", question.getSummary());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取问题回答信息
        @ResponseBody
        @RequestMapping(value = "/getQuestionAnswerInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getQuestionAnswerInfo(int userId, int questionId, int answerPage) {
                logger.info("qa/getQuestionAnswerInfo request userId：" + userId + " ;questiongId:" + questionId + " ;answerPage:" + answerPage);
                JSONObject result = new JSONObject();
                try {
                        Question question = qaService.getQuestion(questionId);
                        List<Answer> answers = qaService.getAnswerByQuestionId(questionId, answerPage);
                        List<Integer> userIds = Formula.listDistinct(answers, a -> a.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        result.put("questionId", question.getId());
                        result.put("questionTitle", question.getSummary());
                        result.put("answerCount",answers.size());
                        JSONArray answerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject answerInfo = new JSONObject();
                                User user = userMap.get(answer.getUserId());
                                answerInfo.put("userId", user.getId());
                                answerInfo.put("userAvatar", user.getAvatar());
                                answerInfo.put("userName", user.getName());
                                answerInfo.put("userLevel", user.getLevel());
                                answerInfo.put("userLabels", user.getLabels());
                                UserAttention userAttention = userAttentionMap.get(answer.getUserId());
                                answerInfo.put("isAttention", userAttention == null ? false : true);
                                answerInfo.put("answerId", answer.getId());
                                answerInfo.put("answerUserId", answer.getUserId());
                                answerInfo.put("answerContent", answer.getContent());
                                answerInfo.put("answerTicketCount", answer.getTicketCount());
                                answerInfo.put("answerCommentCount", answer.getCommentCount());
                                answerInfo.put("answerForwardCount", answer.getForwardCount());
                                answerInfo.put("answerAgreeCount", answer.getAgreeCount());
                                answerInfo.put("isOnline", onlineUserTool.getOnlineUser(answer.getUserId()) == null ? false : true);
                                UserAnswerAgree userAnswerAgree = qaService.getUserAnswerAgree(userId, answer.getId());
                                answerInfo.put("isLike", userAnswerAgree == null ? false : true);
                                answerInfoList.add(answerInfo);
                        }
                        result.put("answerInfoList", answerInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 设置最佳回答
        @ResponseBody
        @RequestMapping(value = "/setBestAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String setBestAnswer(int answerId, int questionId, int userId) {
                logger.info("qa/setBestAnswer request answerId：" + answerId + " ;questiongId:" + questionId + " ;userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        qaService.lockQuestion(questionId);
                        try {
                                qaService.lockAnswer(answerId);
                                try {
                                        Question question = qaService.getQuestion(questionId);
                                        if (question == null || question.getBestAnswerId() != null) {
                                                result.put("isSuccess", false);
                                                result.put("msg", "问题不存在");
                                                return result.toJSONString();
                                        }
                                        if (question.getUserId() != userId) {
                                                result.put("isSuccess", false);
                                                result.put("msg", "没有最佳问题");
                                                return result.toJSONString();
                                        }
                                        Answer answer = qaService.getAnswer(answerId);
                                        // 提问题的人不可以将自己的答案设置为最佳答案
                                        if (answer == null || userId == answer.getUserId()) {
                                                result.put("isSuccess", false);
                                                result.put("msg", "最佳回答是自己");
                                                return result.toJSONString();
                                        }
                                        List<Integer> userIds = new LinkedList<Integer>();
                                        userIds.add(userId);
                                        userIds.add(answer.getUserId());
                                        userService.lockUsers(userIds);
                                        try {
                                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                                User quetsionUser = userMap.get(userId);
                                                if (quetsionUser.getTicket() < question.getRewardTicket()) {
                                                        result.put("isSuccess", false);
                                                        result.put("msg", "票数不够");
                                                        return result.toJSONString();
                                                }
                                                qaService.updateQuestionBestAnswer(questionId, answerId);
                                                qaService.addAnswerTicketCount(answer, question.getRewardTicket());
                                                userService.reduceUserTicket(quetsionUser, question.getRewardTicket());
                                                User answerUser = userMap.get(answer.getUserId());
                                                userService.addUserTicket(answerUser, question.getRewardTicket());
                                                userService.addUserVote(userId, answer.getUserId(), answer.getId(), question.getRewardTicket());
                                        } finally {
                                                userService.unlockUsers(userIds);
                                        }
                                } finally {
                                        qaService.unlockAnswer(answerId);
                                }
                        } finally {
                                qaService.unlockQuestion(questionId);
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取多次提问
        @ResponseBody
        @RequestMapping(value = "/getQuestionLinkInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getQuestionLinkInfo(int questionId) {
                logger.info("qa/getQuestionLinkInfo request questiongId：" + questionId);
                JSONObject result = new JSONObject();
                try {
                        QuestionLinkRecord questionLinkRecord = qaService.getQuestionLinkRecord(questionId);
                        List<Integer> questionIds = new LinkedList<Integer>();
                        if (questionLinkRecord != null) {
                                List<QuestionLinkRecord> questionLinkRecords = qaService.getQuestionLinkRecordByFatherId(questionLinkRecord.getFatherId());
                                questionIds.addAll(Formula.listDistinct(questionLinkRecords, q -> q.getId()));
                                questionIds.add(questionLinkRecord.getFatherId());
                        } else {
                                List<QuestionLinkRecord> questionLinkRecords = qaService.getQuestionLinkRecordByFatherId(questionId);
                                questionIds.addAll(Formula.listDistinct(questionLinkRecords, q -> q.getId()));
                                questionIds.add(questionId);
                        }
                        List<Question> questions = qaService.getQuestions(questionIds);
                        JSONArray questionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject questionInfo = new JSONObject();
                                questionInfo.put("id", question.getId());
                                questionInfo.put("labels", question.getLabels());
                                questionInfo.put("title", question.getSummary());
                                questionInfo.put("content", question.getContent());
                                questionInfo.put("createTime", question.getCreateTime());
                                questionInfoList.add(questionInfo);
                        }
                        result.put("questionInfoList", questionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        //追加提问
        @ResponseBody
        @RequestMapping(value = "/addQuestionInfo",method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String addQuestionInfo(int questionId,String title,String content){
                logger.info("qa/addQuestionInfo request questionId：" + questionId);
                JSONObject result = new JSONObject();
                try {
                        Question question = qaService.getQuestion(questionId);
                        if (question==null){
                                result.put("isSuccess",false);
                                result.put("msg","问题不存在");
                                return result.toJSONString();
                        }
                        question.setSummary(title);
                        question.setContent(content);
                        Question questionInfo=qaService.addQuestion(question);
                        QuestionLinkRecord questionLinkRecord=qaService.addQuestionLinkRecord(questionInfo.getId(),questionId);
                        result.put("isSuccess",true);
                        result.put("questionId",questionLinkRecord.getFatherId());
                        result.put("questionLinkRecordId",questionLinkRecord.getId());
                        return result.toJSONString();
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 创建快问问题
        @ResponseBody
        @RequestMapping(value = "/createFastQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createFastQuestion(int userId,int subjectId, boolean isAnonymous, String summary, String pictureUrl, HttpServletRequest request) {
                logger.info("qa/createFastQuestion request userId：" + userId + " ;isAnonymous:" + isAnonymous + " ;summary:" + summary + " ;pictureURL:" + pictureUrl);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        String cityAddress = addressService.getCityAdress(request);
                        Subject subject =null;
                        if (subjectId!=0){
                                subject = subjectService.getSubject(subjectId);
                        }
                        Question question = qaService.createFastQuestion(userId, isAnonymous, summary, pictureUrl, cityAddress, subject);
                        userService.addUserQuestionCount(user);
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_1);
                        if (energyBeans.size()<=4){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_1);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        result.put("questionId", question.getId());
                        result.put("questionTitle", question.getSummary());
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 转发朋友圈
        @ResponseBody
        @RequestMapping(value = "/userForward", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public Map<String,String> userForward(String url, int userId, int questionId, int answerId ) {
                logger.info("qa/userForward request url：" + url + ";userId:" +userId + ";questionId:" +questionId+ ";answerId:"+ answerId);
                Map<String, String> map = new HashMap<String, String>();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                map.put("msg","用户不存在");
                                map.put("isSuccess","false");
                                return map;
                        }
                        String getAccessTokenPath = IConstants.WX_TOKEN_URL.replace("APPID", IConstants.APP_ID).replace("APPSECRET", IConstants.WX_APPSECRET);
                        net.sf.json.JSONObject jsonObject = WxUtil.doGetStr(getAccessTokenPath);
                        String access_token = jsonObject.getString("access_token");
                        // 根据“access_token”获取“jsapi_ticket”
                        String getTicketPath = IConstants.WX_TICKET_URL.replace("ACCESS_TOKEN", access_token);
                        net.sf.json.JSONObject jsonObject1 = WxUtil.doGetStr(getTicketPath);
                        logger.info("jsonObject1:"+jsonObject1);
                        String ticket = jsonObject1.getString("ticket");
                        Map<String, String> ret = Sign.sign(ticket, url);
                        qaService.addAnswerForwardCount(answerId);
                        qaService.addQuestionForwardCount(questionId);
                        userService.addUserForwardCount(userId);
                        return ret;
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return map;
        }
        // 微信模板消息
        @ResponseBody
        @RequestMapping(value = "/wxMessage", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String wxMessage(String openId,int type,String userName) {
                logger.info("qa/userForward request openId：" +openId );
                JSONObject result = new JSONObject();
                try {
                        String message=qaService.message(openId,type,userName);
                        String replace = IConstants.WX_TOKEN_Message_URL.replace("APPID", IConstants.APP_ID).replace("APPSECRET", IConstants.WX_APPSECRET);
                        String get = WxUtil.httpsRequest(replace, "GET", null);
                        net.sf.json.JSONObject token = net.sf.json.JSONObject.fromObject(get);
                        String access_token = token.getString("access_token");
                        String mapToken = IConstants.WX_Message_URL.replace("ACCESS_TOKEN", access_token);
                        String post = WxUtil.httpsRequest(mapToken, "POST", message);
                        net.sf.json.JSONObject msg = net.sf.json.JSONObject.fromObject(post);
                        result.put("isSuccess",msg.getString("errmsg"));
                        if ("0".equals(msg.getString("errcode"))){
                                result.put("isSuccess",true);
                                return result.toJSONString();
                        }else {
                                result.put("msg",msg.getString("errmsg"));
                                result.put("isSuccess",false);
                                return result.toJSONString();
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 排行榜转发
        @ResponseBody
        @RequestMapping(value = "/rankForward", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String rankForward(int userId) {
                logger.info("qa/rankForward request userId：" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        userService.addUserForwardCount(userId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 邀请用户回答
        @ResponseBody
        @RequestMapping(value = "/visiteUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String visiteUser(int questionId, int userId, int targetUserId) {
                logger.info("qa/visiteUser request questionId :" +questionId+ ";userId：" + userId + " ;targetUserId:" + targetUserId + " ;questionId:" + questionId);
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
                                result.put("msg", "邀请用户不存在");
                                return result.toJSONString();
                        }
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "问题不存在");
                                return result.toJSONString();
                        }
                        User2visited user2visiteds=userService.getUser2visitedByQuestion(questionId, targetUserId, userId, IConstants.ANSWER_TYPE);
                        if (user2visiteds != null) {
                                result.put("isSuccess", false);
                                result.put("msg", "已邀请用户回答");
                                return result.toJSONString();
                        }
                        userService.addUser2visited(questionId, userId, targetUserId, IConstants.ANSWER_TYPE);
                        chatService.addUserNews3(targetUserId, questionId, user.getName(), question.getSummary(),targetUser.getName(),userId);
                        result.put("isSuccess", true);
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_7);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_7);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 邀请用户提问
        @ResponseBody
        @RequestMapping(value = "/questionUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String questionUser(int questionId, int userId, int targetUserId) {
                logger.info("qa/questionUser request questionId :" +questionId+ ";userId：" + userId + " ;targetUserId:" + targetUserId );
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("msg","没有该用户");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        User2visited user2visiteds=userService.getUser2visitedByQuestion(questionId, targetUserId, userId, IConstants.QUESTION_TYPE);
                        if (user2visiteds != null) {
                                result.put("isSuccess", false);
                                result.put("msg", "已邀请用户回答");
                                return result.toJSONString();
                        }
                        userService.addUser2visited(questionId, userId, targetUserId, IConstants.QUESTION_TYPE);
                        chatService.addUserNews8(targetUserId, user.getName(), userService.getUser(targetUserId).getName(),userId);
                        result.put("isSuccess", true);
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_6);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_6);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 一键邀请用户回答,提问
        @ResponseBody
        @RequestMapping(value = "/batchVisiteUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String batchVisiteUser(int questionId, int userId, String targetUserIds, int type) {
                logger.info("qa/batchVisiteUser request userId：" + userId + " ;targetUserIds:" + targetUserIds + " ;questionId:" + questionId);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> targetIds = new LinkedList<Integer>();
                        String[] idArr = targetUserIds.split(",");
                        for (String idString : idArr) {
                            User2visited user2visitedByQuestion = userService.getUser2visitedByQuestion(questionId, Integer.valueOf(idString), userId, type);
                            if (user2visitedByQuestion==null) {
                                targetIds.add(Integer.valueOf(idString));
                            }
                        }
                        userService.addUser2visiteds(questionId, userId, targetIds, type);
                        new Thread(() -> {
                                for (Integer targetUserId : targetIds) {
                                        chatService.addUserNews3(targetUserId, questionId, userService.getUser(userId).getName(), qaService.getQuestion(questionId).getSummary(),userService.getUser(targetUserId).getName(),userId);
                                }
                        }).start();
                        if (type==0){
                            List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_6);
                            if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_6);
                                result.put("isEnergyBeanSuccess", true);
                            }else {
                                result.put("isEnergyBeanSuccess", false);
                            }
                        }else {
                            List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_7);
                            if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_7);
                                result.put("isEnergyBeanSuccess", true);
                            }else {
                                result.put("isEnergyBeanSuccess", false);
                            }
                        }
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 收藏问题
        @ResponseBody
        @RequestMapping(value = "/collectionQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String collectionQuestion(int userId, int questionId) {
                logger.info("qa/collectionQuestion request userId：" + userId + " ;questionId:" + questionId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("msg","用户不存在");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                result.put("msg","问题不存在");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        UserQuestionCollection userQuestionCollection = qaService.getUserQuestionCollection(userId, questionId);
                        if (userQuestionCollection != null) {
                                result.put("msg","用户已收藏");
                                result.put("isSuccess", false);
                                return result.toJSONString();
                        }
                        qaService.addQuestionCollectionCount(userId,questionId);
                        qaService.addUserQuestionCollection(userId, questionId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 取消收藏问题
        @ResponseBody
        @RequestMapping(value = "/cancelCollectionQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cancelCollectionQuestion(int userId, int questionId) {
                logger.info("qa/cancelCollectionQuestion request userId：" + userId + " ;questionId:" + questionId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Question question = qaService.getQuestion(questionId);
                        if (question == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "问题不存在");
                                return result.toJSONString();
                        }
                        UserQuestionCollection userQuestionCollection = qaService.getUserQuestionCollection(userId, questionId);
                        if (userQuestionCollection == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "没有收藏问题");
                                return result.toJSONString();
                        }
                        qaService.delUserQuestionCollection(userId, questionId);
                        qaService.reduceQuestionCollectionCount(questionId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 获取用户收藏
        @ResponseBody
        @RequestMapping(value = "/getCollectionQuestions", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getCollectionQuestions(int userId,int page) {
                logger.info("qa/getCollectionQuestions request userId：" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<UserQuestionCollection> userQuestionCollections = qaService.getUserQuestionCollectionsList(userId,page);
                        JSONArray collectionInfoList = new JSONArray();
                        if (!Formula.isEmptyCollection(userQuestionCollections)) {
                                List<Integer> questionIds = Formula.listDistinct(userQuestionCollections, u -> u.getQuestionId());
                                Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                                List<Integer> userIds = Formula.listDistinct(questionMap.values(), u -> u.getUserId());
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                for (UserQuestionCollection userQuestionCollection : userQuestionCollections) {
                                        Question question = questionMap.get(userQuestionCollection.getQuestionId());
                                        User user = userMap.get(question.getUserId());
                                        JSONObject collectionInfo = new JSONObject();
                                        collectionInfo.put("userId", user.getId());
                                        collectionInfo.put("userName", user.getName());
                                        collectionInfo.put("questionId", question.getId());
                                        collectionInfo.put("questionTitle", question.getSummary());
                                        collectionInfo.put("questionCreateTime", question.getCreateTime());
                                        collectionInfoList.add(collectionInfo);
                                }
                        }

                        result.put("collectionInfoList", collectionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 获取用户浏览历史
        @ResponseBody
        @RequestMapping(value = "/getUserBrowses", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserBrowses(int userId) {
                logger.info("qa/getUserBrowses request userId：" + userId);
                JSONObject result = new JSONObject();
                try {
                        List<UserBrowse> userBrowses = qaService.getUserBrowse(userId);
                        JSONArray browseInfoList = new JSONArray();
                        if (!Formula.isEmptyCollection(userBrowses)) {
                                List<Integer> questionIds = Formula.listDistinct(userBrowses, u -> u.getQuestionId());
                                Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                                List<Integer> userIds = Formula.listDistinct(questionMap.values(), u -> u.getUserId());
                                Map<Integer, User> userMap = userService.getUserMap(userIds);
                                for (UserBrowse userBrowse : userBrowses) {
                                        Question question = questionMap.get(userBrowse.getQuestionId());
                                        if (question.getStatus()==0) {
                                                User user = userMap.get(question.getUserId());
                                                JSONObject browseInfo = new JSONObject();
                                                browseInfo.put("userId", user.getId());
                                                browseInfo.put("userName", user.getName());
                                                browseInfo.put("questionId", question.getId());
                                                browseInfo.put("questionTitle", question.getSummary());
                                                browseInfo.put("questionCreateTime", userBrowse.getCreateTime());
                                                browseInfo.put("isSolve", question.getBestAnswerId() == null ? false : true);
                                                browseInfoList.add(browseInfo);
                                        }
                                }
                        }

                        result.put("browseInfoList", browseInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 点赞回答
        @ResponseBody
        @RequestMapping(value = "/agreeAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String agreeAnswer(int userId, int answerId) {
                logger.info("qa/agreeAnswer request userId：" + userId + " ;answerId:" + answerId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Answer answer = qaService.getAnswer(answerId);
                        if (answer == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "回答不存在");
                                return result.toJSONString();
                        }
                        UserAnswerAgree userAnswerAgree = qaService.getUserAnswerAgree(userId, answerId);
                        if (userAnswerAgree != null) {
                                result.put("isSuccess", false);
                                result.put("msg", "您已赞过啦~");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_3);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_3);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        qaService.addAnswerAgreeCount(userId,answerId);
                        qaService.addUserAnswerAgree(userId, answerId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 点赞评论
        @ResponseBody
        @RequestMapping(value = "/agreeComment", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String agreeComment(int userId, int commentId) {
                logger.info("qa/agreeComment request userId：" + userId + " ;commentId:" + commentId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Comment comment = qaService.getComment(commentId);
                        if (comment == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "评论不存在");
                                return result.toJSONString();
                        }
                        UserCommentAgree userCommentAgree = qaService.getUserCommentAgree(userId, commentId);
                        if (userCommentAgree != null) {
                                result.put("isSuccess", false);
                                result.put("msg", "您已评论过啦~");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_3);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_3);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        qaService.addCommentAgreeCount(userId,commentId);
                        qaService.addUserCommentAgree(userId, commentId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 点赞回复
        @ResponseBody
        @RequestMapping(value = "/agreeReply", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String agreeReply(int userId, int replyId) {
                logger.info("qa/agreeReply request userId：" + userId + " ;replyId:" + replyId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Reply reply = qaService.getReply(replyId);
                        if (reply == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "回复不存在");
                                return result.toJSONString();
                        }
                        UserReplyAgree userReplyAgree = qaService.getUserReplyAgree(userId, replyId);
                        if (userReplyAgree != null) {
                                result.put("isSuccess", false);
                                result.put("msg", "您已回复过啦~");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_3);
                        if (energyBeans.size()<=1){
                                qaService.addEnergyBean(userId,energyBeans.size(),IConstants.ENERGY_BEAN_3);
                                result.put("isEnergyBeanSuccess", true);
                        }else {
                                result.put("isEnergyBeanSuccess", false);
                        }
                        qaService.addReplyAgreeCount(userId,replyId);
                        qaService.addUserReplyAgree(userId, replyId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 取消点赞回答
        @ResponseBody
        @RequestMapping(value = "/cancelAgreeAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cancelAgreeAnswer(int userId, int answerId) {
                logger.info("qa/cancelAgreeAnswer request userId：" + userId + " ;answerId:" + answerId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Answer answer = qaService.getAnswer(answerId);
                        if (answer == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "回答不存在");
                                return result.toJSONString();
                        }
                        UserAnswerAgree userAnswerAgree = qaService.getUserAnswerAgree(userId, answerId);
                        if (userAnswerAgree == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "点赞回答不存在");
                                return result.toJSONString();
                        }
                        qaService.delUserAnswerAgree(userId, answerId);
                        qaService.reduceAnswerAgreeCount(answerId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 取消点赞评论
        @ResponseBody
        @RequestMapping(value = "/cancelAgreeComment", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cancelAgreeComment(int userId, int commentId) {
                logger.info("qa/cancelAgreeComment request userId：" + userId + " ;commentId:" + commentId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Comment comment = qaService.getComment(commentId);
                        if (comment == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "评论不存在");
                                return result.toJSONString();
                        }
                        UserCommentAgree userCommentAgree = qaService.getUserCommentAgree(userId, commentId);
                        if (userCommentAgree == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "点赞评论不存在");
                                return result.toJSONString();
                        }
                        user.setLikeCount(user.getLikeCount()-1);
                        userService.updateUser(user);
                        qaService.delUserCommentAgree(userId, commentId);
                        qaService.reduceCommentAgreeCount(commentId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 取消点赞回复
        @ResponseBody
        @RequestMapping(value = "/cancelAgreeReply", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cancelAgreeReply(int userId, int replyId) {
                logger.info("qa/cancelAgreeReply request userId：" + userId + " ;replyId:" + replyId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "用户不存在");
                                return result.toJSONString();
                        }
                        Reply reply = qaService.getReply(replyId);
                        if (reply == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "回复不存在");
                                return result.toJSONString();
                        }
                        UserReplyAgree userReplyAgree = qaService.getUserReplyAgree(userId, replyId);
                        if (userReplyAgree == null) {
                                result.put("isSuccess", false);
                                result.put("msg", "点赞回复不存在");
                                return result.toJSONString();
                        }
                        user.setLikeCount(user.getLikeCount()-1);
                        userService.updateUser(user);
                        qaService.delUserReplyAgree(userId, replyId);
                        qaService.reduceReplyAgreeCount(replyId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 推荐邀请回答
        @ResponseBody
        @RequestMapping(value = "/recommendVisite", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String recommendVisite(int userId, String labels, int page, int questionId) {
                logger.info("qa/recommendVisite request userId：" + userId + " ;labels:" + labels + " ;page:" + page +" ;questionId:" + questionId);
                JSONObject result = new JSONObject();
                try {
                        JSONArray userInfoList = new JSONArray();
                        List<User> userList = userService.getUserList(page);
                        for (User user : userList) {
                                if (user.getId()!=userId) {
                                        JSONObject userInfo = new JSONObject();
                                        userInfo.put("userId", user.getId());
                                        userInfo.put("userName", user.getName());
                                        userInfo.put("userAvatar", user.getAvatar());
                                        userInfo.put("userLevel", user.getLevel());
                                        userInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        userInfo.put("isVisited", userService.getUser2visitedByQuestion(questionId, user.getId(),userId, IConstants.ANSWER_TYPE) == null ? false : true);
                                        userInfo.put("reason", user.getLabels());
                                        userInfoList.add(userInfo);
                                }
                        }
                        result.put("userInfoList", userInfoList);
                        result.put("visitedCount", userService.getUserCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }
        // 推荐邀请提问
        @ResponseBody
        @RequestMapping(value = "/recommendVisiteQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String recommendVisiteQuestion(int userId, String labels, int page, int questionId) {
                logger.info("qa/recommendVisiteQuestion request userId：" + userId + " ;labels:" + labels + " ;page:" + page +" ;questionId:" +questionId );
                JSONObject result = new JSONObject();
                try {
                        JSONArray userInfoList = new JSONArray();
                        List<User> userList = userService.getUserList(page);
                        for (User user : userList) {
                                if (user.getId()!=userId) {
                                        JSONObject userInfo = new JSONObject();
                                        userInfo.put("userId", user.getId());
                                        userInfo.put("userName", user.getName());
                                        userInfo.put("userAvatar", user.getAvatar());
                                        userInfo.put("userLevel", user.getLevel());
                                        userInfo.put("isTeacher", user.getIdentity()==0 ? false:true);
                                        userInfo.put("isVisited", userService.getUser2visitedByQuestion(questionId, user.getId(),userId, IConstants.QUESTION_TYPE) == null ? false : true);
                                        userInfo.put("reason", user.getLabels());
                                        userInfoList.add(userInfo);
                                }
                        }
                        result.put("userInfoList", userInfoList);
                        result.put("visitedCount", userService.getUserCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }

                return result.toJSONString();
        }

        // 相关问题推荐
        @ResponseBody
        @RequestMapping(value = "/recommendQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String recommendQuestion(int questionId) {
                logger.info("qa/recommendQuestion request questionId：" + questionId);
                JSONObject result = new JSONObject();
                try {
                        Question question = qaService.getQuestion(questionId);
                        List<Question> questions = new LinkedList<Question>();
                        List<Question> questionByInfo = new LinkedList<Question>();
                        List<Question> questionList = qaService.getQuestionList();
                        for (int i = 0; i < 5; i++) {
                                Random random = new Random();
                                int j = random.nextInt(questionList.size());
                                Question questionInfo = questionList.get(j);
                                questions.add(questionInfo);
                                questionList.remove(j);
                        }
                        JSONArray questionInfoList = new JSONArray();
                        for (Question q : questions) {
                                JSONObject questionInfo = new JSONObject();
                                questionInfo.put("questionId", q.getId());
                                questionInfo.put("questionTitle", q.getSummary());
                                questionInfo.put("questionPictureUrl", q.getPictureUrl());
                                questionInfoList.add(questionInfo);
                        }
                        result.put("questionInfoList", questionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 获取关注用户提问
        @ResponseBody
        @RequestMapping(value = "/getUserQuestion", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserQuestion(int userId, int page) {
                logger.info("qa/getUserQuestion request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        List<Question> questions = new ArrayList<>();
                        List<UserAttention> userAttentionsInfo = userService.getUserAttentions(userId);
                        List<Integer> integerListInfo = Formula.listDistinct(userAttentionsInfo, u -> u.getAttentionUserId());
                        Integer questionByIdsCount = qaService.getQuestionByIdsCount(integerListInfo);
                        int pages=questionByIdsCount/20+1;
                        if (page<pages){
                            questions = qaService.getQuestionsByUserIdsPage(integerListInfo, page);
                        }
                        //question表的id
                        List<Integer> questionList = Formula.listDistinct(questions, q -> q.getId());
                        List<Integer> userListInfo = Formula.listDistinct(questions, q -> q.getUserId());
                        List<User> users = userService.getUsers(userListInfo);
                        Map<Integer, User> UserMap = Formula.list2map(users, u -> u.getId());
                        //answer表的questionId
                        Map<Integer, List<Answer>> answerMapByQuestionIds = qaService.getAnswerMapByQuestionIds(questionList);
                        //answer表的questionId
                        List<Answer> answersByQuestionIds = qaService.getAnswersByQuestionIds(questionList);
                        //answer表的userId
                        List<Integer> userIdList = Formula.listDistinct(answersByQuestionIds, a -> a.getUserId());
                        //user表的id
                        Map<Integer, User> userMap = userService.getUserMap(userIdList);
                        JSONArray userQuestionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject userQuestionInfo = new JSONObject();
                                User userInfo = UserMap.get(question.getUserId());
                                userQuestionInfo.put("questionUserAvatar", userInfo.getAvatar());
                                userQuestionInfo.put("questionUserName", userInfo.getName());
                                userQuestionInfo.put("questionUserLevel", userInfo.getLevel());
                                userQuestionInfo.put("questionUserLabels", userInfo.getLabels());
                                userQuestionInfo.put("isTeacher", userInfo.getIdentity()==0 ? false:true);
                                userQuestionInfo.put("questionId", question.getId());
                                userQuestionInfo.put("questionUserId", question.getUserId());
                                userQuestionInfo.put("questionCreateTime", question.getCreateTime());
                                userQuestionInfo.put("questionCityAddress", question.getCityAddress());
                                userQuestionInfo.put("questionTitle", question.getSummary());
                                userQuestionInfo.put("questionContent", question.getContent());
                                userQuestionInfo.put("questionPictures", question.getPictureUrl());
                                userQuestionInfo.put("questionReadCount", question.getReadCount());
                                userQuestionInfo.put("questionAnswerCount", question.getAnswerCount());
                                userQuestionInfo.put("questionCollectionCount", question.getCollectionCount());
                                userQuestionInfo.put("isAnonymous", question.getIsAnonymous());
                                userQuestionInfo.put("rewardTicker", question.getRewardTicket());
                                userQuestionInfo.put("isOnline", onlineUserTool.getOnlineUser(question.getUserId())==null ? false:true);
                                List<Answer> answerList = answerMapByQuestionIds.get(question.getId());
                                if (!Formula.isEmptyCollection(answerList)) {
                                        JSONArray answerArray = new JSONArray();
                                        for (Answer answer:answerList) {
                                                JSONObject answerInfo = new JSONObject();
                                                User userList = userMap.get(answer.getUserId());
                                                answerInfo.put("answerUserAvatar", userList.getAvatar());
                                                answerInfo.put("answerUserName", userList.getName());
                                                answerInfo.put("answerUserId", answer.getUserId());
                                                answerInfo.put("answerUserLevel", userList.getLevel());
                                                answerInfo.put("answerUserLabels", userList.getLabels());
                                                answerInfo.put("isTeacher", userList.getIdentity()==0 ? false:true);
                                                answerInfo.put("answerContent", answer.getContent());
                                                answerInfo.put("answerPictures", answer.getPictureUrl());
                                                answerInfo.put("answerAgreeCount", answer.getAgreeCount());
                                                answerInfo.put("answerCommentCount", answer.getCommentCount());
                                                answerInfo.put("answerCreateTime", answer.getCreateTime());
                                                answerInfo.put("answerCityAddress", answer.getCityAddress());
                                                answerArray.add(answerInfo);
                                        }
                                        userQuestionInfo.put("answerInfoList",answerArray);
                                }
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
        // 获取用户关注回答
        @ResponseBody
        @RequestMapping(value = "/getUserAnswer", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getUserAnswer(int userId,int page) {
                logger.info("qa/getUserAnswer request userId:" + userId );
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        List<Answer> answers = new ArrayList<>();
                        List<UserAttention> userAttentions = userService.getUserAttentionsInfo(userId);
                        List<Integer> integerList = Formula.listDistinct(userAttentions, u -> u.getAttentionUserId());
                        Integer answersByUserIdsCount = qaService.getAnswersByUserIdsCount(integerList);
                        int pages=answersByUserIdsCount/20+1;
                        if (page<pages){
                            answers = qaService.getAnswersByUserIdsPage(integerList,page);
                        }
                        List<Integer> questionList = Formula.listDistinct(answers, a -> a.getQuestionId());
                        Map<Integer, Question> questionMap = qaService.getQuestionMap(questionList);
                        Map<Integer, User> userMap = userService.getUserMap(integerList);
                        JSONArray userAnswerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject userAnswerInfo = new JSONObject();
                                userAnswerInfo.put("answerId", answer.getId());
                                userAnswerInfo.put("answerUserId", answer.getUserId());
                                userAnswerInfo.put("questionId", answer.getQuestionId());
                                userAnswerInfo.put("answerContent", answer.getContent());
                                userAnswerInfo.put("answerAgreeCount", answer.getAgreeCount());
                                userAnswerInfo.put("answerCommentCount", answer.getCommentCount());
                                userAnswerInfo.put("answerForwardCount", answer.getForwardCount());
                                userAnswerInfo.put("answerClickCount", answer.getClickCount());
                                userAnswerInfo.put("answerTicketCount", answer.getTicketCount());
                                userAnswerInfo.put("answerCityAddress", answer.getCityAddress());
                                userAnswerInfo.put("answerPictures", answer.getPictureUrl());
                                userAnswerInfo.put("answerCreateTime", answer.getCreateTime());
                                Question question = questionMap.get(answer.getQuestionId());
                                userAnswerInfo.put("questionRewardTicket", question.getRewardTicket());
                                userAnswerInfo.put("questionTitle", question.getSummary());
                                userAnswerInfo.put("questionUserId", question.getUserId());
                                User userInfo = userMap.get(answer.getUserId());
                                userAnswerInfo.put("answerUserAvatar", userInfo.getAvatar());
                                userAnswerInfo.put("answerUserName", userInfo.getName());
                                userAnswerInfo.put("answerUserLevel", userInfo.getLevel());
                                userAnswerInfo.put("answerUserLabels", userInfo.getLabels());
                                userAnswerInfo.put("isTeacher", userInfo.getIdentity()==0 ? false:true);
                                userAnswerInfo.put("isOnline", onlineUserTool.getOnlineUser(answer.getUserId())==null ? false:true);
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

    // 获取能量豆信息
    @ResponseBody
    @RequestMapping(value = "/getEnergyBeanInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String getEnergyBeanInfo(int userId) {
        logger.info("qa/getEnergyBeanInfo request userId:" + userId );
        JSONObject result = new JSONObject();
        try {
            User user = userService.getUser(userId);
            if (user==null){
                result.put("isSuccess",false);
                result.put("msg","用户不存在");
                return result.toJSONString();
            }
            List<SignIn> signIn = qaService.getSignIn(userId,0);
            List<SignIn> signInDay = qaService.getSignIn(userId,1);
            result.put("userId",user.getId());
                List<EnergyBean> energyBeanRank = qaService.getEnergyBeanRank();
                int rank=1;
                for (EnergyBean e:energyBeanRank) {
                        if (e.getUserId()==userId){
                                result.put("rankEnergyBean",rank);
                        }
                        rank++;
                }
            result.put("energyBean",user.getEnergyBean());
            if (signInDay.size()==0 || signInDay.get(0).getDay()==7){
                    result.put("signInDay",0);
            }else {
                    result.put("signInDay",signInDay.get(0).getDay());
            }
            if (signIn.size()==0){
                    result.put("isFirst",true);
            }else {
                    result.put("isFirst",false);
            }
                if (signIn.size()!=0){
                        result.put("signInDay",signIn.get(0).getDay());
                }
            result.put("question",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_1).size());
            result.put("answer",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_2).size());
            result.put("agree",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_3).size());
            result.put("comment",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_4).size());
            result.put("reply",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_5).size());
            result.put("inviteQuestions",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_6).size());
            result.put("inviteAnswers",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_7).size());
            result.put("chat",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_8).size());
            result.put("ticket",qaService.getEnergyBeanByTime(userId,IConstants.ENERGY_BEAN_9).size());
            result.put("isSuccess",true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }

        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);
    }

    // 签到
    @ResponseBody
    @RequestMapping(value = "/addSignIn", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String andSignIn(int userId, int energyBean) {
        logger.info("qa/andSignIn request userId:" + userId );
        JSONObject result = new JSONObject();
        try {
            User user = userService.getUser(userId);
            if (user==null){
                result.put("isSuccess",false);
                result.put("msg","用户不存在");
                return result.toJSONString();
            }
            List<SignIn> signIn = qaService.getSignIn(userId,0);
            if (signIn.size()>0){
                result.put("isSuccess",false);
                result.put("msg","您已签到");
                return result.toJSONString();
            }else {
                List<SignIn> yestersignIn = qaService.getSignIn(userId,1);
                user.setEnergyBean(user.getEnergyBean()+energyBean);
                if (yestersignIn.size()==0){
                    qaService.addSignIn(userId,energyBean,0);
                }else {
                    qaService.addSignIn(userId, energyBean, yestersignIn.get(0).getDay());
                }
                qaService.addEnergyBeansSignIn(userId,0,energyBean,0);
                userService.updateUser(user);
                result.put("isSuccess",true);
                return result.toJSONString();
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            logger.error(sw.toString());
        }
        return result.toJSONString();
    }
        // 能量豆明细
        @ResponseBody
        @RequestMapping(value = "/getEnergyBeanDetail", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String getEnergyBeanDetail(int userId, int page) {
                logger.info("qa/getEnergyBeanDetail request userId:" + userId +";page:"+ page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBeans=qaService.getEnergyBeans(userId,page);
                        JSONArray energyInfo = new JSONArray();
                        for (EnergyBean e:energyBeans) {
                                JSONObject energy = new JSONObject();
                                energy.put("type",e.getType());
                                energy.put("energyBean",e.getEnergyBean());
                                energy.put("createTime",e.getCreateTime());
                                energyInfo.add(energy);
                        }
                        result.put("energyBeanInfo",energyInfo);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 能量豆日排行
        @ResponseBody
        @RequestMapping(value = "/energyBeanRank", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String energyBeanRank(int userId, int page) {
                logger.info("qa/energyBeanRank request userId:" + userId +";page:"+ page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBeanRank = qaService.getEnergyBeanRank(page);
                        List<Integer> userIds = Formula.listDistinct(energyBeanRank, e -> e.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, UserAttention> userAttentionMap = userService.getUserAttentionMap(userId);
                        JSONArray array = new JSONArray();
                        int rank=1+(page-1)*20;
                        for (EnergyBean e:energyBeanRank) {
                                JSONObject energyBeanInfo = new JSONObject();
                                energyBeanInfo.put("userId",e.getUserId());
                                User userInfo = userMap.get(e.getUserId());
                                energyBeanInfo.put("userName",userInfo.getName());
                                energyBeanInfo.put("rank",userInfo.getRankEnergyBean()-rank);
                                energyBeanInfo.put("rankEnergyBean",rank);
                                energyBeanInfo.put("avatar",userInfo.getAvatar());
                                energyBeanInfo.put("energyBean",e.getEnergyBean());
                                UserAttention userAttention = userAttentionMap.get(e.getUserId());
                                energyBeanInfo.put("isAttention", userAttention == null ? false : true);
                                energyBeanInfo.put("isOnline", onlineUserTool.getOnlineUser(e.getUserId()) == null ? false : true);
                                array.add(energyBeanInfo);
                                rank++;
                        }
                        result.put("rankEnergyBean",array);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 抽奖
        @ResponseBody
        @RequestMapping(value = "/luckyDraw", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String luckyDraw(int userId) {
                logger.info("qa/luckyDraw request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        if (user==null){
                                result.put("isSuccess",false);
                                result.put("msg","用户不存在");
                                return result.toJSONString();
                        }
                        if (user.getEnergyBean()<100){
                                result.put("isSuccess",false);
                                result.put("msg","能量豆不够");
                                return result.toJSONString();
                        }
                        List<EnergyBean> energyBean = qaService.getEnergyBeanByType(userId,IConstants.ENERGY_BEAN_10);
                        while (1==1){
                                if (user.getEnergyBean()<100){
                                        result.put("isSuccess",false);
                                        result.put("msg","能量豆不够");
                                        return result.toJSONString();
                                }
                                user.setEnergyBean(user.getEnergyBean()-100);
                                userService.updateUser(user);
                                if (energyBean.size()==0){
                                    qaService.addEnergyBeans(userId,0,-100,0);
                                }else {
                                    qaService.addEnergyBeans(userId, energyBean.get(0).getNumber(), -100, 0);
                                }
                                double a=Math.random() * 1;
                                if (a<=0.25){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,1,1);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),1,1);
                                    }
                                    userService.addUserVote(0,userId,1);
                                    result.put("isSuccess",true);
                                    result.put("msg","1票");
                                    userService.addUserTicket(user, 1);
                                    return result.toJSONString();
                                }else if((a<=0.4)&&(0.25<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,2,1);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),2,1);
                                    }
                                    userService.addUserVote(0,userId,2);
                                    userService.addUserTicket(user, 2);
                                    result.put("isSuccess",true);
                                    result.put("msg","2票");
                                    return result.toJSONString();
                                }else if((a<=0.48)&&(0.4<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,5,1);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),5,1);
                                    }
                                    userService.addUserTicket(user, 5);
                                    userService.addUserVote(0,userId,5);
                                    result.put("isSuccess",true);
                                    result.put("msg","5票");
                                    return result.toJSONString();
                                }else if((a<=0.5)&&(0.48<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,10,1);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),10,1);
                                    }
                                    userService.addUserTicket(user, 10);
                                    userService.addUserVote(0,userId,10);
                                    result.put("isSuccess",true);
                                    result.put("msg","10票");
                                    return result.toJSONString();
                                }else if((a<=0.75)&&(0.5<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,20,0);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),20,0);
                                    }
                                    user.setEnergyBean(user.getEnergyBean()+20);
                                    userService.updateUser(user);
                                    rankZSetCDao.addEnergyBean(userId,20);
                                    result.put("isSuccess",true);
                                    result.put("msg","20豆");
                                    return result.toJSONString();
                                }else if((a<=0.85)&&(0.75<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,150,0);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),150,0);
                                    }
                                    user.setEnergyBean(user.getEnergyBean()+150);
                                    userService.updateUser(user);
                                    rankZSetCDao.addEnergyBean(userId,150);
                                    result.put("isSuccess",true);
                                    result.put("msg","150豆");
                                    return result.toJSONString();
                                }else if((a<=0.9)&&(0.85<a)){
                                    if (energyBean.size()==0){
                                        qaService.addEnergyBeanLuckyDraw(userId,0,200,0);
                                    }else {
                                        qaService.addEnergyBeanLuckyDraw(userId,energyBean.get(0).getNumber(),200,0);
                                    }
                                    user.setEnergyBean(user.getEnergyBean()+200);
                                    userService.updateUser(user);
                                    rankZSetCDao.addEnergyBean(userId,200);
                                    result.put("isSuccess",true);
                                    result.put("msg","200豆");
                                    return result.toJSONString();
                                }else if((a<=1)&&(0.9<a)){
                                    result.put("isSuccess",true);
                                    result.put("msg","谢谢惠顾");
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

}
