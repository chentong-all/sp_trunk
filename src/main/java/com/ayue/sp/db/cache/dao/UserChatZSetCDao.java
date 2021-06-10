package com.ayue.sp.db.cache.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.redisCache.dao.AbstractZSetCDao;

/**
 * 2020年8月24日
 *
 * @author ayue
 */
@Repository
public class UserChatZSetCDao extends AbstractZSetCDao<String> {
        private final static String key = "chat:userChatList:";

        public UserChatZSetCDao() {
                super(String.class);
        }

        private String getKey(int userId) {
                return key + userId;
        }

        /**
         * 添加一个聊天
         * 
         * @param chatId
         * @param
         */
        public void addChat(int userId, String chatId) {
                this.zadd(this.getKey(userId), chatId, System.currentTimeMillis());
        }

        /**
         * 移除一个聊天
         * 
         * @param chatId
         * @param
         */
        public void removeChat(int userId, String chatId) {
                this.zrem(this.getKey(userId), chatId);
        }

        /**
         * 获取一个聊天用户的所有聊天
         * 
         * @param
         * @return
         */
        public List<String> getAllChatId(int userId) {
                return this.zrange(this.getKey(userId), 0, this.count(userId));
        }

        /**
         * 获取一个用户的聊天总数
         * 
         * @param
         * @return
         */
        public int count(int userId) {
                return this.zcard(this.getKey(userId));
        }

        /**
         * 删除一个用户的聊天
         * 
         * @param
         */
        public void del(int userId) {
                this.delete(this.getKey(userId));
        }
}
