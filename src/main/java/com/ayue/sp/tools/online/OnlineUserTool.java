package com.ayue.sp.tools.online;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * 2020年8月24日
 *
 * @author ayue
 */
@Repository
public class OnlineUserTool {
        private Map<Integer, OnlineUser> onlineUserCacheMap = new ConcurrentHashMap<Integer, OnlineUser>();

        public Map<Integer, OnlineUser> getAllOnlineUserMap() {
                return this.onlineUserCacheMap;
        }

        public OnlineUser getOnlineUser(int userId) {
                return this.onlineUserCacheMap.get(userId);
        }

        public void removeOnlineUser(int userId) {
                onlineUserCacheMap.remove(userId);
        }

        public OnlineUser initOnlineUser(int userId) {
                OnlineUser onlineUser = new OnlineUser(userId);
                onlineUserCacheMap.put(userId, onlineUser);
                return onlineUser;
        }
}
