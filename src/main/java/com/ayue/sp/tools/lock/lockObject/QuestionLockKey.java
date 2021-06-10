package com.ayue.sp.tools.lock.lockObject;

import com.ayue.sp.tools.lock.ILockKey;

/**
 * 2020年9月1日
 *
 * @author ayue
 */
public class QuestionLockKey implements ILockKey {
        private int questionId;
        private int hashCode = 0;

        public QuestionLockKey(int questionId) {
                this.questionId = questionId;
        }

        @Override
        public int hashCode() {
                if (hashCode == 0) {
                        final int prime = 31;
                        int result = 1;
                        result = prime * result + (int) (questionId ^ (questionId >>> 32));
                        return result;
                } else{
                        return hashCode;}
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj){
                        return true;}
                if (obj == null){
                        return false;}
                if (getClass() != obj.getClass()){
                        return false;}
                QuestionLockKey other = (QuestionLockKey) obj;
                if (questionId != other.questionId){
                        return false;}
                return true;
        }
}
