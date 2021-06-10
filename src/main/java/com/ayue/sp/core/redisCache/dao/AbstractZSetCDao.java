package com.ayue.sp.core.redisCache.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import com.ayue.sp.core.redisCache.AbstractRedisCDao;

@Repository
public abstract class AbstractZSetCDao<T> extends AbstractRedisCDao {

        private Class<T> clazz;

        public AbstractZSetCDao(Class<T> classType) {
                this.clazz = classType;
        }

        protected int zcard(String key) {
                return redisTemplate.opsForZSet().zCard(key).intValue();
        }

        protected List<T> zrange(String key, long start, long end) {
                Set<String> set = redisTemplate.opsForZSet().range(key, start, end);
                return parseJsonList(setToList(set), clazz);
        }

        protected List<T> zrangeDesc(String key, long start, long end) {
                Set<String> set = redisTemplate.opsForZSet().reverseRange(key, start, end);
                return parseJsonList(setToList(set), clazz);
        }

        protected List<T> zrangeByScore(String key, double startScore, double endScore) {
                Set<String> set = redisTemplate.opsForZSet().rangeByScore(key, startScore, endScore);
                return parseJsonList(setToList(set), clazz);
        }

        protected List<T> zrangeDescByScore(String key, double startScore, double endScore) {
                Set<String> set = redisTemplate.opsForZSet().reverseRangeByScore(key, startScore, endScore);
                return parseJsonList(setToList(set), clazz);
        }

        protected Set<TypedTuple<String>> zrangeTypedTuple(String key, long start, long end) {
                return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        }

        protected Set<TypedTuple<String>> zrangeTypedTupleDesc(String key, long start, long end) {
                return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        }

        private static List<String> setToList(Set<String> set) {
                if (set == null) {
                        return null;
                }
                return new ArrayList<String>(set);
        }

        protected void zadd(String key, Object obj, double score) {
                if (obj == null) {
                        return;
                }
                redisTemplate.opsForZSet().add(key, toJson(obj), score);
        }

        protected void zaddAll(String key, Set<TypedTuple<String>> tupleSet, Long timeout, TimeUnit unit) {
                if (tupleSet == null || tupleSet.isEmpty()) {
                        return;
                }

                redisTemplate.opsForZSet().add(key, tupleSet);
                if (timeout != null) {
                        redisTemplate.expire(key, timeout, unit);
                }
        }

        @SuppressWarnings("unused")
        private Set<TypedTuple<String>> toTupleSet(List<TypedTuple<String>> tupleList) {
                Set<TypedTuple<String>> tupleSet = new LinkedHashSet<TypedTuple<String>>();
                for (TypedTuple<?> t : tupleList) {
                        tupleSet.add(new DefaultTypedTuple<String>(toJson(t.getValue()), t.getScore()));
                }
                return tupleSet;
        }

        protected void zrem(String key, Object obj) {
                if (obj == null) {
                        return;
                }
                redisTemplate.opsForZSet().remove(key, toJson(obj));
        }

        @SuppressWarnings("hiding")
        protected <T> void zremList(String key, List<T> objs) {
                if (objs == null || objs.size() <= 0) {
                        return;
                }
                List<String> strList = this.toJsonList(objs);
                redisTemplate.opsForZSet().remove(key, strList.toArray());
        }

        protected void unionStore(String destKey, Collection<String> keys, Long timeout, TimeUnit unit) {
                if (keys == null || keys.isEmpty()) {
                        return;
                }

                Object[] keyArr = keys.toArray();
                String key = (String) keyArr[0];

                Collection<String> otherKeys = new ArrayList<String>(keys.size() - 1);
                for (int i = 1; i < keyArr.length; i++) {
                        otherKeys.add((String) keyArr[i]);
                }

                redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
                if (timeout != null) {
                        redisTemplate.expire(destKey, timeout, unit);
                }
        }

        protected Double getScore(String key, Object obj) {
                return redisTemplate.opsForZSet().score(key, toJson(obj));
        }

        protected Long getRank(String key, Object obj) {
                return redisTemplate.opsForZSet().rank(key, toJson(obj));
        }

        protected Long getDescRank(String key, Object obj) {
                return redisTemplate.opsForZSet().reverseRank(key, toJson(obj));
        }
}
