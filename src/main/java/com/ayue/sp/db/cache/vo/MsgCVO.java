package com.ayue.sp.db.cache.vo;

/**
 * 2020年8月21日
 *
 * @author ayue
 */
public class MsgCVO {
        private int userId;
        private String textContent;
        private String img;
        private byte[] voiceContent;
        private long sendTime;

        public int getUserId() {
                return userId;
        }

        public void setUserId(int userId) {
                this.userId = userId;
        }

        public String getTextContent() {
                return textContent;
        }

        public void setTextContent(String textContent) {
                this.textContent = textContent;
        }

        public byte[] getVoiceContent() {
                return voiceContent;
        }

        public void setVoiceContent(byte[] voiceContent) {
                this.voiceContent = voiceContent;
        }

        public long getSendTime() {
                return sendTime;
        }

        public void setSendTime(long sendTime) {
                this.sendTime = sendTime;
        }

        public String getImg() {
                return img;
        }

        public void setImg(String img) {
                this.img = img;
        }
}
