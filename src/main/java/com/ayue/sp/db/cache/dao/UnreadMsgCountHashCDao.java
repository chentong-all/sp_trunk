package com.ayue.sp.db.cache.dao;

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.redisCache.dao.AbstractHashCDao;

/**
 * 2020年9月8日
 *
 * @author ayue
 */
@Repository
public class UnreadMsgCountHashCDao extends AbstractHashCDao<Integer> {

        private final static String key = "chat:unreadMsgCount";

        protected UnreadMsgCountHashCDao() {
                super(Integer.class);
        }

        protected String getKey(int userId) {
                return key + ":" + userId;
        }

        public Map<String, Integer> getUnreadMsgCountMap(int userId) {
                return this.getAll(this.getKey(userId));
        }

        public long incrUnread(int userId, String chatId) {
                return this.increment(this.getKey(userId), chatId, 1);
        }

        public void clearUserUnread(int userId, String chatId) {
                this.put(this.getKey(userId), chatId, 0);
        }

        public int getUnreadCount(int userId, String chatId) {
                return this.get(this.getKey(userId), chatId);
        }

}
