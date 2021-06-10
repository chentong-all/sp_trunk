package com.ayue.sp.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import com.alibaba.fastjson.JSONObject;
import com.ayue.sp.db.cache.dao.RankZSetCDao;
import com.ayue.sp.db.dao.*;
import com.ayue.sp.db.po.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.ayue.sp.tools.lock.LockTools;
import com.ayue.sp.tools.lock.lockObject.AnswerLockKey;
import com.ayue.sp.tools.lock.lockObject.QuestionLockKey;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 2020年8月27日
 *
 * @author ayue
 */
@Service
public class QAService {
        @Autowired
        private QuestionMapper questionMapper;
        @Autowired
        private AnswerMapper answerMapper;
        @Autowired
        private CommentMapper commentMapper;
        @Autowired
        private ReplyMapper replyMapper;
        @Autowired
        private QuestionLinkRecordMapper questionLinkRecordMapper;
        @Autowired
        private UserReplyAgreeMapper userReplyAgreeMapper;
        @Autowired
        private UserQuestionCollectionMapper userQuestionCollectionMapper;
        @Autowired
        private UserAnswerAgreeMapper userAnswerAgreeMapper;
        @Autowired
        private UserCommentAgreeMapper userCommentAgreeMapper;
        @Autowired
        private IdGeneratorTools idGeneratorTools;
        @Autowired
        private LockTools lockTools;
        @Autowired
        private UserBrowseMapper userBrowseMapper;
        @Autowired
        private UserQuestionForwardMapper userQuestionForwardMapper;
        @Autowired
        private UserAnswerForwardMapper userAnswerForwardMapper;
        @Autowired
        private AnswerChargeMapper answerChargeMapper;
        @Autowired
        private EnergyBeanMapper energyBeanMapper;
        @Autowired
        private SignInMapper signInMapper;
        @Autowired
        private UserMapper userMapper;
        @Autowired
        private RankZSetCDao rankZSetCDao;

        public void lockQuestion(int questionId) {
                QuestionLockKey lockKey = new QuestionLockKey(questionId);
                Lock lock = lockTools.getLock(lockKey);
                lock.lock();
        }

        public void unlockQuestion(int questionId) {
                QuestionLockKey lockKey = new QuestionLockKey(questionId);
                Lock lock = lockTools.getLock(lockKey);
                lock.unlock();
        }

        public void lockAnswer(int answerId) {
                AnswerLockKey lockKey = new AnswerLockKey(answerId);
                Lock lock = lockTools.getLock(lockKey);
                lock.lock();
        }

        public void unlockAnswer(int answerId) {
                AnswerLockKey lockKey = new AnswerLockKey(answerId);
                Lock lock = lockTools.getLock(lockKey);
                lock.unlock();
        }

        public List<Question> getDynamicQuestions(int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andTypeEqualTo(IConstants.QUESTION_TYPE_0).andLastAnswerTimeIsNotNull().andStatusEqualTo(0);
                example.setOrderByClause("last_answer_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getDynamicQuestions(int page, int subjectId) {
                QuestionExample example = new QuestionExample();
                example.or().andCrowdSubjectIdEqualTo(subjectId);
                example.or().andRegionSubjectIdEqualTo(subjectId);
                example.or().andOtherSubjectIdEqualTo(subjectId);
                example.createCriteria().andTypeEqualTo(IConstants.QUESTION_TYPE_0).andLastAnswerTimeIsNotNull();
                example.setOrderByClause("last_answer_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getRewardQuestions(int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andStatusEqualTo(0);
                example.setOrderByClause("reward_ticket desc,id asc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getRewardQuestions(int page, int subjectId) {
                QuestionExample example = new QuestionExample();
                example.or().andCrowdSubjectIdEqualTo(subjectId);
                example.or().andRegionSubjectIdEqualTo(subjectId);
                example.or().andOtherSubjectIdEqualTo(subjectId);
                example.setOrderByClause("reward_ticket desc,id asc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getNewestQuestions(int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andStatusEqualTo(0).andLastAnswerTimeIsNull().andTypeEqualTo(IConstants.QUESTION_TYPE_0);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }
        public List<Question> getNewestQuestionss(int page) {
                QuestionExample example = new QuestionExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, 3);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getNewestQuestions(int page, int subjectId) {
                QuestionExample example = new QuestionExample();
                example.or().andCrowdSubjectIdEqualTo(subjectId);
                example.or().andRegionSubjectIdEqualTo(subjectId);
                example.or().andOtherSubjectIdEqualTo(subjectId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getFastQuestions(int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andTypeEqualTo(IConstants.QUESTION_TYPE_1).andStatusEqualTo(0);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getFastQuestions(int page, int subjectId) {
                QuestionExample example = new QuestionExample();
                example.or().andCrowdSubjectIdEqualTo(subjectId);
                example.or().andRegionSubjectIdEqualTo(subjectId);
                example.or().andOtherSubjectIdEqualTo(subjectId);
                example.createCriteria().andTypeEqualTo(IConstants.QUESTION_TYPE_1);
                example.setOrderByClause("last_answer_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public Question getQuestion(int questionId) {
                return questionMapper.selectByPrimaryKey(questionId);
        }
        public List<Question> getQuestionList() {
                return questionMapper.selectByExample(null);
        }

        public Answer getAnswer(int answerId) {
                return answerMapper.selectByPrimaryKey(answerId);
        }

        public void updateQuestion(Question question) {
                questionMapper.updateByPrimaryKey(question);
        }

        public List<Question> getQuestions(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyList();
                }
                QuestionExample example = new QuestionExample();
                example.createCriteria().andIdIn(questionIds).andStatusEqualTo(0);
                return questionMapper.selectByExample(example);
        }

        public Map<Integer, Question> getQuestionMap(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyMap();
                }
                List<Question> questions = this.getQuestions(questionIds);
                if (Formula.isEmptyCollection(questions)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(questions, q -> q.getId());
        }

        public List<Answer> getMaxClickAnswerByQuestionIds(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds))
                        return Collections.emptyList();
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer integer : questionIds) {
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                }
                String questionIdString = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
                return answerMapper.selectMaxClick(questionIdString);
        }

        public List<Question> getMaxReadQuestionByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds))
                        return Collections.emptyList();
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer integer : userIds) {
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                }
                String userIdString = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
                return questionMapper.selectMaxRead(userIdString);
        }

        public List<Answer> getLastAnswerByQuestionIds(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyList();
                }
                Map<Integer, List<Answer>> answerMap = this.getAnswerMapByQuestionIds(questionIds);
                List<Answer> answers = new LinkedList<Answer>();
                for (Integer questionId : questionIds) {
                        List<Answer> as = answerMap.get(questionId);
                        if (!Formula.isEmptyCollection(as)) {
                                answers.add(as.get(0));
                                continue;
                        }
                }
                if (Formula.isEmptyCollection(answers)){
                        return Collections.emptyList();}
                return answers;

        }

