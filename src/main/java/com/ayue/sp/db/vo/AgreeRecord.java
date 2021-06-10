package com.ayue.sp.db.vo;

import java.util.Date;

/**
 * 2020年10月8日
 *
 * @author ayue
 */
public class AgreeRecord implements Comparable<AgreeRecord> {
        private Date time;
        private String content;
        private String userName;
        private String agreeUserName;
        private int agreeCount;

        public Date getTime() {
                return time;
        }

        public String getContent() {
                return content;
        }

        public String getUserName() {
                return userName;
        }

        public String getAgreeUserName() {
                return agreeUserName;
        }

        public int getAgreeCount() {
                return agreeCount;
        }

        public void setTime(Date time) {
                this.time = time;
        }

        public void setContent(String content) {
                this.content = content;
        }

        public void setUserName(String userName) {
                this.userName = userName;
        }

        public void setAgreeUserName(String agreeUserName) {
                this.agreeUserName = agreeUserName;
        }

        public void setAgreeCount(int agreeCount) {
                this.agreeCount = agreeCount;
        }

        @Override
        public int compareTo(AgreeRecord other) {
                if (this.time.getTime() > other.time.getTime())
                        return -1;
                else
                        return 1;
        }

}
