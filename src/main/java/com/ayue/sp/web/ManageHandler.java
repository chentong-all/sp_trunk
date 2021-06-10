package com.ayue.sp.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import com.ayue.sp.tools.pay.FileUtil;
import com.ayue.sp.tools.pay.OssClienUtils;
import com.github.pagehelper.Page;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.cache.vo.MsgCVO;
import com.ayue.sp.db.po.AdvertRecord;
import com.ayue.sp.db.po.Answer;
import com.ayue.sp.db.po.Comment;
import com.ayue.sp.db.po.Question;
import com.ayue.sp.db.po.Reply;
import com.ayue.sp.db.po.Subject;
import com.ayue.sp.db.po.User;
import com.ayue.sp.db.po.User2subject;
import com.ayue.sp.db.po.User2visited;
import com.ayue.sp.db.po.UserAnswerAgree;
import com.ayue.sp.db.po.UserAnswerForward;
import com.ayue.sp.db.po.UserCashout;
import com.ayue.sp.db.po.UserCommentAgree;
import com.ayue.sp.db.po.UserLoginRecord;
import com.ayue.sp.db.po.UserQuestionCollection;
import com.ayue.sp.db.po.UserQuestionForward;
import com.ayue.sp.db.po.UserRecharge;
import com.ayue.sp.db.po.UserReplyAgree;
import com.ayue.sp.db.po.UserTipoffs;
import com.ayue.sp.db.po.UserVote;
import com.ayue.sp.db.vo.AgreeRecord;
import com.ayue.sp.db.vo.ChatRecord;
import com.ayue.sp.db.vo.CollectionRecord;
import com.ayue.sp.db.vo.ForwardRecord;
import com.ayue.sp.db.vo.UserCapitalRecord;
import com.ayue.sp.service.AdvertService;
import com.ayue.sp.service.Calculator;
import com.ayue.sp.service.ChatService;
import com.ayue.sp.service.PayService;
import com.ayue.sp.service.QAService;
import com.ayue.sp.service.SubjectService;
import com.ayue.sp.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


/**
 * 2020年10月5日
 *
 * @author ayue
 */
@Controller
@RequestMapping("/manage")
public class ManageHandler {
        private Logger logger = Logger.getLogger(getClass());
        @Autowired
        private UserService userService;
        @Autowired
        private PayService payService;
        @Autowired
        private QAService qaService;
        @Autowired
        private ChatService chatService;
        @Autowired
        private SubjectService subjectService;
        @Autowired
        private Calculator calculator;
        @Autowired
        private AdvertService advertService;