        public List<Question> searchQuestions(String searchContent, int page) {
                String[] searchContents = searchContent.split("");
                QuestionExample example = new QuestionExample();
                example.or().andSummaryLike("%" + searchContent + "%");
                /*for (String string : searchContents) {
                        example.or().andSummaryLike("%" + string + "%");
                }*/
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Answer> getAnswersByQuestionIds(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyList();
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andQuestionIdIn(questionIds);
                example.setOrderByClause("create_time desc");
                return answerMapper.selectByExample(example);
        }

        public Map<Integer, List<Answer>> getAnswerMapByQuestionIds(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)) {
                        return Collections.emptyMap();
                }
                List<Answer> answers = this.getAnswersByQuestionIds(questionIds);
                if (Formula.isEmptyCollection(answers)) {
                        return Collections.emptyMap();
                }
                return Formula.listGrouping(answers, a -> a.getQuestionId());
        }

        public List<Comment> getCommentsByAnswerIds(List<Integer> answerIds) {
                if (Formula.isEmptyCollection(answerIds)){
                        return Collections.emptyList();}
                CommentExample example = new CommentExample();
                example.createCriteria().andAnswerIdIn(answerIds);
                example.setOrderByClause("create_time desc");
                return commentMapper.selectByExample(example);
        }

        public Map<Integer, List<Comment>> getCommentMapByAnswerIds(List<Integer> answerIds) {
                if (Formula.isEmptyCollection(answerIds)){
                        return Collections.emptyMap();}
                List<Comment> comments = this.getCommentsByAnswerIds(answerIds);
                if (Formula.isEmptyCollection(comments)){
                        return Collections.emptyMap();}
                return Formula.listGrouping(comments, c -> c.getAnswerId());

        }

        public Comment getComment(int id) {
                return commentMapper.selectByPrimaryKey(id);
        }

        public List<Reply> getRepliesByCommentIds(List<Integer> commentIds) {
                if (Formula.isEmptyCollection(commentIds)){
                        return Collections.emptyList();}
                ReplyExample example = new ReplyExample();
                example.createCriteria().andCommentIdIn(commentIds);
                example.setOrderByClause("create_time desc");
                return replyMapper.selectByExample(example);
        }

        public Map<Integer, List<Reply>> getReplyMapByCommentIds(List<Integer> commentIds) {
                if (Formula.isEmptyCollection(commentIds)){
                        return Collections.emptyMap();}
                List<Reply> replies = this.getRepliesByCommentIds(commentIds);
                if (Formula.isEmptyCollection(replies)){
                        return Collections.emptyMap();}
                return Formula.listGrouping(replies, r -> r.getCommentId());
        }

        public QuestionLinkRecord getQuestionLinkRecord(int id) {
                return questionLinkRecordMapper.selectByPrimaryKey(id);
        }

        public List<QuestionLinkRecord> getQuestionLinkRecordByFatherId(int fatherId) {
                QuestionLinkRecordExample example = new QuestionLinkRecordExample();
                example.createCriteria().andFatherIdEqualTo(fatherId);
                return questionLinkRecordMapper.selectByExample(example);
        }

        public List<UserQuestionCollection> getUserQuestionCollections(List<Integer> questionIds) {
                if (Formula.isEmptyCollection(questionIds)){
                        return Collections.emptyList();}
                UserQuestionCollectionExample example = new UserQuestionCollectionExample();
                example.createCriteria().andQuestionIdIn(questionIds);
                return userQuestionCollectionMapper.selectByExample(example);
        }

        public List<UserAnswerAgree> getUserAnswerAgrees(List<Integer> answerIds) {
                if (Formula.isEmptyCollection(answerIds)){
                        return Collections.emptyList();}
                UserAnswerAgreeExample example = new UserAnswerAgreeExample();
                example.createCriteria().andAnswerIdIn(answerIds);
                return userAnswerAgreeMapper.selectByExample(example);
        }

        public List<UserAnswerAgree> getUserAnswerAgrees(int userId) {
                UserAnswerAgreeExample example = new UserAnswerAgreeExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userAnswerAgreeMapper.selectByExample(example);
        }

        public List<UserAnswerAgree> getAllUserAnswerAgrees() {
                return userAnswerAgreeMapper.selectByExample(null);
        }

        public UserAnswerAgree getUserAnswerAgree(int userId, int answerId) {
                return userAnswerAgreeMapper.selectByPrimaryKey(userId, answerId);
        }

        public Map<Integer, UserAnswerAgree> getUserAnswerAgreeMap(int userId) {
                List<UserAnswerAgree> userAnswerAgrees = this.getUserAnswerAgrees(userId);
                if (Formula.isEmptyCollection(userAnswerAgrees)){
                        return Collections.emptyMap();}
                return Formula.list2map(userAnswerAgrees, u -> u.getAnswerId());
        }

        public List<UserCommentAgree> getUserCommentAgrees(List<Integer> commentIds) {
                if (Formula.isEmptyCollection(commentIds)){
                        return Collections.emptyList();}
                UserCommentAgreeExample example = new UserCommentAgreeExample();
                example.createCriteria().andCommentIdIn(commentIds);
                return userCommentAgreeMapper.selectByExample(example);
        }

        public List<UserCommentAgree> getUserCommentAgrees(int userId) {
                UserCommentAgreeExample example = new UserCommentAgreeExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userCommentAgreeMapper.selectByExample(example);
        }

        public List<UserCommentAgree> getAllUserCommentAgrees() {
                return userCommentAgreeMapper.selectByExample(null);
        }

        public UserCommentAgree getUserCommentAgree(int userId, int commentId) {
                return userCommentAgreeMapper.selectByPrimaryKey(userId, commentId);
        }

        public Map<Integer, UserCommentAgree> getUserCommentAgreeMap(int userId) {
                List<UserCommentAgree> userCommentAgrees = this.getUserCommentAgrees(userId);
                if (Formula.isEmptyCollection(userCommentAgrees)){
                        return Collections.emptyMap();}
                return Formula.list2map(userCommentAgrees, u -> u.getCommentId());
        }

        public List<UserReplyAgree> getUserReplyAgree(List<Integer> replyIds) {
                if (Formula.isEmptyCollection(replyIds)){
                        return Collections.emptyList();}
                UserReplyAgreeExample example = new UserReplyAgreeExample();
                example.createCriteria().andReplyIdIn(replyIds);
                return userReplyAgreeMapper.selectByExample(example);
        }

        public List<UserReplyAgree> getUserReplyAgree(int userId) {
                UserReplyAgreeExample example = new UserReplyAgreeExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return userReplyAgreeMapper.selectByExample(example);
        }

        public List<UserReplyAgree> getAllUserReplyAgree() {
                return userReplyAgreeMapper.selectByExample(null);
        }

        public UserReplyAgree getUserReplyAgree(int userId, int replyId) {
                return userReplyAgreeMapper.selectByPrimaryKey(userId, replyId);
        }

        public Map<Integer, UserReplyAgree> getUserReplyAgreeMap(int userId) {
                List<UserReplyAgree> userReplyAgrees = this.getUserReplyAgree(userId);
                if (Formula.isEmptyCollection(userReplyAgrees)){
                        return Collections.emptyMap();}
                return Formula.list2map(userReplyAgrees, u -> u.getReplyId());
        }

        public void addQuestionReadCount(Question question) {
                Question record = new Question();
                record.setId(question.getId());
                record.setReadCount(question.getReadCount() + 1);
                questionMapper.updateByPrimaryKeySelective(record);
        }

        public void addAnswerCommentCount(int answerId) {
                answerMapper.addCommentCount(answerId);
        }

        public void addCommentReplyCount(int commentId) {
                commentMapper.addReplyCount(commentId);
        }

        public Answer creatAnswer(int userId, int questionId, String content, String pictureUrl, String cityAddress, int direction, int ticket, int charge) {
                Answer answer = new Answer();
                answer.setId(idGeneratorTools.getAnswerId());
                answer.setUserId(userId);
                answer.setQuestionId(questionId);
                answer.setContent(content);
                answer.setPictureUrl(pictureUrl);
                answer.setCityAddress(cityAddress);
                answer.setDirection(direction);
                answer.setTicket(ticket);
                answer.setCharge(charge);
                answerMapper.insertSelective(answer);
                return answer;
        }

        public Comment createComment(int userId, int answerId, String content) {
                Comment comment = new Comment();
                comment.setId(idGeneratorTools.getCommentId());
                comment.setAnswerId(answerId);
                comment.setUserId(userId);
                comment.setContent(content);
                commentMapper.insertSelective(comment);
                return comment;
        }

        public Reply createReply(int userId, int targetUserId, int commentId, String content) {
                Reply reply = new Reply();
                reply.setId(idGeneratorTools.getReplyId());
                reply.setCommentId(commentId);
                reply.setUserId(userId);
                reply.setTargetUserId(targetUserId);
                reply.setContent(content);
                replyMapper.insertSelective(reply);
                return reply;
        }

        public List<Question> getQuestionsByUserId(int userId, int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(IConstants.QUESTION_TYPE_0).andStatusEqualTo(0);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getAllQuestionsByUserId(int userId, int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getAllQuestions(int page) {
                QuestionExample example = new QuestionExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Answer> getAnswers(int page) {
                AnswerExample example = new AnswerExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Answer>(answerMapper.selectByExample(example)).getList();
        }

        public List<Question> getQuestionsByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdIn(userIds).andStatusEqualTo(0);
                return questionMapper.selectByExample(example);
        }
        public List<Question> getQuestionsByUserIdsPage(List<Integer> userIds,int page) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdIn(userIds).andStatusEqualTo(0);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }
        public Integer getQuestionByIdsCount(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return 0;
                }
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdIn(userIds).andStatusEqualTo(0);
                return (int)questionMapper.countByExample(example);
        }

        public Map<Integer, List<Question>> getQuestionMapByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyMap();
                }
                List<Question> questions = this.getQuestionsByUserIds(userIds);
                if (Formula.isEmptyCollection(questions)) {
                        return Collections.emptyMap();
                }
                return Formula.listGrouping(questions, question -> question.getUserId());
        }

        public List<Question> getFastQuestionsByUserId(int userId, int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(IConstants.QUESTION_TYPE_1);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Answer> getAnswersByUserId(int userId, int page) {
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Answer>(answerMapper.selectByExample(example)).getList();
        }

        public List<Answer> getAnswersByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdIn(userIds);
                return answerMapper.selectByExample(example);
        }
        public Integer getAnswersByUserIdsCount(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return 0;
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdIn(userIds);
                return (int)answerMapper.countByExample(example);
        }
        public List<Answer> getAnswersByUserIdsPage(List<Integer> userIds,int page) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdIn(userIds);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Answer>(answerMapper.selectByExample(example)).getList();
        }

        public Map<Integer, List<Answer>> getAnswerMapByUserIds(List<Integer> userIds,Integer adopt) {
                List<Answer> answers =null;
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyMap();
                }
                if (adopt==1){
                        answers = this.getAnswersByUserStatus(userIds,adopt);
                }else {
                        answers = this.getAnswersByUserIds(userIds);
                }
                if (Formula.isEmptyCollection(answers)) {
                        return Collections.emptyMap();
                }
                return Formula.listGrouping(answers, a -> a.getUserId());
        }
        public List<Answer> getAnswersByUserStatus(List<Integer> userIds,Integer adopt) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdIn(userIds).andUserAdopt(adopt);
                return answerMapper.selectByExample(example);
        }

        public List<Comment> getCommentsByUserId(int userId, int page) {
                CommentExample example = new CommentExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Comment>(commentMapper.selectByExample(example)).getList();
        }
        public Integer getCommentUserIdCount(int userId){
                return commentMapper.getCommentUserIdCount(userId);
        }

        public List<Comment> getCommentsByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds))
                        return Collections.emptyList();
                CommentExample example = new CommentExample();
                example.createCriteria().andUserIdIn(userIds);
                return commentMapper.selectByExample(example);
        }

        public Map<Integer, List<Comment>> getCommentMapByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds))
                        return Collections.emptyMap();
                List<Comment> comments = this.getCommentsByUserIds(userIds);
                if (Formula.isEmptyCollection(comments))
                        return Collections.emptyMap();
                return Formula.listGrouping(comments, a -> a.getUserId());
        }

        public List<Reply> getReplyByUserId(int userId, int page) {
                ReplyExample example = new ReplyExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Reply>(replyMapper.selectByExample(example)).getList();
        }
        public Integer getReplyUserIdCount(int userId){
                return replyMapper.getReplyUserIdCount(userId);
        }

        public List<Reply> getReplysByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyList();
                }
                ReplyExample example = new ReplyExample();
                example.createCriteria().andUserIdIn(userIds);
                return replyMapper.selectByExample(example);
        }

        public Map<Integer, List<Reply>> getReplyMapByUserIds(List<Integer> userIds) {
                if (Formula.isEmptyCollection(userIds)) {
                        return Collections.emptyMap();
                }
                List<Reply> replys = this.getReplysByUserIds(userIds);
                if (Formula.isEmptyCollection(replys)) {
                        return Collections.emptyMap();
                }
                return Formula.listGrouping(replys, a -> a.getUserId());
        }

        public Reply getReply(int id) {
                return replyMapper.selectByPrimaryKey(id);
        }

        public List<Answer> getAnswers(List<Integer> ids) {
                if (Formula.isEmptyCollection(ids)) {
                        return Collections.emptyList();
                }
                AnswerExample example = new AnswerExample();
                example.createCriteria().andIdIn(ids);
                return answerMapper.selectByExample(example);
        }

        public Map<Integer, Answer> getAnswerMap(List<Integer> ids) {
                if (Formula.isEmptyCollection(ids)) {
                        return Collections.emptyMap();
                }
                List<Answer> answers = this.getAnswers(ids);
                if (Formula.isEmptyCollection(answers)) {
                        return Collections.emptyMap();
                }
                return Formula.list2map(answers, a -> a.getId());
        }

        public Question createHealthyQuestion(int userId, boolean isAnonymous, String title, String content, String pictureUrl, boolean isMan, int birthYear, byte relation, int rewardTicket,
                        Subject subject, String labels, String cityAddress) {
                Question question = new Question();
                question.setId(idGeneratorTools.getQuestionId());
                question.setUserId(userId);
                Boolean isAnonymou=isAnonymous;
                if (isAnonymou!=null){
                        question.setIsAnonymous(isAnonymous);
                }
                question.setType(IConstants.QUESTION_TYPE_0);
                question.setSummary(title);
                if (content!="" || content!=null) {
                        question.setContent(content);
                }
                if (pictureUrl!="" || pictureUrl!=null){
                        question.setPictureUrl(pictureUrl);
                }
                if (isMan) {
                        question.setGender(IConstants.GENDER_1);
                } else {
                        question.setGender(IConstants.GENDER_0);
                }
                question.setBirthYear(birthYear);
                if (relation!=0){
                        question.setRelation(relation);
                }
                if (labels!="" || labels!=null){
                        question.setLabels(labels);
                }
                if (subject!=null) {
                        if (subject.getType() == IConstants.SUBJECT_TYPE_1) {
                                question.setCrowdSubjectId(subject.getId());
                        } else {
                                question.setOtherSubjectId(subject.getId());
                        }
                }
                if (rewardTicket!=0) {
                        question.setRewardTicket(rewardTicket);
                }
                question.setCityAddress(cityAddress);
                questionMapper.insertSelective(question);
                return question;
        }

        public Question createSkillQuestion(int userId, boolean isAnonymous, String title, String content, String pictureUrl, int rewardTicket, Subject subject, String labels, String cityAddress) {
                Question question = new Question();
                question.setId(idGeneratorTools.getQuestionId());
                question.setUserId(userId);
                Boolean isAnonymou=isAnonymous;
                if (isAnonymou!=null){
                        question.setIsAnonymous(isAnonymous);
                }
                question.setType(IConstants.QUESTION_TYPE_0);
                question.setSummary(title);
                if (content!="" || content!=null) {
                        question.setContent(content);
                }
                if (pictureUrl!="" || pictureUrl!=null){
                        question.setPictureUrl(pictureUrl);
                }
                if (labels!="" || labels!=null){
                        question.setLabels(labels);
                }
                if (subject!=null) {
                        if (subject.getType() == IConstants.SUBJECT_TYPE_1) {
                                question.setCrowdSubjectId(subject.getId());
                        } else {
                                question.setOtherSubjectId(subject.getId());
                        }
                }
                if (rewardTicket!=0) {
                        question.setRewardTicket(rewardTicket);
                }
                question.setCityAddress(cityAddress);
                questionMapper.insertSelective(question);
                return question;
        }

        public Question addQuestion(Question question){
                Question questionInfo = new Question();
                questionInfo.setId(idGeneratorTools.getQuestionId());
                questionInfo.setIsAnonymous(question.getIsAnonymous());
                questionInfo.setUserId(question.getUserId());
                questionInfo.setType(question.getType());
                questionInfo.setSummary(question.getSummary());
                questionInfo.setContent(question.getContent());
                questionInfo.setPictureUrl(question.getPictureUrl());
                questionInfo.setGender(question.getGender());
                questionInfo.setBirthYear(question.getBirthYear());
                questionInfo.setRelation(question.getRelation());
                questionInfo.setLabels(question.getLabels());
                questionInfo.setCrowdSubjectId(question.getCrowdSubjectId());
                questionInfo.setRegionSubjectId(question.getRegionSubjectId());
                questionInfo.setOtherSubjectId(question.getOtherSubjectId());
                questionInfo.setRewardTicket(question.getRewardTicket());
                questionInfo.setAnswerCount(question.getAnswerCount());
                questionInfo.setReadCount(question.getReadCount());
                questionInfo.setCollectionCount(question.getCollectionCount());
                questionInfo.setForwardCount(question.getForwardCount());
                questionInfo.setBestAnswerId(question.getBestAnswerId());
                questionInfo.setCityAddress(question.getCityAddress());
                questionMapper.insertSelective(questionInfo);
                return questionInfo;
        }

        public QuestionLinkRecord addQuestionLinkRecord(int questionLinkRecordId,int questionId){
                QuestionLinkRecord questionLinkRecord = new QuestionLinkRecord();
                questionLinkRecord.setId(questionLinkRecordId);
                questionLinkRecord.setFatherId(questionId);
                questionLinkRecordMapper.insert(questionLinkRecord);
                return questionLinkRecord;
        }

        public Question createFastQuestion(int userId, boolean isAnonymous, String summary, String pictureUrl, String cityAddress, Subject subject) {
                Question question = new Question();
                question.setId(idGeneratorTools.getQuestionId());
                question.setUserId(userId);
                question.setIsAnonymous(isAnonymous);
                question.setType(IConstants.QUESTION_TYPE_1);
                question.setSummary(summary);
                question.setPictureUrl(pictureUrl);
                question.setCityAddress(cityAddress);
                if (subject!=null) {
                        if (subject.getType() == IConstants.SUBJECT_TYPE_1) {
                                question.setCrowdSubjectId(subject.getId());
                        } else {
                                question.setOtherSubjectId(subject.getId());
                        }
                }
                questionMapper.insertSelective(question);
                return question;
        }

        public List<Answer> getAnswerByQuestionId(int questionId, int page) {
                AnswerExample example = new AnswerExample();
                example.createCriteria().andQuestionIdEqualTo(questionId);
                example.setOrderByClause("click_count desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Answer>(answerMapper.selectByExample(example)).getList();
        }
        public List<Answer> getAnswerByQuestion(int questionId){
                AnswerExample example = new AnswerExample();
                example.createCriteria().andQuestionIdEqualTo(questionId);
                example.setOrderByClause("click_count desc");
                return new PageInfo<Answer>(answerMapper.selectByExample(example)).getList();

        }

        public void updateQuestionBestAnswer(int questionId, int answerId) {
                Question record = new Question();
                record.setId(questionId);
                record.setBestAnswerId(answerId);
                questionMapper.updateByPrimaryKeySelective(record);
        }

        public void addAnswerTicketCount(Answer answer, int addTicketCount) {
                Answer record = new Answer();
                record.setId(answer.getId());
                record.setTicketCount(answer.getTicketCount() + addTicketCount);
                answerMapper.updateByPrimaryKeySelective(record);
        }

        public List<UserQuestionCollection> getUserQuestionCollections(int userId) {
                UserQuestionCollectionExample example = new UserQuestionCollectionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time");
                return userQuestionCollectionMapper.selectByExample(example);
        }
        public List<UserQuestionCollection> getUserQuestionCollectionsList(int userId,int page) {
                UserQuestionCollectionExample example = new UserQuestionCollectionExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return userQuestionCollectionMapper.selectByExample(example);
        }

        public Map<Integer, UserQuestionCollection> getUserQuestionCollectionMap(int userId) {
                List<UserQuestionCollection> userQuestionCollections = this.getUserQuestionCollections(userId);
                if (Formula.isEmptyCollection(userQuestionCollections)) {
                    return Collections.emptyMap();
                }
                return Formula.list2map(userQuestionCollections, u -> u.getQuestionId());
        }

        public UserQuestionCollection getUserQuestionCollection(int userId, int questionId) {
                return userQuestionCollectionMapper.selectByPrimaryKey(userId, questionId);
        }

        public void addUserQuestionCollection(int userId, int questionId) {
                UserQuestionCollection userQuestionCollection = new UserQuestionCollection();
                userQuestionCollection.setUserId(userId);
                userQuestionCollection.setQuestionId(questionId);
                userQuestionCollectionMapper.insert(userQuestionCollection);
        }

        public void delUserQuestionCollection(int userId, int questionId) {
                userQuestionCollectionMapper.deleteByPrimaryKeys(userId, questionId);
        }

        public List<Answer> getuserAnswers(int userId) {
                AnswerExample example = new AnswerExample();
                example.createCriteria().andUserIdEqualTo(userId);
                return answerMapper.selectByExample(example);
        }

        public void replaceUserBrowse(int userId, int questionId) {
                UserBrowse userBrowse = new UserBrowse();
                userBrowse.setUserId(userId);
                userBrowse.setQuestionId(questionId);
                userBrowse.setCreateTime(new Date());
                userBrowseMapper.replace(userBrowse);
        }

        public List<UserBrowse> getUserBrowse(int userId) {
                UserBrowseExample example = new UserBrowseExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                return userBrowseMapper.selectByExample(example);
        }

        public List<Integer> getMaxIdByOtherSubjectId(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyList();
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer integer : subjectIds) {
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                }
                String subjectIdString = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
                return questionMapper.selectMaxIdByOtherSubjectId(subjectIdString);
        }

        public List<Integer> getMaxIdByRegionSubjectId(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyList();
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer integer : subjectIds) {
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                }
                String subjectIdString = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
                return questionMapper.selectMaxIdByRegionSubjectId(subjectIdString);
        }

        public List<Integer> getMaxIdByCrowdSubjectId(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyList();
                StringBuilder stringBuilder = new StringBuilder();
                for (Integer integer : subjectIds) {
                        stringBuilder.append(integer);
                        stringBuilder.append(",");
                }
                String subjectIdString = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
                return questionMapper.selectMaxIdByCrowdSubjectId(subjectIdString);
        }

        public void addAnswerForwardCount(int answerId) {
                answerMapper.addForwardCount(answerId);
        }

        public void addQuestionForwardCount(int questionId) {
                questionMapper.addForwardCount(questionId);
        }

        public void addUserAnswerAgree(int userId, int answerId) {
                UserAnswerAgree userAnswerAgree = new UserAnswerAgree();
                userAnswerAgree.setUserId(userId);
                userAnswerAgree.setAnswerId(answerId);
                userAnswerAgreeMapper.insert(userAnswerAgree);
        }

        public void delUserAnswerAgree(int userId, int answerId) {
                userAnswerAgreeMapper.deleteByPrimaryKey(userId, answerId);
        }

        public void addUserCommentAgree(int userId, int commentId) {
                UserCommentAgree userCommentAgree = new UserCommentAgree();
                userCommentAgree.setUserId(userId);
                userCommentAgree.setCommentId(commentId);
                userCommentAgreeMapper.insert(userCommentAgree);
        }

        public void delUserCommentAgree(int userId, int commentId) {
                userCommentAgreeMapper.deleteByPrimaryKey(userId, commentId);
        }

        public void addUserReplyAgree(int userId, int replyId) {
                UserReplyAgree userReplyAgree = new UserReplyAgree();
                userReplyAgree.setReplyId(replyId);
                userReplyAgree.setUserId(userId);
                userReplyAgreeMapper.insert(userReplyAgree);
        }

        public void delUserReplyAgree(int userId, int replyId) {
                userReplyAgreeMapper.deleteByPrimaryKey(userId, replyId);
        }

        public void addAnswerAgreeCount(int userId,int answerId) {
                answerMapper.addAgreeCount(userId,answerId);
        }

        public void addCommentAgreeCount(int userId,int commentId) {
                commentMapper.addAgreeCount(userId,commentId);
        }

        public void addReplyAgreeCount(int userId,int replyId) {
                replyMapper.addAgreeCount(userId,replyId);
        }

        public void reduceAnswerAgreeCount(int answerId) {
                answerMapper.reduceAgreeCount(answerId);
        }

        public void reduceCommentAgreeCount(int commentId) {
                commentMapper.reduceAgreeCount(commentId);
        }

        public void reduceReplyAgreeCount(int replyId) {
                replyMapper.reduceAgreeCount(replyId);
        }

        public void addQuestionCollectionCount(int userId,int questionId) {
                questionMapper.addCollectionCount(userId,questionId);
        }

        public void reduceQuestionCollectionCount(int questionId) {
                questionMapper.reduceCollectionCount(questionId);
        }

        public List<Question> getQuestionsByLabels(String labels, int page) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andLabelsLike(labels);
                PageHelper.startPage(page, 5);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }
        public List<Question> getQuestionByLablesUser(int userId,String labels){
                QuestionExample example = new QuestionExample();
                example.createCriteria().andLabelsLike(labels).andUserIdEqualTo(userId);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();

        }
        public List<Question> getQuestionLables(String labels) {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andLabelsLike(labels);
                return new PageInfo<Question>(questionMapper.selectByExample(example)).getList();
        }

        public List<Question> getAllQuestionsBySubjectIds(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds)){
                        return Collections.emptyList();}
                QuestionExample example = new QuestionExample();
                example.or().andCrowdSubjectIdIn(subjectIds);
                example.or().andRegionSubjectIdIn(subjectIds);
                example.or().andOtherSubjectIdIn(subjectIds);
                return questionMapper.selectByExample(example);
        }

        public Map<Integer, List<Question>> getAllQuestionMapBySubjectIds(List<Integer> subjectIds) {
                if (Formula.isEmptyCollection(subjectIds))
                        return Collections.emptyMap();
                List<Question> questions = this.getAllQuestionsBySubjectIds(subjectIds);
                if (Formula.isEmptyCollection(questions))
                        return Collections.emptyMap();

                Map<Integer, List<Question>> questionMap = new HashMap<Integer, List<Question>>();
                for (Question question : questions) {
                        if (question.getCrowdSubjectId() != null) {
                                List<Question> newQuestions = questionMap.get(question.getCrowdSubjectId());
                                if (Formula.isEmptyCollection(newQuestions)) {
                                        newQuestions = new LinkedList<Question>();
                                        questionMap.put(question.getCrowdSubjectId(), newQuestions);
                                }
                                newQuestions.add(question);
                        }
                        if (question.getRegionSubjectId() != null) {
                                List<Question> newQuestions = questionMap.get(question.getRegionSubjectId());
                                if (Formula.isEmptyCollection(newQuestions)) {
                                        newQuestions = new LinkedList<Question>();
                                        questionMap.put(question.getRegionSubjectId(), newQuestions);
                                }
                                newQuestions.add(question);
                        }
                        if (question.getOtherSubjectId() != null) {
                                List<Question> newQuestions = questionMap.get(question.getOtherSubjectId());
                                if (Formula.isEmptyCollection(newQuestions)) {
                                        newQuestions = new LinkedList<Question>();
                                        questionMap.put(question.getOtherSubjectId(), newQuestions);
                                }
                                newQuestions.add(question);
                        }
                }
                return questionMap;
        }

        public void addUserQuestionForward(int userId, int questionId) {
                UserQuestionForward userQuestionForward = new UserQuestionForward();
                userQuestionForward.setUserId(userId);
                userQuestionForward.setQuestionId(questionId);
                userQuestionForwardMapper.insert(userQuestionForward);
        }

        public void addUserAnswerForward(int userId, int answerId) {
                UserAnswerForward userAnswerForward = new UserAnswerForward();
                userAnswerForward.setUserId(userId);
                userAnswerForward.setAnswerId(answerId);
                userAnswerForwardMapper.insert(userAnswerForward);
        }

        public int getQuestionCount() {
                return (int) questionMapper.countByExample(null);
        }
        public int getQuestionCountInfo() {
                QuestionExample example = new QuestionExample();
                example.createCriteria().andTypeEqualTo(IConstants.QUESTION_TYPE_0).andLastAnswerTimeIsNotNull().andStatusEqualTo(0);
                example.setOrderByClause("last_answer_time desc");
                return (int) questionMapper.countByExample(example);
        }

        public int getTodayQuestionCount() {
                QuestionExample example = new QuestionExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) questionMapper.countByExample(example);
        }

        public int getAnswerCount() {
                return (int) answerMapper.countByExample(null);
        }

        public int getTodayAnswerCount() {
                AnswerExample example = new AnswerExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) answerMapper.countByExample(example);
        }

        public int getCommentCount() {
                return (int) commentMapper.countByExample(null);
        }

        public int getTodayCommentCount() {
                CommentExample example = new CommentExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) commentMapper.countByExample(example);
        }

        public int getReplyCount() {
                return (int) replyMapper.countByExample(null);
        }

        public int getTodayReplyCount() {
                ReplyExample example = new ReplyExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) replyMapper.countByExample(example);
        }

        public int getAgreeCount() {
                int allAgreeCount = 0;
                allAgreeCount += (int) userAnswerAgreeMapper.countByExample(null);
                allAgreeCount += (int) userCommentAgreeMapper.countByExample(null);
                allAgreeCount += (int) userReplyAgreeMapper.countByExample(null);
                return allAgreeCount;
        }

        public int getTodayAgreeCount() {
                UserAnswerAgreeExample answerAgreeExample = new UserAnswerAgreeExample();
                UserCommentAgreeExample commentAgreeExample = new UserCommentAgreeExample();
                UserReplyAgreeExample replyAgreeExample = new UserReplyAgreeExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                answerAgreeExample.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                commentAgreeExample.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                replyAgreeExample.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                int allAgreeCount = 0;
                allAgreeCount += (int) userAnswerAgreeMapper.countByExample(answerAgreeExample);
                allAgreeCount += (int) userCommentAgreeMapper.countByExample(commentAgreeExample);
                allAgreeCount += (int) userReplyAgreeMapper.countByExample(replyAgreeExample);
                return allAgreeCount;
        }

        public int getForwardCount() {
                int allForwardCount = 0;
                allForwardCount += (int) userQuestionForwardMapper.countByExample(null);
                allForwardCount += (int) userAnswerForwardMapper.countByExample(null);
                return allForwardCount;
        }

        public int getTodayForwardCount() {
                UserQuestionForwardExample questionForwardExample = new UserQuestionForwardExample();
                UserAnswerForwardExample answerForwardExample = new UserAnswerForwardExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                questionForwardExample.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                answerForwardExample.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                int allForwardCount = 0;
                allForwardCount += (int) userQuestionForwardMapper.countByExample(questionForwardExample);
                allForwardCount += (int) userAnswerForwardMapper.countByExample(answerForwardExample);
                return allForwardCount;
        }

        public int getCollectionCount() {
                return (int) userQuestionCollectionMapper.countByExample(null);
        }

        public int getTodayCollectionCount() {
                UserQuestionCollectionExample example = new UserQuestionCollectionExample();
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                example.createCriteria().andCreateTimeGreaterThan(calendar.getTime());
                return (int) userQuestionCollectionMapper.countByExample(example);
        }

        public List<Comment> getComments(int page) {
                CommentExample example = new CommentExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Comment>(commentMapper.selectByExample(example)).getList();
        }

        public List<Reply> getreplies(int page) {
                ReplyExample example = new ReplyExample();
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<Reply>(replyMapper.selectByExample(example)).getList();
        }

        public List<Comment> getComments(List<Integer> commentIds) {
                if (Formula.isEmptyCollection(commentIds))
                        return Collections.emptyList();
                CommentExample example = new CommentExample();
                example.createCriteria().andIdIn(commentIds);
                return commentMapper.selectByExample(example);
        }

        public Map<Integer, Comment> getCommentMap(List<Integer> commentIds) {
                if (Formula.isEmptyCollection(commentIds)) {
                        return Collections.emptyMap();
                }
                List<Comment> comments = this.getComments(commentIds);
                if (Formula.isEmptyCollection(comments)){
                        return Collections.emptyMap();}
                return Formula.list2map(comments, c -> c.getId());
        }

        public List<Reply> getReplies(List<Integer> replyIds) {
                if (Formula.isEmptyCollection(replyIds)){
                        return Collections.emptyList();}
                ReplyExample example = new ReplyExample();
                example.createCriteria().andIdIn(replyIds);
                return replyMapper.selectByExample(example);
        }

        public Map<Integer, Reply> getReplyMap(List<Integer> replyIds) {
                if (Formula.isEmptyCollection(replyIds)){
                        return Collections.emptyMap();}
                List<Reply> replies = this.getReplies(replyIds);
                if (Formula.isEmptyCollection(replies)){
                        return Collections.emptyMap();}
                return Formula.list2map(replies, r -> r.getId());
        }

        public List<UserQuestionForward> getAllUserQuestionForward() {
                return userQuestionForwardMapper.selectByExample(null);
        }

        public List<UserAnswerForward> getAllUserAnswerForward() {
                return userAnswerForwardMapper.selectByExample(null);
        }

        public List<UserQuestionCollection> getAllUserQuestionCollection() {
                return userQuestionCollectionMapper.selectByExample(null);
        }
        public void updateAnswerTicket( int targetUserId,int ticket,int answerId){
                answerMapper.updateAnswerTicket(targetUserId,ticket,answerId);
        }
        public void updateQuestionTicket(int questionId){
                questionMapper.updateQuestionTicket(questionId);
        }
        public void updateAnswerAdopt(int answerId, int ticket){
            answerMapper.updateAnswerAdopt(answerId,ticket);
        }

        public void updateQuestionOnline(int questionId){
                questionMapper.updateQuestionOnline(questionId);
        }
        public void updateQuestionOffline(int questionId){
                questionMapper.updateQuestionOffline(questionId);
        }
        public void updateAnswerOnline(int answerId){
                answerMapper.updateAnswerOnline(answerId);
        }
        public void updateAnswerOffline(int answerId){
                answerMapper.updateAnswerOffline(answerId);
        }
        public void updateCommentOnline(int commentId){
                commentMapper.updateCommentOnline(commentId);
        }
        public void updateCommentOffline(int commentId){
                commentMapper.updateCommentOffline(commentId);
        }
        public void updateReplyOnline(int replyId){
                replyMapper.updateReplyOnline(replyId);
        }
        public void updateReplyOffline(int replyId){
                replyMapper.updateReplyOffline(replyId);
        }
        public Integer getQuestionUserIdCount(int userId){
                return questionMapper.getQuestionUserIdCount(userId);
        }
        public String message(String openId,int type,String userName){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("touser",openId);
                jsonObject.put("template_id",IConstants.WX_Message_ID);
                jsonObject.put("url","http://dayi.yidaoku.com/dist");
                JSONObject data = new JSONObject();
                JSONObject first = new JSONObject();
                first.put("value", "您有新的消息");
                JSONObject keyword3 = new JSONObject();
                if (type==IConstants.MESSAGE_TYPE_SYSTEM){
                        keyword3.put("value", "系统通知");
                        keyword3.put("color", "#173177");
                }else if (type==IConstants.MESSAGE_TYPE_TICKET){
                        keyword3.put("value", "回答被欣赏");
                        keyword3.put("color", "#173177");
                }else if (type==IConstants.MESSAGE_TYPE_QUESTION){
                        keyword3.put("value", "问题被回答");
                        keyword3.put("color", "#173177");
                }else if (type==IConstants.MESSAGE_TYPE_CHAT){
                        keyword3.put("value", "想跟你私聊");
                        keyword3.put("color", "#173177");
                }else if (type==IConstants.MESSAGE_TYPE_PRICE){
                        keyword3.put("value", "有人向你付费咨询");
                        keyword3.put("color", "#173177");
                }else if (type==IConstants.MESSAGE_TYPE_ACCEPT){
                        keyword3.put("value", "您好，非常高兴收到您的咨询，感谢信任，但是由于个人原因，我暂时无法回复您，给您带来不便深感抱歉，期待下次再联系。");
                        keyword3.put("color", "#173177");
                }
                JSONObject keyword2 = new JSONObject();
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
                String date = format.format(new Date());
                keyword2.put("value", date);
                JSONObject remark = new JSONObject();
                remark.put("value", "请去公众号查看详情");
                JSONObject keyword1 = new JSONObject();
                keyword1.put("value", userName);

                data.put("first",first);
                data.put("keyword1",keyword1);
                data.put("keyword2",keyword2);
                data.put("keyword3",keyword3);
                data.put("remark",remark);

                jsonObject.put("data", data);
                jsonObject.put("appid",IConstants.APP_ID);
                return jsonObject.toJSONString();
        }
        public void addAnswerCharge(int answerId, int userId){
                AnswerCharge answerCharge = new AnswerCharge();
                answerCharge.setAnswerId(answerId);
                answerCharge.setUserId(userId);
                answerChargeMapper.insert(answerCharge);
        }
        public AnswerCharge getAnswerCharge(int answerId,int userId){
               return answerChargeMapper.selectByPrimaryKey(answerId,userId);
        }
        public EnergyBean getEnergyBean(int userId) {
                return energyBeanMapper.selectByPrimaryKey(userId);
        }
        public List<EnergyBean> getEnergyBeans(int userId, int page) {
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(0);
                example.setOrderByClause("create_time desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<EnergyBean>(energyBeanMapper.selectByExample(example)).getList();
        }
        public List<EnergyBean> getEnergyBeanRank(int page) {
                Date date = new Date();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTime(date);
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                calendarStart.set(Calendar.MILLISECOND, 0);
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andStatusEqualTo(0).andEnergyBeanEqualTo(0).andCreateTimeGreaterThanOrEqualTo(calendarStart.getTime());
                example.setOrderByClause("energy_bean desc");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<EnergyBean>(energyBeanMapper.selectByRankExample(example)).getList();
        }
        public List<EnergyBean> getEnergyBeanRank() {
                Date date = new Date();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTime(date);
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                calendarStart.set(Calendar.MILLISECOND, 0);
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andStatusEqualTo(0).andEnergyBeanEqualTo(0).andCreateTimeGreaterThanOrEqualTo(calendarStart.getTime());
                example.setOrderByClause("energy_bean desc");
                return energyBeanMapper.selectByRankExample(example);
        }
        public List<EnergyBean> getEnergyBeanByType(int userId,int type) {
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(type);
                example.setOrderByClause("create_time desc");
                return energyBeanMapper.selectByExample(example);
        }
        public List<SignIn> getSignIn(int userId, int time) {
                Date date = new Date();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTime(date);
                if (time==1){
                        calendarStart.add(calendarStart.DATE, -1);
                }
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                calendarStart.set(Calendar.MILLISECOND, 0);
                SignInExample example = new SignInExample();
                example.createCriteria().andCreateTimeGreaterThanOrEqualTo(calendarStart.getTime()).andUserIdEqualTo(userId);
                example.setOrderByClause("create_time desc");
                return signInMapper.selectByExample(example);
        }
        public SignIn getSignInByUserId(int userId) {
                return signInMapper.selectByPrimaryKey(userId);
        }
        public void addSignIn(int userId, int energyBean, int day) {
            SignIn signIn = new SignIn();
            signIn.setUserId(userId);
            signIn.setEnergyBean(energyBean);
            if (day==7){
                signIn.setDay(1);
            }else {
                signIn.setDay(day+1);
            }
            signInMapper.insert(signIn);
        }
        public List<EnergyBean> getEnergyBeanByTime(int userId, int type){
                Date date = new Date();
                Calendar calendarStart = Calendar.getInstance();
                calendarStart.setTime(date);
                calendarStart.set(Calendar.HOUR_OF_DAY, 0);
                calendarStart.set(Calendar.MINUTE, 0);
                calendarStart.set(Calendar.SECOND, 0);
                calendarStart.set(Calendar.MILLISECOND, 0);
                EnergyBeanExample example = new EnergyBeanExample();
                example.createCriteria().andUserIdEqualTo(userId).andCreateTimeGreaterThanOrEqualTo(calendarStart.getTime()).andTypeEqualTo(type);
                return energyBeanMapper.selectByExample(example);
        }
        public void addEnergyBean(int userId, int number, int type){
                User user = userMapper.selectByPrimaryKey(userId);
                EnergyBean energyBean = new EnergyBean();
                energyBean.setUserId(userId);
                if (type==1) {
                        energyBean.setType(IConstants.ENERGY_BEAN_1);
                        energyBean.setEnergyBean(9);
                        user.setEnergyBean(user.getEnergyBean()+9);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==2) {
                        energyBean.setType(IConstants.ENERGY_BEAN_2);
                        energyBean.setEnergyBean(9);
                        user.setEnergyBean(user.getEnergyBean()+9);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==3) {
                        energyBean.setType(IConstants.ENERGY_BEAN_3);
                        energyBean.setEnergyBean(3);
                        user.setEnergyBean(user.getEnergyBean()+3);
                        user.setLikeCount(user.getLikeCount()+1);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==4) {
                        energyBean.setType(IConstants.ENERGY_BEAN_4);
                        energyBean.setEnergyBean(6);
                        user.setEnergyBean(user.getEnergyBean()+6);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==5) {
                        energyBean.setType(IConstants.ENERGY_BEAN_5);
                        energyBean.setEnergyBean(6);
                        user.setEnergyBean(user.getEnergyBean()+6);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==6) {
                        energyBean.setType(IConstants.ENERGY_BEAN_6);
                        energyBean.setEnergyBean(9);
                        user.setEnergyBean(user.getEnergyBean()+9);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==7) {
                        energyBean.setType(IConstants.ENERGY_BEAN_7);
                        energyBean.setEnergyBean(9);
                        user.setEnergyBean(user.getEnergyBean()+9);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==8) {
                        energyBean.setType(IConstants.ENERGY_BEAN_8);
                        energyBean.setEnergyBean(6);
                        user.setEnergyBean(user.getEnergyBean()+6);
                        //userMapper.updateByPrimaryKey(user);
                }
                if (type==9) {
                        energyBean.setType(IConstants.ENERGY_BEAN_9);
                        energyBean.setEnergyBean(30);
                        user.setEnergyBean(user.getEnergyBean()+30);
                        //userMapper.updateByPrimaryKey(user);
                }
                energyBean.setNumber(number+1);
                energyBean.setLuckyDrawStatus(0);
                energyBeanMapper.insert(energyBean);
                userMapper.updateByPrimaryKey(user);
        }
        public void addEnergyBeanLuckyDraw(int userId, int number, int energyBeanCount, int status){
                EnergyBean energyBean = new EnergyBean();
                energyBean.setUserId(userId);
                energyBean.setType(IConstants.ENERGY_BEAN_10);
                if (number==100){
                        energyBean.setEnergyBean(115);
                        energyBean.setLuckyDrawStatus(1);
                        energyBean.setNumber(1);
                }else {
                        energyBean.setNumber(number+1);
                        energyBean.setEnergyBean(energyBeanCount);
                        energyBean.setLuckyDrawStatus(status);
                }
                energyBeanMapper.insert(energyBean);
        }
        public void addEnergyBeans(int userId, int number, int energyBeanCount, int status){
                EnergyBean energyBean = new EnergyBean();
                energyBean.setUserId(userId);
                energyBean.setType(IConstants.ENERGY_BEAN_10);
                energyBean.setNumber(number+1);
                energyBean.setEnergyBean(energyBeanCount);
                energyBean.setLuckyDrawStatus(status);
                energyBeanMapper.insert(energyBean);
        }
        public void addEnergyBeansSignIn(int userId, int number, int energyBeanCount, int status){
                EnergyBean energyBean = new EnergyBean();
                energyBean.setUserId(userId);
                energyBean.setType(IConstants.ENERGY_BEAN_11);
                energyBean.setNumber(number+1);
                energyBean.setEnergyBean(energyBeanCount);
                energyBean.setLuckyDrawStatus(status);
                energyBeanMapper.insert(energyBean);
        }
}
