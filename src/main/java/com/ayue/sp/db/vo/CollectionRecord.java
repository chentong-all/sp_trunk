package com.ayue.sp.db.vo;

import java.util.Date;

/**
 * 2020年10月9日
 *
 * @author ayue
 */
public class CollectionRecord implements Comparable<CollectionRecord> {
        private Date time;
        private String content;
        private String userName;
        private String collectionUserName;
        private int collectionCount;

        public Date getTime() {
                return time;
        }

        public String getContent() {
                return content;
        }

        public String getUserName() {
                return userName;
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

        public String getCollectionUserName() {
                return collectionUserName;
        }

        public void setCollectionUserName(String collectionUserName) {
                this.collectionUserName = collectionUserName;
        }

        public int getCollectionCount() {
                return collectionCount;
        }

        public void setCollectionCount(int collectionCount) {
                this.collectionCount = collectionCount;
        }

        @Override
        public int compareTo(CollectionRecord other) {
                if (this.time.getTime() > other.time.getTime())
                        return -1;
                else
                        return 1;
        }
}
