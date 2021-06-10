package com.ayue.sp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ayue.sp.core.Formula;
import com.ayue.sp.core.IConstants;
import com.ayue.sp.db.cache.dao.BlackListZsetCDao;
import com.ayue.sp.db.cache.dao.MsgListZsetCDao;
import com.ayue.sp.db.cache.dao.UnreadMsgCountHashCDao;
import com.ayue.sp.db.cache.dao.UserChatZSetCDao;
import com.ayue.sp.db.cache.vo.MsgCVO;
import com.ayue.sp.db.dao.UserNewsMapper;
import com.ayue.sp.db.po.UserNews;
import com.ayue.sp.db.po.UserNewsExample;
import com.ayue.sp.tools.idgenerator.IdGeneratorTools;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 2020年8月21日
 *
 * @author ayue
 */
@Service
public class ChatService {
        @Autowired
        private MsgListZsetCDao msgListZsetCDao;
        @Autowired
        private BlackListZsetCDao blackListZsetCDao;
        @Autowired
        private UserChatZSetCDao userChatZSetCDao;
        @Autowired
        private UserNewsMapper userNewsMapper;
        @Autowired
        private UnreadMsgCountHashCDao unreadMsgCountHashCDao;
        @Autowired
        private IdGeneratorTools idGeneratorTools;

        public void sendChat(int userId, String chatId, String textContent, byte[] voiceContent,String img) {
                MsgCVO msgCVo = new MsgCVO();
                msgCVo.setUserId(userId);
                if (!Formula.isEmptyString(textContent)) {
                        msgCVo.setTextContent(textContent);
                }
                if (voiceContent != null && voiceContent.length > 0) {
                        msgCVo.setVoiceContent(voiceContent);
                }
                if (!Formula.isEmptyString(img)){
                        msgCVo.setImg(img);
                }
                msgCVo.setSendTime(System.currentTimeMillis());
                msgListZsetCDao.addMsg(chatId, msgCVo);
        }

        public List<MsgCVO> getChatMsgCVo(String chatId, long cursor) {
                return msgListZsetCDao.getMsgs(chatId, cursor);
        }

        public MsgCVO getChatLastMsgCVo(String chatId) {
                return msgListZsetCDao.getLastMsgs(chatId);
        }

        public List<String> getUserChatIds(int userId) {
                return userChatZSetCDao.getAllChatId(userId);
        }

        public List<Integer> getBlackList(int userId) {
                return blackListZsetCDao.getBlackLists(userId);
        }

        public String getChatId(int... userIds) {
                //Arrays.sort(userIds);
                return userIds[0] + ":" + userIds[1];
        }

        public void addUserChat(int userId, String chatId) {
                userChatZSetCDao.addChat(userId, chatId);
        }

        public void addUserBlack(int userId, int targetUserId) {
                blackListZsetCDao.addBlackList(userId, targetUserId);
        }
        public void removeBlackList(int userId, int targetUserId) {
                blackListZsetCDao.removeBlackList(userId, targetUserId);
        }

        public List<Integer> getChatMembers(String chatId) {
                String[] userIdStrArr = chatId.split(":");
                List<Integer> userIds = new ArrayList<Integer>(2);
                for (String userIdStr : userIdStrArr) {
                        userIds.add(Integer.parseInt(userIdStr));
                }
                return userIds;
        }

        public List<Integer> getAllChatUserList(int userId) {
                List<String> userChatIds = this.getUserChatIds(userId);
                if (Formula.isEmptyCollection(userChatIds)) {
                        return Collections.emptyList();
                }
                List<Integer> userIds = new LinkedList<Integer>();
                for (String chatId : userChatIds) {
                        List<Integer> members = this.getChatMembers(chatId);
                        for (Integer memberId : members) {
                                if (userId == memberId) {
                                        continue;
                                }
                                userIds.add(memberId);
                        }
                }
                return userIds;
        }

        public List<UserNews> getUserNews(int userId, int page) {
                UserNewsExample example = new UserNewsExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("is_read=1,create_time DESC");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserNews>(userNewsMapper.selectByExample(example)).getList();
        }
        public List<UserNews> getUserNews(int userId, int page, byte type) {
                UserNewsExample example = new UserNewsExample();
                example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(type);
                example.setOrderByClause("is_read=1,create_time DESC");
                PageHelper.startPage(page, IConstants.PAGE_SIZE);
                return new PageInfo<UserNews>(userNewsMapper.selectByExample(example)).getList();
        }
        public List<UserNews> getUserNew(int userId) {
                UserNewsExample example = new UserNewsExample();
                example.createCriteria().andUserIdEqualTo(userId);
                example.setOrderByClause("is_read=1");
                return new PageInfo<UserNews>(userNewsMapper.selectByExample(example)).getList();
        }
        public List<UserNews> getUserNew(int userId,  byte type, boolean isRead) {
                UserNewsExample example = new UserNewsExample();
                if (isRead==false){
                        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(type);
                }else {
                        example.createCriteria().andUserIdEqualTo(userId).andTypeEqualTo(type).andIsReadEqualTo(false);

                }
                example.setOrderByClause("is_read=1");

                return new PageInfo<UserNews>(userNewsMapper.selectByExample(example)).getList();
        }
        public List<UserNews> getChat(Integer[] chatId){
                return userNewsMapper.getChat(chatId);
        }
        public void updateChatStatus(Integer[] chatId){
            userNewsMapper.updateChatStatus(chatId);
        }
        public void delReadChat(Integer[] chatId){
            userNewsMapper.delReadChat(chatId);
        }
        public int countUnReadNews(int userId) {
                UserNewsExample example = new UserNewsExample();
                example.createCriteria().andUserIdEqualTo(userId).andIsReadEqualTo(false);
                return (int) userNewsMapper.countByExample(example);
        }

