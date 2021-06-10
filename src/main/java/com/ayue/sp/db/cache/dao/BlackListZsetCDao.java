package com.ayue.sp.db.cache.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.IConstants;
import com.ayue.sp.core.redisCache.dao.AbstractZSetCDao;

/**
 * 2020年8月26日
 *
 * @author ayue
 */
@Repository
public class BlackListZsetCDao extends AbstractZSetCDao<Integer> {

        private final static String key = "chat:blackList";

        public BlackListZsetCDao() {
                super(Integer.class);
        }

        private String getKey(int userId) {
                return key + ":" + userId;
        }

        public void addBlackList(int userId, int targetUserId) {
                this.zadd(this.getKey(userId), targetUserId, System.currentTimeMillis());
        }

        public void removeBlackList(int userId, int targetUserId) {
                this.zrem(getKey(userId), targetUserId);
        }

        public List<Integer> getBlackLists(int userId) {
                return this.zrangeDesc(this.getKey(userId), 0, IConstants.BLACK_LIST_MAX_COUNT);
        }

        public boolean isHaveBlackList(int userId, long targetUserId) {
                return this.getRank(getKey(userId), targetUserId) != null;
        }

        public int getCount(int userId) {
                return this.zcard(this.getKey(userId));
        }

}
