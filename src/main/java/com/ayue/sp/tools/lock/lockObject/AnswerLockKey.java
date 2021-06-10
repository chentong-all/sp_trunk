package com.ayue.sp.tools.lock.lockObject;

import com.ayue.sp.tools.lock.ILockKey;

/**
 * 2020年9月3日
 *
 * @author ayue
 */
public class AnswerLockKey implements ILockKey {
        private int answerId;
        private int hashCode = 0;

        public AnswerLockKey(int answerId) {
                this.answerId = answerId;
        }

        @Override
        public int hashCode() {
                if (hashCode == 0) {
                        final int prime = 31;
                        int result = 1;
                        result = prime * result + (int) (answerId ^ (answerId >>> 32));
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
                AnswerLockKey other = (AnswerLockKey) obj;
                if (answerId != other.answerId){
                        return false;}
                return true;
        }

}
