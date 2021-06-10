package com.ayue.sp.db.cache.dao;

import com.ayue.sp.core.redisCache.dao.AbstractZSetCDao;
import org.springframework.stereotype.Repository;

@Repository
public class UserLoginZsetDao extends AbstractZSetCDao<String> {


    private final static String key = "user:userLoginToken:";

    public UserLoginZsetDao() {
        super(String.class);
    }
    private String getKey(String userName) {
        return key + userName;
    }

    /**
     * 添加token
     * @param userName
     * @param token
     */
    public void addUserToken(String userName,String token){
        this.zadd(this.getKey(userName), token, System.currentTimeMillis());
    }

    /**
     * 移除token
     * @param userName
     * @param token
     */
    public void removeToken(String userName,String token) {
        this.zrem(this.getKey(userName), token);
    }

    public String getValue(String userName){
        return this.redisTemplate.opsForValue().get(userName);
    }
}
