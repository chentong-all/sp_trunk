package com.ayue.sp.db.vo;

import java.util.Date;

/**
 * 2020年10月9日
 *
 * @author ayue
 */
public class ForwardRecord implements Comparable<ForwardRecord> {
        private Date time;
        private String content;
        private String userName;
        private String forwardUserName;
        private int forwardCount;

        public Date getTime() {
                return time;
        }

        public void setTime(Date time) {
                this.time = time;
        }

        public String getContent() {
                return content;
        }

        public void setContent(String content) {
                this.content = content;
        }

        public String getUserName() {
                return userName;
        }

        public void setUserName(String userName) {
                this.userName = userName;
        }

        public String getForwardUserName() {
                return forwardUserName;
        }

        public void setForwardUserName(String forwardUserName) {
                this.forwardUserName = forwardUserName;
        }

        public int getForwardCount() {
                return forwardCount;
        }

        public void setForwardCount(int forwardCount) {
                this.forwardCount = forwardCount;
        }

        @Override
        public int compareTo(ForwardRecord o) {
                if (this.time.getTime() > o.time.getTime())
                        return -1;
                else
                        return 1;
        }

}
