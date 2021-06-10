package com.ayue.sp.db.vo;

/**
 * 2020年10月9日
 *
 * @author ayue
 */
public class ChatRecord implements Comparable<ChatRecord> {

        private String chatId;
        private long time;

        public String getChatId() {
                return chatId;
        }

        public void setChatId(String chatId) {
                this.chatId = chatId;
        }

        public long getTime() {
                return time;
        }

        public void setTime(long time) {
                this.time = time;
        }

        @Override
        public int compareTo(ChatRecord other) {
                if (this.time > other.time)
                        return -1;
                else
                        return 1;
        }
}