        public void clearUserUnreadChat(int userId, String chatId) {
                unreadMsgCountHashCDao.clearUserUnread(userId, chatId);
        }

        public long incrUnreadChat(int userId, String chatId) {
                return unreadMsgCountHashCDao.incrUnread(userId, chatId);
        }

        public Map<String, Integer> getUnreadChatCount(int userId) {
                return unreadMsgCountHashCDao.getUnreadMsgCountMap(userId);
        }

        public int getUnreadChatCount(int userId, String chatId) {
                return unreadMsgCountHashCDao.getUnreadCount(userId, chatId);
        }

        public void addUserNews(UserNews userNews) {
                userNews.setId(idGeneratorTools.getUserNewsId());
                userNews.setIsRead(false);
                userNewsMapper.insert(userNews);
        }

        public void addUserNews0(int userId, int questionId, int answerId, String summary, String answerUserName, String userName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_0);
                userNews.setTitle("你的问题被回答");
                userNews.setContent(answerUserName+":"+summary +":"+userName);
                userNews.setParam(targetUserId+ ":" + questionId + ":" + answerId);
                this.addUserNews(userNews);
        }

        public void addUserNews1(int userId, String content, int questionId, int answerId, String userName, String userTargetName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_1);
                userNews.setTitle("你的回答被采纳");
                userNews.setContent(userTargetName+":"+content +":"+userName);
                userNews.setParam(targetUserId+ ":" + questionId + ":" + answerId);
                this.addUserNews(userNews);
        }

        public void addUserNews2(int userId, String userName, String targetUserName, int ticket, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_2);
                userNews.setTitle("有人给你投票");
                userNews.setContent(targetUserName+":"+userName+":"+ticket);
                userNews.setParam(targetUserId+"");
                this.addUserNews(userNews);
        }

        public void addUserNews3(int userId, int questionId, String userName, String summary, String targetUserName,int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_3);
                userNews.setTitle("你被邀请回答问题");
                userNews.setContent(targetUserName+":"+userName+":"+summary);
                userNews.setParam(targetUserId+":"+questionId);
                this.addUserNews(userNews);
        }

        public void addUserNews4(int userId, int questionId, int answerId, int commentId, String content, String userName, String targetUserName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_4);
                userNews.setTitle("你的回答被评论");
                userNews.setContent(targetUserName+":"+content+":"+userName);
                userNews.setParam(targetUserId+":"+questionId + ":" + answerId + ":" + commentId);
                this.addUserNews(userNews);
        }

        public void addUserNews5(int userId, int questionId, int answerId, int commentId, int replyId, String content, String userName, String targetUserName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_5);
                userNews.setTitle("你的评论被回复");
                userNews.setContent(targetUserName+":"+content+":"+userName);
                userNews.setParam(targetUserId+":"+questionId + ":" + answerId + ":" + commentId + ":" + replyId);
                this.addUserNews(userNews);
        }

        public void addUserNews6(int userId, String userName, String targetUserName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_6);
                userNews.setTitle("你被其他用户关注");
                userNews.setContent(targetUserName+":"+userName);
                userNews.setParam(targetUserId+"");
                this.addUserNews(userNews);
        }

        public void addUserNews7(int userId, String title, String content) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_7);
                userNews.setTitle(title);
                userNews.setContent(content);
                userNews.setParam(null);
                this.addUserNews(userNews);
        }
        public void addUserNews8(int userId, String userName, String targetUserName, int targetUserId) {
                UserNews userNews = new UserNews();
                userNews.setUserId(userId);
                userNews.setType(IConstants.NEWS_TYPE_8);
                userNews.setTitle("你被邀请提问");
                userNews.setContent(targetUserName+":"+userName);
                userNews.setParam(targetUserId+"");
                this.addUserNews(userNews);
        }

        public int getChatCount() {
                int chatCount = 0;
                Set<String> chatIdKeys = msgListZsetCDao.getAllMsgListKeys();
                if (!Formula.isEmptyCollection(chatIdKeys)) {
                        for (String chatIdKey : chatIdKeys) {
                                List<MsgCVO> msgCVos = msgListZsetCDao.getMsgs(chatIdKey, 0);
                                if (!Formula.isEmptyCollection(msgCVos)) {
                                        chatCount += msgCVos.size();
                                }
                        }
                }
                return chatCount;
        }

        public Set<String> getAllChatId() {
                return msgListZsetCDao.getAllMsgListKeys();
        }

        public int getTodayChatCount() {
                int chatCount = 0;
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Set<String> chatIdKeys = this.getAllChatId();
                if (!Formula.isEmptyCollection(chatIdKeys)) {
                        for (String chatIdKey : chatIdKeys) {
                                List<MsgCVO> msgCVos = msgListZsetCDao.getMsgs(chatIdKey, calendar.getTimeInMillis());
                                if (!Formula.isEmptyCollection(msgCVos)) {
                                        chatCount += msgCVos.size();
                                }
                        }
                }
                return chatCount;
        }

}