        // 用户列表
        @ResponseBody
        @RequestMapping(value = "/userList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userList(int page) {
                logger.info("manage/userList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<User> users = userService.getUserList(page);
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("id", user.getId());
                                userInfo.put("userName", user.getName());
                                userInfo.put("status", user.getStatus());
                                userInfo.put("time", user.getCreateTime());
                                userInfoList.add(userInfo);
                        }
                        result.put("total",userService.getAllUser().size());
                        result.put("userInfoList", userInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户封号
        @ResponseBody
        @RequestMapping(value = "/closeUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String closeUser(int userId) {
                logger.info("manage/closeUser request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        userService.closeUser(userId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户解封
        @ResponseBody
        @RequestMapping(value = "/openUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String openUser(int userId) {
                logger.info("manage/openUser request userId:" + userId);
                JSONObject result = new JSONObject();
                try {
                        userService.openUser(userId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户搜索
        @ResponseBody
        @RequestMapping(value = "/searchUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchUser(String name) {
                logger.info("manage/searchUser request name:" + name);
                JSONObject result = new JSONObject();
                try {
                        List<User> users = userService.getUserByName(name);
                        JSONArray userInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userInfo = new JSONObject();
                                userInfo.put("id", user.getId());
                                userInfo.put("userName", user.getName());
                                userInfo.put("status", user.getStatus());
                                userInfo.put("time", user.getCreateTime());
                                userInfo.put("labels", user.getLabels());
                                userInfo.put("profile", user.getProfile());
                                userInfo.put("auditTeacher",user.getTeacher()==0 ? false:true);
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

        // 用户登录信息
        @ResponseBody
        @RequestMapping(value = "/userLoginList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userLoginList(int userId, int page) {
                logger.info("manage/userLoginList request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserLoginRecord> userLoginRecords = userService.getUserLoginRecords(userId, page);
                        JSONArray userLoginInfoList = new JSONArray();
                        for (UserLoginRecord userLoginRecord : userLoginRecords) {
                                JSONObject userLoginInfo = new JSONObject();
                                userLoginInfo.put("time", userLoginRecord.getLoginTime());
                                userLoginInfoList.add(userLoginInfo);
                        }
                        result.put("total",userService.getUserLoginRecords(userId).size());
                        result.put("userLoginInfoList", userLoginInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户资金信息
        @ResponseBody
        @RequestMapping(value = "/userCapitalList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userCapitalList(int userId, int page) {
                logger.info("manage/userCapitalList request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserCashout> userCashouts = payService.getUserCashoutsByUserId(userId);
                        List<UserRecharge> userRecharges = payService.getUserRechargesByUserId(userId);
                        List<UserCapitalRecord> userCapitalRecords = new LinkedList<UserCapitalRecord>();
                        int allCashoutAmount = 0;
                        int allRechargeAmount = 0;
                        for (UserCashout userCashout : userCashouts) {
                                allCashoutAmount += userCashout.getMoneyCount();
                                userCapitalRecords.add(new UserCapitalRecord(userCashout));
                        }
                        for (UserRecharge userRecharge : userRecharges) {
                                allRechargeAmount += userRecharge.getMoneyCount();
                                userCapitalRecords.add(new UserCapitalRecord(userRecharge));
                        }

                        userCapitalRecords.sort((e1, e2) -> {
                                if ((e1.getDate().getTime() > e2.getDate().getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<UserCapitalRecord> resultCapitalRecords = new PageInfo<UserCapitalRecord>(userCapitalRecords).getList();
                        JSONArray userCapitalInfoList = new JSONArray();
                        for (UserCapitalRecord userCapitalRecord : resultCapitalRecords) {
                                JSONObject userCapitalInfo = new JSONObject();
                                userCapitalInfo.put("time", userCapitalRecord.getDate());
                                userCapitalInfo.put("moneyCount", userCapitalRecord.getMoneyCount());
                                userCapitalInfo.put("type", userCapitalRecord.getType());// 0:提现，1：充值
                                userCapitalInfoList.add(userCapitalInfo);
                        }
                        result.put("total",userCashouts.size()+userRecharges.size());
                        result.put("allRechargeAmount", allRechargeAmount);
                        result.put("allCashoutAmount", allCashoutAmount);
                        result.put("userCapitalInfoList", userCapitalInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户票数信息
        @ResponseBody
        @RequestMapping(value = "/userTicketList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userTicketList(int userId, int page) {
                logger.info("manage/userTicketList request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        User user = userService.getUser(userId);
                        List<UserVote> userVotes = new LinkedList<UserVote>();
                        userVotes.addAll(userService.getUserVoteByUserId(userId));
                        userVotes.addAll(userService.getUserVoteByTargetUserId(userId));
                        int allGainVotes = 0;
                        int allCastVotes = 0;
                        userVotes.sort((e1, e2) -> {
                                if ((e1.getCreateTime().getTime() > e2.getCreateTime().getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<UserVote> resultVoteRecords = new PageInfo<UserVote>(userVotes).getList();
                        JSONArray userTicketInfoList = new JSONArray();
                        for (UserVote userVote : resultVoteRecords) {
                                JSONObject userTicketInfo = new JSONObject();
                                userTicketInfo.put("time", userVote.getCreateTime());
                                userTicketInfo.put("ticketCount", userVote.getCount());
                                if (userId == userVote.getUserId()) {
                                        userTicketInfo.put("type", 0);// 0:投票，1：得票，2：充值票数
                                        allCastVotes += userVote.getCount();
                                } else{
                                        userTicketInfo.put("type", 1);// 0:投票，1：得票，2：充值票数
                                        allGainVotes += userVote.getCount();
                                }
                                userTicketInfoList.add(userTicketInfo);
                        }
                        result.put("total",userVotes.size());
                        result.put("userTicketInfoList", userTicketInfoList);
                        result.put("allCastVotes", allCastVotes);
                        result.put("allGainVotes", allGainVotes);
                        result.put("allVotes", user.getTicket());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户聊天信息
        @ResponseBody
        @RequestMapping(value = "/userChatList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userChatList(int userId, int page) {
                logger.info("manage/userChatList request userId:" + userId + " ;page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Integer> userIds = chatService.getAllChatUserList(userId);
                        if (userIds.size()<1){
                                result.put("isSuccess",false);
                                result.put("msg","没有聊天对象");
                                return result.toJSONString();
                        }
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        Map<Integer, User> userMap1 = userService.getUserMap(userIds);

                        List<User> userList=userService.getUserPage(userIds,page);
                        int allChatMsgCount = 0;
                        JSONArray userChatInfoList = new JSONArray();
                        for (User user : userList) {
                                JSONObject userChatInfo = new JSONObject();
                                String chatId = chatService.getChatId(userId, user.getId());
                                MsgCVO msgCVO = chatService.getChatLastMsgCVo(chatId);
                                userChatInfo.put("time", msgCVO.getSendTime());
                                userChatInfo.put("name", user.getName());
                                List<MsgCVO> msgCVOs = chatService.getChatMsgCVo(chatId, 0);
                                userChatInfo.put("chatMsgCount", msgCVOs.size());
                                userChatInfoList.add(userChatInfo);
                                allChatMsgCount += msgCVOs.size();
                        }
                        result.put("total",userMap.size());
                        result.put("userChatInfoList", userChatInfoList);
                        List<UserLoginRecord> userLoginRecords = userService.getUserLoginRecords(userId);
                        long allLoginTime = userLoginRecords.stream().mapToLong(UserLoginRecord::getDuration).sum();
                        result.put("allLoginTime", allLoginTime);
                        result.put("allChatMsgCount", allChatMsgCount);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户内容信息
        @ResponseBody
        @RequestMapping(value = "/userQAList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userQAList(int page) {
                logger.info("manage/userQAList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<User> users = userService.getUserList(page);
                        List<Integer> userIds = Formula.listDistinct(users, u -> u.getId());
                        Map<Integer, List<Question>> questiontMap = qaService.getQuestionMapByUserIds(userIds);
                        Map<Integer, List<Answer>> answerMap = qaService.getAnswerMapByUserIds(userIds,0);
                        Map<Integer, List<Answer>> answerMapAdopt = qaService.getAnswerMapByUserIds(userIds,1);
                        Map<Integer, List<Comment>> commentMap = qaService.getCommentMapByUserIds(userIds);
                        Map<Integer, List<Reply>> replyMap = qaService.getReplyMapByUserIds(userIds);
                        JSONArray userQAInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userQAInfo = new JSONObject();
                                userQAInfo.put("userId", user.getId());
                                userQAInfo.put("time", user.getCreateTime());
                                userQAInfo.put("name", user.getName());
                                userQAInfo.put("questionCount", questiontMap.get(user.getId()) == null ? 0 : questiontMap.get(user.getId()).size());
                                userQAInfo.put("answerCount", answerMap.get(user.getId()) == null ? 0 : answerMap.get(user.getId()).size());
                                userQAInfo.put("commentCount", commentMap.get(user.getId()) == null ? 0 : commentMap.get(user.getId()).size());
                                userQAInfo.put("replyCount", replyMap.get(user.getId()) == null ? 0 : replyMap.get(user.getId()).size());
                                userQAInfo.put("answerAdoptCount", answerMapAdopt.get(user.getId()) == null ? 0 : answerMapAdopt.get(user.getId()).size());
                                userQAInfoList.add(userQAInfo);
                        }
                        result.put("total",userService.getAllUser().size());
                        result.put("userQAInfoList", userQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 搜索用户的内容信息
        @ResponseBody
        @RequestMapping(value = "/searchUserQA", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchUserQA(String name) {
                logger.info("manage/searchUserQA request name:" + name);
                JSONObject result = new JSONObject();
                try {
                        List<User> users = userService.getUserByName(name);
                        List<Integer> userIds = Formula.listDistinct(users, u -> u.getId());
                        Map<Integer, List<Question>> questiontMap = qaService.getQuestionMapByUserIds(userIds);
                        Map<Integer, List<Answer>> answerMap = qaService.getAnswerMapByUserIds(userIds,0);
                        Map<Integer, List<Comment>> commentMap = qaService.getCommentMapByUserIds(userIds);
                        Map<Integer, List<Reply>> replyMap = qaService.getReplyMapByUserIds(userIds);
                        Map<Integer, List<Answer>> answerMapAdopt = qaService.getAnswerMapByUserIds(userIds,1);
                        JSONArray userQAInfoList = new JSONArray();
                        for (User user : users) {
                                JSONObject userQAInfo = new JSONObject();
                                userQAInfo.put("userId", user.getId());
                                userQAInfo.put("time", user.getCreateTime());
                                userQAInfo.put("name", user.getName());
                                userQAInfo.put("questionCount", questiontMap.get(user.getId()) == null ? 0 : questiontMap.get(user.getId()).size());
                                userQAInfo.put("answerCount", answerMap.get(user.getId()) == null ? 0 : answerMap.get(user.getId()).size());
                                userQAInfo.put("commentCount", commentMap.get(user.getId()) == null ? 0 : commentMap.get(user.getId()).size());
                                userQAInfo.put("replyCount", replyMap.get(user.getId()) == null ? 0 : replyMap.get(user.getId()).size());
                                userQAInfo.put("answerAdoptCount", answerMapAdopt.get(user.getId()) == null ? 0 : answerMapAdopt.get(user.getId()).size());
                                userQAInfoList.add(userQAInfo);
                        }
                        result.put("userQAInfoList", userQAInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户提问记录
        @ResponseBody
        @RequestMapping(value = "/userQuestionList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userQuestionList(int userId, int page) {
                logger.info("manage/userQuestionList request userId:" + userId + ";page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getAllQuestionsByUserId(userId, page);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        Map<Integer, List<User2visited>> user2visitedMap = userService.getAllUser2visitedMap(questionIds);
                        JSONArray userQuestionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject userQusetionInfo = new JSONObject();
                                userQusetionInfo.put("id", question.getId());
                                userQusetionInfo.put("time", question.getCreateTime());
                                userQusetionInfo.put("answerCount", question.getAnswerCount());
                                userQusetionInfo.put("collectionCount",question.getCollectionCount());
                                List<User2visited> user2visiteds = user2visitedMap.get(question.getId());
                                userQusetionInfo.put("visitedCount", Formula.isEmptyCollection(user2visiteds) ? 0 : user2visiteds.size());
                                userQusetionInfo.put("title", question.getSummary());
                                userQusetionInfo.put("status",question.getStatus());
                                userQuestionInfoList.add(userQusetionInfo);
                        }
                        result.put("total",qaService.getQuestionUserIdCount(userId));
                        result.put("userQuestionInfoList", userQuestionInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户回答记录
        @ResponseBody
        @RequestMapping(value = "/userAnswerList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userAnswerList(int userId, int page) {
                logger.info("manage/userAnswerList request userId:" + userId + ";page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Answer> answers = qaService.getAnswersByUserId(userId, page);
                        JSONArray userAnswerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject userAnswerInfo = new JSONObject();
                                userAnswerInfo.put("id", answer.getId());
                                userAnswerInfo.put("time", answer.getCreateTime());
                                userAnswerInfo.put("content", answer.getContent());
                                userAnswerInfo.put("commentCount", answer.getCommentCount());
                                userAnswerInfo.put("agreeCount", answer.getAgreeCount());
                                userAnswerInfo.put("forwardCount", answer.getForwardCount());
                                userAnswerInfo.put("status",answer.getStatus());
                                userAnswerInfoList.add(userAnswerInfo);
                        }
                        result.put("total",qaService.getuserAnswers(userId).size());
                        result.put("userAnswerInfoList", userAnswerInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户评论记录
        @ResponseBody
        @RequestMapping(value = "/userCommentList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userCommentList(int userId, int page) {
                logger.info("manage/userCommentList request userId:" + userId + ";page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Comment> comments = qaService.getCommentsByUserId(userId, page);
                        JSONArray userCommentInfoList = new JSONArray();
                        for (Comment comment : comments) {
                                JSONObject userCommentInfo = new JSONObject();
                                userCommentInfo.put("id", comment.getId());
                                userCommentInfo.put("time", comment.getCreateTime());
                                userCommentInfo.put("content", comment.getContent());
                                userCommentInfo.put("replyCount", comment.getReplyCount());
                                userCommentInfo.put("agreeCount", comment.getAgreeCount());
                                userCommentInfo.put("status",comment.getStatus());
                                userCommentInfoList.add(userCommentInfo);
                        }
                        result.put("total",qaService.getCommentUserIdCount(userId));
                        result.put("userCommentInfoList", userCommentInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户回复记录
        @ResponseBody
        @RequestMapping(value = "/userReplyList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userReplyList(int userId, int page) {
                logger.info("manage/userReplyList request userId:" + userId + ";page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Reply> replys = qaService.getReplyByUserId(userId, page);
                        JSONArray userReplyInfoList = new JSONArray();
                        for (Reply reply : replys) {
                                JSONObject useReolyInfo = new JSONObject();
                                useReolyInfo.put("id", reply.getId());
                                useReolyInfo.put("time", reply.getCreateTime());
                                useReolyInfo.put("content", reply.getContent());
                                useReolyInfo.put("agreeCount", reply.getAgreeCount());
                                useReolyInfo.put("status",reply.getStatus());
                                userReplyInfoList.add(useReolyInfo);
                        }
                        result.put("total",qaService.getReplyUserIdCount(userId));
                        result.put("userReplyInfoList", userReplyInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 提问上下架状态
        @ResponseBody
        @RequestMapping(value = "/questionStatus", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String questionStatus(int questionId){
                logger.info("manage/questionStatus request page:" + questionId);
                JSONObject result = new JSONObject();
                try{
                        Question question = qaService.getQuestion(questionId);
                        if (question.getStatus()==1){
                                qaService.updateQuestionOnline(questionId);
                                result.put("isSuccess",true);
                                result.put("id",question.getId());
                                result.put("status",question.getStatus());
                        }else {
                                qaService.updateQuestionOffline(questionId);
                                result.put("isSuccess",true);
                                result.put("id",question.getId());
                                result.put("status",question.getStatus());
                        }

                }catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 回答上下架状态
        @ResponseBody
        @RequestMapping(value = "/answerStatus", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String answerStatus(int answerId){
                logger.info("manage/answerStatus request page:" + answerId);
                JSONObject result = new JSONObject();
                try{
                        Answer answer = qaService.getAnswer(answerId);
                        if (answer.getStatus()==1){
                                qaService.updateAnswerOnline(answerId);
                                result.put("isSuccess",true);
                                result.put("id",answer.getId());
                                result.put("status",answer.getStatus());
                        }else {
                                qaService.updateAnswerOffline(answerId);
                                result.put("isSuccess",true);
                                result.put("id",answer.getId());
                                result.put("status",answer.getStatus());
                        }

                }catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 评论上下架状态
        @ResponseBody
        @RequestMapping(value = "/commentStatus", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String commentStatus(int commentId){
                logger.info("manage/commentStatus request page:" + commentId);
                JSONObject result = new JSONObject();
                try{
                        Comment comment = qaService.getComment(commentId);
                        if (comment.getStatus()==1){
                                qaService.updateCommentOnline(commentId);
                                result.put("isSuccess",true);
                                result.put("id",comment.getId());
                                result.put("status",comment.getStatus());
                        }else {
                                qaService.updateCommentOffline(commentId);
                                result.put("isSuccess",true);
                                result.put("id",comment.getId());
                                result.put("status",comment.getStatus());
                        }

                }catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 回复上下架状态
        @ResponseBody
        @RequestMapping(value = "/replyStatus", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String replyStatus(int replyId){
                logger.info("manage/replyStatus request page:" + replyId);
                JSONObject result = new JSONObject();
                try{
                        Reply reply = qaService.getReply(replyId);
                        if (reply.getStatus()==1){
                                qaService.updateReplyOnline(replyId);
                                result.put("isSuccess",true);
                                result.put("id",reply.getId());
                                result.put("status",reply.getStatus());
                        }else {
                                qaService.updateReplyOffline(replyId);
                                result.put("isSuccess",true);
                                result.put("id",reply.getId());
                                result.put("status",reply.getStatus());
                        }

                }catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 充值记录
        @ResponseBody
        @RequestMapping(value = "/rechargeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String rechargeList(int page) {
                logger.info("manage/rechargeList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserRecharge> userRecharges = payService.getUserRecharges(page);
                        List<Integer> userIds = Formula.listDistinct(userRecharges, u -> u.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray rechargeInfoList = new JSONArray();
                        for (UserRecharge userRecharge : userRecharges) {
                                JSONObject rechargeInfo = new JSONObject();
                                rechargeInfo.put("time", userRecharge.getCreateTime());
                                rechargeInfo.put("moneyCount", userRecharge.getMoneyCount());
                                rechargeInfo.put("status", userRecharge.getStatus());
                                rechargeInfo.put("userName", userMap.get(userRecharge.getUserId()).getName());
                                rechargeInfoList.add(rechargeInfo);
                        }
                        result.put("total",payService.getUserRechargeCount());
                        result.put("rechargeInfoList", rechargeInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        //搜索充值人员
        @ResponseBody
        @RequestMapping(value = "/searchRechargeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchRechargeList(String userName,int page) {
                logger.info("manage/searchRechargeList request userName:" + userName);
                JSONObject result = new JSONObject();
                try {
                        List<User> userList=userService.getUserByName(userName);
                        List<Integer> userIds = Formula.listDistinct(userList, u -> u.getId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<UserRecharge> userRecharges =payService.getUserRechargeByUserIds(userIds,page);
                        JSONArray rechargeInfoList = new JSONArray();
                        for (UserRecharge userRecharge : userRecharges) {
                                JSONObject rechargeInfo = new JSONObject();
                                rechargeInfo.put("time", userRecharge.getCreateTime());
                                rechargeInfo.put("moneyCount", userRecharge.getMoneyCount());
                                rechargeInfo.put("status", userRecharge.getStatus());
                                rechargeInfo.put("userName", userMap.get(userRecharge.getUserId()).getName());
                                rechargeInfoList.add(rechargeInfo);
                        }
                        result.put("total",payService.getUserRechargeList(userIds).size());
                        result.put("rechargeInfoList", rechargeInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 提现记录
        @ResponseBody
        @RequestMapping(value = "/cashoutList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String cashoutList(int page) {
                logger.info("manage/cashoutList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserCashout> userCashouts = payService.getUserCashouts(page);
                        List<Integer> userIds = Formula.listDistinct(userCashouts, u -> u.getUserId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray cashoutInfoList = new JSONArray();
                        for (UserCashout userCashout : userCashouts) {
                                JSONObject cashoutInfo = new JSONObject();
                                cashoutInfo.put("time", userCashout.getCreateTime());
                                cashoutInfo.put("moneyCount", userCashout.getMoneyCount());
                                cashoutInfo.put("isSuccess", userCashout.getIsSuccess());
                                cashoutInfo.put("userName", userMap.get(userCashout.getUserId()).getName());
                                cashoutInfoList.add(cashoutInfo);
                        }
                        result.put("total",payService.getUserCashoutCount());
                        result.put("cashoutInfoList", cashoutInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        //搜索提现人员
        @ResponseBody
        @RequestMapping(value = "/searchCashoutList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchCashoutList(String userName,int page) {
                logger.info("manage/searchCashoutList request userName:" + userName);
                JSONObject result = new JSONObject();
                try {
                        List<User> userList=userService.getUserByName(userName);
                        List<Integer> userIds = Formula.listDistinct(userList, u -> u.getId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<UserCashout> userCashouts =payService.getUserCashoutByUserIds(userIds, page);
                        JSONArray cashoutInfoList = new JSONArray();
                        for (UserCashout userCashout : userCashouts) {
                                JSONObject cashoutInfo = new JSONObject();
                                cashoutInfo.put("time", userCashout.getCreateTime());
                                cashoutInfo.put("moneyCount", userCashout.getMoneyCount());
                                cashoutInfo.put("isSuccess", userCashout.getIsSuccess());
                                cashoutInfo.put("userName", userMap.get(userCashout.getUserId()).getName());
                                cashoutInfoList.add(cashoutInfo);
                        }
                        result.put("total",payService.getUserCashoutList(userIds).size());
                        result.put("cashoutInfoList", cashoutInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 投票记录
        @ResponseBody
        @RequestMapping(value = "/ticketList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String ticketList(int page) {
                logger.info("manage/ticketList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserVote> userVotes = userService.getUserVotes(page);
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(userVotes, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(userVotes, u -> u.getTargetUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray voteInfoList = new JSONArray();
                        for (UserVote userVote : userVotes) {
                                JSONObject voteInfo = new JSONObject();
                                voteInfo.put("time", userVote.getCreateTime());
                                voteInfo.put("ticketCount", userVote.getCount());
                                User voteUser = userMap.get(userVote.getUserId());
                                User targetUser = userMap.get(userVote.getTargetUserId());
                                if (voteUser!=null){
                                        voteInfo.put("voteUserName", voteUser.getName());
                                }else {
                                        voteInfo.put("voteUserName", "抽奖获得");
                                }
                                if (targetUser!=null){
                                        voteInfo.put("targetUserName", targetUser.getName());
                                }
                                voteInfoList.add(voteInfo);
                        }
                        result.put("total",userService.getUserVoteCount());
                        result.put("voteInfoList", voteInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        //搜索投票人员
        @ResponseBody
        @RequestMapping(value = "/searchTicketList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchTicketList(String userName,int page) {
                logger.info("manage/searchCashoutList request userName:" + userName);
                JSONObject result = new JSONObject();
                try {
                        List<User> userList=userService.getUserByName(userName);
                        List<Integer> userIds = Formula.listDistinct(userList, u -> u.getId());
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<UserVote> userVoteUserId =userService.getUserVoteByUserIds(userIds, page);
                        JSONArray voteInfoList = new JSONArray();
                        for (UserVote userVote : userVoteUserId) {
                                JSONObject voteInfo = new JSONObject();
                                voteInfo.put("time", userVote.getCreateTime());
                                voteInfo.put("ticketCount", userVote.getCount());
                                User voteUser = userMap.get(userVote.getUserId());
                                if (voteUser!=null){
                                        voteInfo.put("voteUserName", voteUser.getName());
                                }
                                User targetUser = userMap.get(userVote.getTargetUserId());
                                if (targetUser!=null){
                                        voteInfo.put("targetUserName", targetUser.getName());
                                }
                                voteInfoList.add(voteInfo);
                        }
                        result.put("total",userService.getUserVoteList(userIds).size());
                        result.put("voteInfoList", voteInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 系统创建专题
        @ResponseBody
        @RequestMapping(value = "/createSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String createSubject(byte type, String name, String avatar, String description) {
                logger.info("manage/createSubject request type:" + type + " ;name:" + name + " ;avatar;" + avatar + " ;description:" + description);
                JSONObject result = new JSONObject();
                try {
                        Subject subject = subjectService.createSystemSubject(type, name, avatar, description);
                        result.put("subjectId", subject.getId());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        // 修改专题
        @ResponseBody
        @RequestMapping(value = "/updateSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String updateSubjcet(String name, String avatar, String description, int subjectId) {
                logger.info("manage/updateSubjcet request name:" + name + " ;avatar;" + avatar + " ;description:" + description);
                JSONObject result = new JSONObject();
                try {
                        subjectService.updateSubjectInfo(name, avatar, description, subjectId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        result.put("isSuccess", false);
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 用户专题
        @ResponseBody
        @RequestMapping(value = "/userSubjectList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String userSubjectList(int page,int isSystem) {
                logger.info("manage/userSubjectList request page:" +page);
                JSONObject result = new JSONObject();
                try {
                        List<Subject> subjects = subjectService.getSubjects(page,isSystem);
                        List<Integer> subjectIds = Formula.listDistinct(subjects, s -> s.getId());
                        Map<Integer, List<User2subject>> user2subjectMap = subjectService.getUser2subjectMapBySubjectIds(subjectIds);
                        Map<Integer, List<Question>> questionMap = qaService.getAllQuestionMapBySubjectIds(subjectIds);
                        JSONArray subjectInfoList = new JSONArray();
                        for (Subject subject : subjects) {
                                JSONObject subjectInfo = new JSONObject();
                                subjectInfo.put("id", subject.getId());
                                subjectInfo.put("time", subject.getCreateTime());
                                subjectInfo.put("name", subject.getName());
                                subjectInfo.put("type", subject.getType());
                                subjectInfo.put("isSystem", subject.getIsSystem());
                                List<User2subject> user2subjects = user2subjectMap.get(subject.getId());
                                subjectInfo.put("userCount", Formula.isEmptyCollection(user2subjects) ? 0 : user2subjects.size());
                                List<Question> questions = questionMap.get(subject.getId());
                                subjectInfo.put("questionCount", Formula.isEmptyCollection(questions) ? 0 : questions.size());
                                subjectInfo.put("description", subject.getDescription());
                                subjectInfo.put("status", subject.getStatus());
                                subjectInfo.put("avatar", subject.getAvatar());
                                subjectInfoList.add(subjectInfo);
                        }
                        result.put("total",subjectService.getSubjectCount());
                        result.put("subjectInfoList", subjectInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 专题封号
        @ResponseBody
        @RequestMapping(value = "/closeSubjcet", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String closeSubjcet(int subjectId) {
                logger.info("manage/closeSubjcet request subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        subjectService.closeSubject(subjectId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 专题解封
        @ResponseBody
        @RequestMapping(value = "/openSubject", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String openSubject(int subjectId) {
                logger.info("manage/openSubject request subjectId:" + subjectId);
                JSONObject result = new JSONObject();
                try {
                        subjectService.openSubject(subjectId);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 专题搜索
        @ResponseBody
        @RequestMapping(value = "/searchSubjcet", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchSubjcet(String name) {
                logger.info("manage/searchSubjcet request name:" + name);
                JSONObject result = new JSONObject();
                try {
                        List<Subject> subjects = subjectService.getSubjectByName(name);
                        List<Integer> subjectIds = Formula.listDistinct(subjects, s -> s.getId());
                        Map<Integer, List<User2subject>> user2subjectMap = subjectService.getUser2subjectMapBySubjectIds(subjectIds);
                        Map<Integer, List<Question>> questionMap = qaService.getAllQuestionMapBySubjectIds(subjectIds);
                        JSONArray subjectInfoList = new JSONArray();
                        for (Subject subject : subjects) {
                                JSONObject subjectInfo = new JSONObject();
                                subjectInfo.put("id", subject.getId());
                                subjectInfo.put("time", subject.getCreateTime());
                                subjectInfo.put("name", subject.getName());
                                List<User2subject> user2subjects = user2subjectMap.get(subject.getId());
                                subjectInfo.put("userCount", Formula.isEmptyCollection(user2subjects) ? 0 : user2subjects.size());
                                List<Question> questions = questionMap.get(subject.getId());
                                subjectInfo.put("questionCount", Formula.isEmptyCollection(questions) ? 0 : questions.size());
                                subjectInfo.put("description", subject.getDescription());
                                subjectInfo.put("status", subject.getStatus());
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



        // 举报信息
        @ResponseBody
        @RequestMapping(value = "/tipoffList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String tipoffList(int page) {
                logger.info("manage/tipoffList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserTipoffs> userTipoffs = userService.getUserTipoffs(page);
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(userTipoffs, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(userTipoffs, u -> u.getTargetUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray tipoffInfoList = new JSONArray();
                        for (UserTipoffs userTipoff : userTipoffs) {
                                JSONObject tipoffInfo = new JSONObject();
                                User user = userMap.get(userTipoff.getUserId());
                                User targetUser = userMap.get(userTipoff.getTargetUserId());
                                tipoffInfo.put("userName", user.getName());
                                tipoffInfo.put("targetUserName", targetUser.getName());
                                tipoffInfo.put("userId", userTipoff.getUserId());
                                tipoffInfo.put("targetUserId", userTipoff.getTargetUserId());
                                tipoffInfo.put("time", userTipoff.getCreateTime());
                                tipoffInfo.put("type", userTipoff.getType());
                                tipoffInfo.put("picture", userTipoff.getPictureUrl());
                                tipoffInfo.put("description", userTipoff.getDescription());
                                tipoffInfo.put("isDealed", userTipoff.getIsDealed());
                                tipoffInfoList.add(tipoffInfo);
                        }
                        result.put("total",userService.getUserTipoffsCount());
                        result.put("tipoffInfoList", tipoffInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
        //处理举报消息
        @ResponseBody
        @RequestMapping(value = "/dealedTipoff", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String dealedTipoff(int userId,int targetUserId){
                logger.info("manage/dealedTipoff request userId:" + userId+",targetUserId:"+targetUserId);
                JSONObject result = new JSONObject();
                try {
                        UserTipoffs userTipoffs = userService.getUserTipoffs(targetUserId, userId);
                        if (userTipoffs==null){
                                result.put("isSuccess",false);
                                result.put("msg","没有举报消息");
                                return result.toJSONString();
                        }
                        userService.dealedTipoff(userId,targetUserId);
                        result.put("isSuccess", true);
        } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw, true));
                logger.error(sw.toString());
        }
                return result.toJSONString();
        }

        // 发布系统消息
        @ResponseBody
        @RequestMapping(value = "/sendNews", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String sendNews(String title, String content, int type) {
                logger.info("manage/sendNews request title:" + title + " ;content:" + content);
                JSONObject result = new JSONObject();
                try {
                        if (type == 0) {
                                calculator.sendSystemNews(title, content);
                        }else {
                                List<User> allUser = userService.getAllUser();
                                for (User user:allUser) {
                                     if (user.getIdentity()==1) {
                                             chatService.addUserNews7(user.getId(),title,content);
                                     }
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
        // 发布单个消息
        @ResponseBody
        @RequestMapping(value = "/sendUserNews", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String sendUserNews(int userId, String title, String content) {
                logger.info("manage/sendUserNews request userId:"+userId+";title:" + title + " ;content:" + content);
                JSONObject result = new JSONObject();
                try {
                        chatService.addUserNews7(userId,title,content);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 数据列表
        @ResponseBody
        @RequestMapping(value = "/dataList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String dataList() {
                logger.info("manage/dataList request");
                JSONObject result = new JSONObject();
                try {
                        result.put("allUserCount", userService.getUserCount());
                        result.put("todayUserCount", userService.getTodayUserCount());
                        result.put("todayVisitorCount", userService.getTodayUserCounts());
                        result.put("allRechargeCount", payService.getRechargeCount());
                        result.put("todayRechargeCount", payService.getTodayRechargeCount());
                        result.put("allCashoutCount", payService.getCashoutCount());
                        result.put("todayCashoutCount", payService.getTodayCashoutCount());
                        result.put("allQuestionCount", qaService.getQuestionCount());
                        result.put("todayQuestionCount", qaService.getTodayQuestionCount());
                        result.put("allAnswerCount", qaService.getAnswerCount());
                        result.put("todayAnswerCount", qaService.getTodayAnswerCount());
                        result.put("allCommentCount", qaService.getCommentCount());
                        result.put("todayCommentCount", qaService.getTodayCommentCount());
                        result.put("allReplyCount", qaService.getReplyCount());
                        result.put("todayReplyCount", qaService.getTodayReplyCount());
                        result.put("allTicketCount", userService.getTicketCount());
                        result.put("todayTicketCount", userService.getTodayTicketCount());
                        result.put("allSubjectCount", subjectService.getSubjectCount());
                        result.put("todaySubjectCount", subjectService.getTodaySubjectCount());
                        result.put("allAgreeCount", qaService.getAgreeCount());
                        result.put("todayAgreeCount", qaService.getTodayAgreeCount());
                        result.put("allForwardCount", qaService.getForwardCount());
                        result.put("todayForwardCount", qaService.getTodayForwardCount());
                        result.put("allCollectionCount", qaService.getCollectionCount());
                        result.put("todayCollectionCount", qaService.getTodayCollectionCount());
                        result.put("allChatCount", chatService.getChatCount());
                        result.put("todayChatCount", chatService.getTodayChatCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 提问记录
        @ResponseBody
        @RequestMapping(value = "/questionList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String questionList(int page) {
                logger.info("manage/questionList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Question> questions = qaService.getAllQuestions(page);
                        List<Integer> questionIds = Formula.listDistinct(questions, q -> q.getId());
                        List<Integer> collect = questions.stream().map(Question::getUserId).collect(Collectors.toList());
                        List<User> userName=userService.getUserName(collect);
                        Map<Integer, String> userMap = userName.stream().collect(Collectors.toMap(User::getId, User::getName));
                        Map<Integer, List<Answer>> answerMap = qaService.getAnswerMapByQuestionIds(questionIds);
                        List<Integer> answerIds = new LinkedList<Integer>();
                        for (List<Answer> answers : answerMap.values()) {
                                answerIds.addAll(Formula.listDistinct(answers, a -> a.getId()));
                        }
                        Map<Integer, List<Comment>> commentMap = qaService.getCommentMapByAnswerIds(answerIds);
                        List<Integer> commentIds = new LinkedList<Integer>();
                        for (List<Comment> comments : commentMap.values()) {
                                commentIds.addAll(Formula.listDistinct(comments, c -> c.getId()));
                        }
                        Map<Integer, List<Reply>> replyMap = qaService.getReplyMapByCommentIds(commentIds);
                        JSONArray questionInfoList = new JSONArray();
                        for (Question question : questions) {
                                JSONObject qusetionInfo = new JSONObject();
                                qusetionInfo.put("id", question.getId());
                                qusetionInfo.put("title", question.getSummary());
                                qusetionInfo.put("answerCount", question.getAnswerCount());
                                qusetionInfo.put("username",userMap.get(question.getUserId()));
                                List<Answer> answers = answerMap.get(question.getId());
                                int commentCount = 0;
                                int replyCount = 0;
                                if (answers!=null){
                                        for (Answer answer : answers) {
                                                List<Comment> comments = commentMap.get(answer.getId());
                                                if (!Formula.isEmptyCollection(comments)) {
                                                        commentCount += comments.size();
                                                        for (Comment comment : comments) {
                                                                List<Reply> replies = replyMap.get(comment.getId());
                                                                if (!Formula.isEmptyCollection(replies)) {
                                                                        replyCount += replies.size();
                                                                }
                                                        }
                                                }
                                        }
                                }
                                qusetionInfo.put("commentCount", commentCount);
                                qusetionInfo.put("replyCount", replyCount);
                                questionInfoList.add(qusetionInfo);
                        }
                        result.put("questionInfoList", questionInfoList);
                        result.put("todayQuestionCount", qaService.getTodayQuestionCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 回答记录
        @ResponseBody
        @RequestMapping(value = "/answerList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String answerList(int page) {
                logger.info("manage/answerList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Answer> answers = qaService.getAnswers(page);
                        List<Integer> answerIds = Formula.listDistinct(answers, a -> a.getId());
                        Map<Integer, List<Comment>> commentMap = qaService.getCommentMapByAnswerIds(answerIds);
                        List<Integer> commentIds = new LinkedList<Integer>();
                        for (List<Comment> comments : commentMap.values()) {
                                commentIds.addAll(Formula.listDistinct(comments, c -> c.getId()));
                        }
                        Map<Integer, List<Reply>> replyMap = qaService.getReplyMapByCommentIds(commentIds);
                        JSONArray answerInfoList = new JSONArray();
                        for (Answer answer : answers) {
                                JSONObject answerInfo = new JSONObject();
                                answerInfo.put("id", answer.getId());
                                answerInfo.put("time", answer.getCreateTime());
                                answerInfo.put("content", answer.getContent());
                                int commentCount = 0;
                                int replyCount = 0;
                                List<Comment> comments = commentMap.get(answer.getId());
                                if (!Formula.isEmptyCollection(comments)) {
                                        commentCount += comments.size();
                                        for (Comment comment : comments) {
                                                List<Reply> replies = replyMap.get(comment.getId());
                                                if (!Formula.isEmptyCollection(replies)) {
                                                        replyCount += replies.size();
                                                }
                                        }
                                }
                                answerInfo.put("commentCount", commentCount);
                                answerInfo.put("replyCount", replyCount);
                                answerInfo.put("forwardCount", answer.getForwardCount());
                                answerInfoList.add(answerInfo);
                        }
                        result.put("answerInfoList", answerInfoList);
                        result.put("todayAnswerCount", qaService.getTodayAnswerCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 评论记录
        @ResponseBody
        @RequestMapping(value = "/commentList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String commentList(int page) {
                logger.info("manage/commentList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Comment> comments = qaService.getComments(page);
                        List<Integer> commentIds = Formula.listDistinct(comments, c -> c.getId());
                        Map<Integer, List<Reply>> replyMap = qaService.getReplyMapByCommentIds(commentIds);
                        JSONArray commentInfoList = new JSONArray();
                        for (Comment comment : comments) {
                                JSONObject commentInfo = new JSONObject();
                                commentInfo.put("id", comment.getId());
                                commentInfo.put("time", comment.getCreateTime());
                                commentInfo.put("content", comment.getContent());
                                int replyCount = 0;
                                List<Reply> replies = replyMap.get(comment.getId());
                                if (!Formula.isEmptyCollection(replies)) {
                                        replyCount += replies.size();
                                }
                                commentInfo.put("replyCount", replyCount);
                                commentInfo.put("agreeCount", comment.getAgreeCount());
                                commentInfoList.add(commentInfo);
                        }
                        result.put("commentInfoList", commentInfoList);
                        result.put("todayCommentCount", qaService.getTodayCommentCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 回复记录
        @ResponseBody
        @RequestMapping(value = "/replyList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String replyList(int page) {
                logger.info("manage/replyList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Reply> replies = qaService.getreplies(page);
                        JSONArray replyInfoList = new JSONArray();
                        for (Reply reply : replies) {
                                JSONObject replyInfo = new JSONObject();
                                replyInfo.put("id", reply.getId());
                                replyInfo.put("time", reply.getCreateTime());
                                replyInfo.put("content", reply.getContent());
                                replyInfo.put("agreeCount", reply.getAgreeCount());
                                replyInfoList.add(replyInfo);
                        }
                        result.put("replyInfoList", replyInfoList);
                        result.put("todayReplyCount", qaService.getTodayReplyCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 专题记录
        @ResponseBody
        @RequestMapping(value = "/subjectList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String subjectList(int page) {
                logger.info("manage/subjectList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<Subject> subjects = subjectService.getSubjects(page);
                        List<Integer> subjectIds = Formula.listDistinct(subjects, s -> s.getId());
                        Map<Integer, List<User2subject>> user2subjectMap = subjectService.getUser2subjectMapBySubjectIds(subjectIds);
                        Map<Integer, List<Question>> questionMap = qaService.getAllQuestionMapBySubjectIds(subjectIds);
                        JSONArray subjectInfoList = new JSONArray();
                        for (Subject subject : subjects) {
                                JSONObject subjectInfo = new JSONObject();
                                subjectInfo.put("id", subject.getId());
                                subjectInfo.put("time", subject.getCreateTime());
                                subjectInfo.put("name", subject.getName());
                                List<User2subject> user2subjects = user2subjectMap.get(subject.getId());
                                subjectInfo.put("userCount", Formula.isEmptyCollection(user2subjects) ? 0 : user2subjects.size());
                                List<Question> questions = questionMap.get(subject.getId());
                                subjectInfo.put("questionCount", Formula.isEmptyCollection(questions) ? 0 : questions.size());
                                subjectInfo.put("description", subject.getDescription());
                                subjectInfo.put("status", subject.getStatus());
                                subjectInfoList.add(subjectInfo);
                        }
                        result.put("subjectInfoList", subjectInfoList);
                        result.put("todaySubjectCount", subjectService.getTodaySubjectCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 点赞记录
        @ResponseBody
        @RequestMapping(value = "/agreeList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String agreeList(int page) {
                logger.info("manage/agreeList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserAnswerAgree> userAnswerAgrees = qaService.getAllUserAnswerAgrees();
                        List<UserCommentAgree> userCommentAgrees = qaService.getAllUserCommentAgrees();
                        List<UserReplyAgree> userReplyAgrees = qaService.getAllUserReplyAgree();
                        List<Integer> answerIds = Formula.listDistinct(userAnswerAgrees, u -> u.getAnswerId());
                        List<Integer> commentIds = Formula.listDistinct(userCommentAgrees, u -> u.getCommentId());
                        List<Integer> replyIds = Formula.listDistinct(userReplyAgrees, u -> u.getReplyId());
                        List<Integer> userIds = new LinkedList<Integer>();
                        Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                        userIds.addAll(Formula.listDistinct(userAnswerAgrees, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(answerMap.values(), a -> a.getUserId()));
                        Map<Integer, Comment> commentMap = qaService.getCommentMap(commentIds);
                        userIds.addAll(Formula.listDistinct(userCommentAgrees, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(commentMap.values(), a -> a.getUserId()));
                        Map<Integer, Reply> replyMap = qaService.getReplyMap(replyIds);
                        userIds.addAll(Formula.listDistinct(userReplyAgrees, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(replyMap.values(), a -> a.getUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<AgreeRecord> agreeRecords = new LinkedList<AgreeRecord>();
                        for (UserAnswerAgree userAnswerAgree : userAnswerAgrees) {
                                Answer answer = answerMap.get(userAnswerAgree.getAnswerId());
                                AgreeRecord agreeRecord = new AgreeRecord();
                                agreeRecord.setAgreeCount(answer.getAgreeCount());
                                agreeRecord.setAgreeUserName(userMap.get(userAnswerAgree.getUserId()).getName());
                                agreeRecord.setContent(answer.getContent());
                                agreeRecord.setTime(answer.getCreateTime());
                                agreeRecord.setUserName(userMap.get(answer.getUserId()).getName());
                                agreeRecords.add(agreeRecord);
                        }

                        for (UserCommentAgree userCommentAgree : userCommentAgrees) {
                                Comment comment = commentMap.get(userCommentAgree.getCommentId());
                                AgreeRecord agreeRecord = new AgreeRecord();
                                agreeRecord.setAgreeCount(comment.getAgreeCount());
                                agreeRecord.setAgreeUserName(userMap.get(userCommentAgree.getUserId()).getName());
                                agreeRecord.setContent(comment.getContent());
                                agreeRecord.setTime(comment.getCreateTime());
                                agreeRecord.setUserName(userMap.get(comment.getUserId()).getName());
                                agreeRecords.add(agreeRecord);
                        }

                        for (UserReplyAgree userReplyAgree : userReplyAgrees) {
                                Reply reply = replyMap.get(userReplyAgree.getReplyId());
                                AgreeRecord agreeRecord = new AgreeRecord();
                                agreeRecord.setAgreeCount(reply.getAgreeCount());
                                agreeRecord.setAgreeUserName(userMap.get(userReplyAgree.getUserId()).getName());
                                agreeRecord.setContent(reply.getContent());
                                agreeRecord.setTime(reply.getCreateTime());
                                agreeRecord.setUserName(userMap.get(reply.getUserId()).getName());
                                agreeRecords.add(agreeRecord);
                        }

                        agreeRecords.sort((e1, e2) -> {
                                if ((e1.getTime().getTime() > e2.getTime().getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<AgreeRecord> resultAgreeRecords = new PageInfo<AgreeRecord>(agreeRecords).getList();

                        JSONArray agreeInfoList = new JSONArray();
                        for (AgreeRecord agreeRecord : resultAgreeRecords) {
                                JSONObject agreeInfo = new JSONObject();
                                agreeInfo.put("time", agreeRecord.getTime());
                                agreeInfo.put("content", agreeRecord.getContent());
                                agreeInfo.put("userName", agreeRecord.getUserName());
                                agreeInfo.put("agreeUserName", agreeRecord.getAgreeUserName());
                                agreeInfo.put("agreeCount", agreeRecord.getAgreeCount());
                                agreeInfoList.add(agreeInfo);
                        }
                        result.put("agreeInfoList", agreeInfoList);
                        result.put("todayAgreeCount", qaService.getTodayAgreeCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 转发记录
        @ResponseBody
        @RequestMapping(value = "/forwardList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String forwardList(int page) {
                logger.info("manage/forwardList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserQuestionForward> userQuestionForwards = qaService.getAllUserQuestionForward();
                        List<UserAnswerForward> userAnswerForwards = qaService.getAllUserAnswerForward();
                        List<Integer> questionIds = Formula.listDistinct(userQuestionForwards, u -> u.getQuestionId());
                        List<Integer> answerIds = Formula.listDistinct(userAnswerForwards, u -> u.getAnswerId());
                        Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                        Map<Integer, Answer> answerMap = qaService.getAnswerMap(answerIds);
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(userQuestionForwards, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(questionMap.values(), a -> a.getUserId()));
                        userIds.addAll(Formula.listDistinct(userAnswerForwards, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(answerMap.values(), a -> a.getUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<ForwardRecord> forwardRecords = new LinkedList<ForwardRecord>();
                        for (UserQuestionForward userQuestionForward : userQuestionForwards) {
                                ForwardRecord forwardRecord = new ForwardRecord();
                                Question question = questionMap.get(userQuestionForward.getQuestionId());
                                User user = userMap.get(question.getUserId());
                                User forwardUser = userMap.get(userQuestionForward.getUserId());
                                forwardRecord.setContent(question.getSummary());
                                forwardRecord.setForwardCount(question.getForwardCount());
                                forwardRecord.setForwardUserName(forwardUser.getName());
                                forwardRecord.setTime(userQuestionForward.getCreateTime());
                                forwardRecord.setUserName(user.getName());
                                forwardRecords.add(forwardRecord);
                        }
                        for (UserAnswerForward userAnswerForward : userAnswerForwards) {
                                ForwardRecord forwardRecord = new ForwardRecord();
                                Answer answer = answerMap.get(userAnswerForward.getAnswerId());
                                User user = userMap.get(answer.getUserId());
                                User forwardUser = userMap.get(userAnswerForward.getUserId());
                                forwardRecord.setContent(answer.getContent());
                                forwardRecord.setForwardCount(answer.getForwardCount());
                                forwardRecord.setForwardUserName(forwardUser.getName());
                                forwardRecord.setTime(userAnswerForward.getCreateTime());
                                forwardRecord.setUserName(user.getName());
                                forwardRecords.add(forwardRecord);
                        }

                        forwardRecords.sort((e1, e2) -> {
                                if ((e1.getTime().getTime() > e2.getTime().getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<ForwardRecord> resultForwardRecords = new PageInfo<ForwardRecord>(forwardRecords).getList();

                        JSONArray forwardInfoList = new JSONArray();
                        for (ForwardRecord forwardRecord : resultForwardRecords) {
                                JSONObject forwardInfo = new JSONObject();
                                forwardInfo.put("time", forwardRecord.getTime());
                                forwardInfo.put("content", forwardRecord.getContent());
                                forwardInfo.put("userName", forwardRecord.getUserName());
                                forwardInfo.put("forwardUserName", forwardRecord.getForwardUserName());
                                forwardInfo.put("forwardCount", forwardRecord.getForwardCount());
                                forwardInfoList.add(forwardInfo);
                        }
                        result.put("forwardInfoList", forwardInfoList);
                        result.put("allForwardCount", qaService.getForwardCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 收藏记录
        @ResponseBody
        @RequestMapping(value = "/collectionList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String collectionList(int page) {
                logger.info("manage/collectionList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<UserQuestionCollection> userQuestionCollections = qaService.getAllUserQuestionCollection();
                        List<Integer> questionIds = Formula.listDistinct(userQuestionCollections, u -> u.getQuestionId());
                        Map<Integer, Question> questionMap = qaService.getQuestionMap(questionIds);
                        List<Integer> userIds = new LinkedList<Integer>();
                        userIds.addAll(Formula.listDistinct(userQuestionCollections, u -> u.getUserId()));
                        userIds.addAll(Formula.listDistinct(questionMap.values(), a -> a.getUserId()));
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        List<CollectionRecord> collectionRecords = new LinkedList<CollectionRecord>();
                        for (UserQuestionCollection userQuestionCollection : userQuestionCollections) {
                                CollectionRecord collectionRecord = new CollectionRecord();
                                Question question = questionMap.get(userQuestionCollection.getQuestionId());
                                User user = userMap.get(question.getUserId());
                                User collectionUser = userMap.get(userQuestionCollection.getUserId());
                                collectionRecord.setCollectionCount(question.getCollectionCount());
                                collectionRecord.setCollectionUserName(collectionUser.getName());
                                collectionRecord.setContent(question.getSummary());
                                collectionRecord.setTime(userQuestionCollection.getCreateTime());
                                collectionRecord.setUserName(user.getName());
                                collectionRecords.add(collectionRecord);
                        }
                        collectionRecords.sort((e1, e2) -> {
                                if ((e1.getTime().getTime() > e2.getTime().getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<CollectionRecord> resultCollectionRecords = new PageInfo<CollectionRecord>(collectionRecords).getList();

                        JSONArray collectionInfoList = new JSONArray();
                        for (CollectionRecord collectionRecord : resultCollectionRecords) {
                                JSONObject collectionInfo = new JSONObject();
                                collectionInfo.put("time", collectionRecord.getTime());
                                collectionInfo.put("content", collectionRecord.getContent());
                                collectionInfo.put("userName", collectionRecord.getUserName());
                                collectionInfo.put("collectionUserName", collectionRecord.getCollectionUserName());
                                collectionInfo.put("collectionCount", collectionRecord.getCollectionCount());
                                collectionInfoList.add(collectionInfo);
                        }
                        result.put("collectionInfoList", collectionInfoList);
                        result.put("todayCollectionCount", qaService.getTodayCollectionCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 聊天记录
        @ResponseBody
        @RequestMapping(value = "/chatList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String chatList(int page) {
                logger.info("manage/chatList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<ChatRecord> chatRecords = new LinkedList<ChatRecord>();
                        Set<String> chatIds = chatService.getAllChatId();
                        for (String chatId : chatIds) {
                                MsgCVO msgCVO = chatService.getChatLastMsgCVo(chatId);
                                ChatRecord chatRecord = new ChatRecord();
                                chatRecord.setChatId(chatId);
                                chatRecord.setTime(msgCVO.getSendTime());
                                chatRecords.add(chatRecord);
                        }
                        chatRecords.sort((e1, e2) -> {
                                if ((e1.getTime() > e2.getTime())) {
                                        return -1;
                                } else {
                                        return 1;
                                }
                        });
                        PageHelper.startPage(page, IConstants.PAGE_SIZE);
                        List<ChatRecord> resultChatRecords = new PageInfo<ChatRecord>(chatRecords).getList();
                        List<Integer> userIds = new LinkedList<Integer>();
                        for (ChatRecord chatRecord : resultChatRecords) {
                                userIds.addAll(chatService.getChatMembers(chatRecord.getChatId()));
                        }
                        Map<Integer, User> userMap = userService.getUserMap(userIds);
                        JSONArray chatInfoList = new JSONArray();
                        for (ChatRecord chatRecord : resultChatRecords) {
                                List<MsgCVO> msgCVOs = chatService.getChatMsgCVo(chatRecord.getChatId(), 0);
                                List<Integer> ids = chatService.getChatMembers(chatRecord.getChatId());
                                JSONObject chatInfo = new JSONObject();
                                chatInfo.put("startTime", msgCVOs.get(0).getSendTime());
                                chatInfo.put("endTime", msgCVOs.get(msgCVOs.size() - 1).getSendTime());
                                chatInfo.put("nameA", userMap.get(ids.get(0)).getName());
                                chatInfo.put("nameB", userMap.get(ids.get(1)).getName());
                                chatInfoList.add(chatInfo);
                        }
                        result.put("chatInfoList", chatInfoList);
                        result.put("todayChatCount", chatService.getTodayChatCount());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 广告记录
        @ResponseBody
        @RequestMapping(value = "/advertList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String advertList(int page,int type) {
                logger.info("manage/advertList request page:" + page);
                JSONObject result = new JSONObject();
                try {
                        List<AdvertRecord> advertRecords = advertService.getAdvertRecords(page,type);
                        JSONArray advertInfoList = new JSONArray();
                        for (AdvertRecord advertRecord : advertRecords) {
                                JSONObject advertInfo = new JSONObject();
                                advertInfo.put("id", advertRecord.getId());
                                advertInfo.put("url", advertRecord.getPictureUrl());
                                advertInfo.put("comment", advertRecord.getComment());
                                advertInfo.put("startTime", advertRecord.getStartTime());
                                advertInfo.put("endTime", advertRecord.getEndTime());
                                advertInfo.put("type", advertRecord.getType());
                                advertInfoList.add(advertInfo);
                        }
                        result.put("advertInfoList", advertInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 添加广告
        @ResponseBody
        @RequestMapping(value = "/addAdvert", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String addAdvert(String pictureUrl, String comment, long startTime, long endTime,int type) {
                logger.info("manage/addAdvert request pictureUrl:" + pictureUrl + " ;comment:" + comment + " ;startTime:" + startTime + " ;endTime:" + endTime);
                JSONObject result = new JSONObject();
                try {
                        AdvertRecord advertRecord = advertService.addAdvertService(pictureUrl, startTime, endTime, comment,type);
                        result.put("id", advertRecord.getId());
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 修改广告
        @ResponseBody
        @RequestMapping(value = "/changeAdvert", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String changeAdvert(int id, String pictureUrl, String comment, long startTime, long endTime,int type) {
                logger.info("manage/changeAdvert request id:" + id + " ;pictureUrl:" + pictureUrl + " ;comment:" + comment + " ;startTime:" + startTime + " ;endTime:" + endTime);
                JSONObject result = new JSONObject();
                try {
                        advertService.updateAdvertRecord(id, pictureUrl, startTime, endTime, comment, type);
                        result.put("id", id);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 删除广告
        @ResponseBody
        @RequestMapping(value = "/delAdvert", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String delAdvert(int id) {
                logger.info("manage/delAdvert request id:" + id);
                JSONObject result = new JSONObject();
                try {
                        advertService.delAdvertRecord(id);
                        result.put("isSuccess", true);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }

        // 搜索广告
        @ResponseBody
        @RequestMapping(value = "/searchAdvert", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
        public String searchAdvert(String comment) {
                logger.info("manage/searchAdvert request comment:" + comment);
                JSONObject result = new JSONObject();
                try {
                        List<AdvertRecord> advertRecords = advertService.getAdvertRecords(comment);
                        JSONArray advertInfoList = new JSONArray();
                        for (AdvertRecord advertRecord : advertRecords) {
                                JSONObject advertInfo = new JSONObject();
                                advertInfo.put("id", advertRecord.getId());
                                advertInfo.put("url", advertRecord.getPictureUrl());
                                advertInfo.put("comment", advertRecord.getComment());
                                advertInfo.put("startTime", advertRecord.getStartTime());
                                advertInfo.put("endTime", advertRecord.getEndTime());
                                advertInfoList.add(advertInfo);
                        }
                        result.put("advertInfoList", advertInfoList);
                } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        logger.error(sw.toString());
                }
                return result.toJSONString();
        }
    //上传图片
    @RequestMapping(value ="/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(MultipartFile file, HttpServletRequest request) throws Exception {
            logger.info("manage/upload request img:" + file);
            JSONObject result = new JSONObject();
            try {
                    if (file==null){
                            result.put("isSuccess", false);
                            result.put("msg", "上传文件有问题");
                            return result.toJSONString();
                    }
                    OssClienUtils ossClient = new OssClienUtils();
                    String name = ossClient.uploadImg2Oss(file);
                    String imgUrl = ossClient.getImgUrl(name);
                    result.put("img", imgUrl);
                    return result.toJSONString();
            } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw, true));
                    logger.error(sw.toString());
            }
            return result.toJSONString();
    }
}
