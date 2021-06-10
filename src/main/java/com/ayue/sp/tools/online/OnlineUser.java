package com.ayue.sp.tools.online;

import java.util.HashMap;
import java.util.Map;

/**
 * 2020年8月24日
 *
 * @author ayue
 */
public class OnlineUser {
        private int userId;
        private long lastRequestTime;
        private Map<String, Long> chatReadCursorMap = new HashMap<String, Long>();

        public OnlineUser(int userId) {
                this.userId = userId;
        }

        public int getUserId() {
                return userId;
        }

        public void setChatReadCursor(String chatId, long cursor) {
                this.chatReadCursorMap.put(chatId, cursor + 1);
        }

        public long getChatReadCursor(String chatId) {
                return chatReadCursorMap.get(chatId) == null ? 0 : chatReadCursorMap.get(chatId);
        }

        public long getLastRequestTime() {
                return lastRequestTime;
        }

        public void updateLastRequestTime() {
                lastRequestTime = System.currentTimeMillis();
        }

}
