package com.ayue.sp.tools.lock.lockObject;

import com.ayue.sp.tools.lock.ILockKey;

/**
 * 2020年8月27日
 *
 * @author ayue
 */
public class UserLockKey implements ILockKey {
        private int userId;
        private int hashCode = 0;

        public UserLockKey(int userId) {
                this.userId = userId;
        }

        @Override
        public int hashCode() {
                if (hashCode == 0) {
                        final int prime = 31;
                        int result = 1;
                        result = prime * result + (int) (userId ^ (userId >>> 32));
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
                UserLockKey other = (UserLockKey) obj;
                if (userId != other.userId){
                        return false;}
                return true;
        }
}
