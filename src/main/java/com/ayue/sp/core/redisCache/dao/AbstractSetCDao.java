package com.ayue.sp.core.redisCache.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Repository;

import com.ayue.sp.core.redisCache.AbstractRedisCDao;

@Repository
public abstract class AbstractSetCDao<T> extends AbstractRedisCDao {
        private Class<T> clazz;

        public AbstractSetCDao(Class<T> classType) {
                this.clazz = classType;
        }

        protected int size(String key) {
                return redisTemplate.opsForSet().size(key).intValue();
        }

        protected void add(String key, T obj) {
                if (obj == null) {
                        return;
                }
                redisTemplate.opsForSet().add(key, toJson(obj));
        }

        protected boolean isHave(String key, T obj) {
                if (obj == null) {
                        return false;
                }
                return redisTemplate.opsForSet().isMember(key, toJson(obj));
        }

        protected void addAll(String key, List<T> tupleList) {
                this.addAll(key, tupleList, null, TimeUnit.SECONDS);
        }

        protected void addAll(String key, List<T> tupleList, Long timeout, TimeUnit unit) {
                if (tupleList == null || tupleList.size() <= 0) {
                        return;
                }

                String[] stringArr = new String[tupleList.size()];
                int index = 0;
                for (T t : tupleList) {
                        stringArr[index++] = toJson(t);
                }

                redisTemplate.opsForSet().add(key, stringArr);
                if (timeout != null) {
                        redisTemplate.expire(key, timeout, unit);
                }
        }

        protected void rem(String key, T obj) {
                if (obj == null) {
                        return;
                }
                redisTemplate.opsForSet().remove(key, toJson(obj));
        }

        protected void rem(String key, List<T> objs) {
                if (objs == null || objs.size() <= 0) {
                        return;
                }
                List<String> objStrs = this.toJsonList(objs);
                redisTemplate.opsForSet().remove(key, objStrs.toArray());
        }

        protected List<T> getAll(String key) {
                Set<String> set = redisTemplate.opsForSet().members(key);
                return parseJsonList(new ArrayList<>(set), clazz);
        }
}
