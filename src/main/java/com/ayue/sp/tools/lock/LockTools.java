package com.ayue.sp.tools.lock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockTools {
        /**
         * 超时时间
         */
        private static final long TIME_OUT = 1000 * 60 * 30;

        /**
         * 全部缓存map
         */
        private Map<Class<? extends ILockKey>, ConcurrentHashMap<ILockKey, ExactLockCache>> lockMaps;

        public LockTools() {
                lockMaps = new HashMap<Class<? extends ILockKey>, ConcurrentHashMap<ILockKey, ExactLockCache>>(32);
        }

        public void registerLockKey(Class<? extends ILockKey> lockClass, int initCount) {
                ConcurrentHashMap<ILockKey, ExactLockCache> lockMap = new ConcurrentHashMap<ILockKey, ExactLockCache>(initCount);
                lockMaps.put(lockClass, lockMap);
        }

        public Lock getLock(ILockKey key) {
                ConcurrentHashMap<ILockKey, ExactLockCache> lockCacheMap = lockMaps.get(key.getClass());
                ExactLockCache cache = lockCacheMap.get(key);
                if (cache == null) {
                        cache = new ExactLockCache();
                        ExactLockCache tempCache = lockCacheMap.putIfAbsent(key, cache);
                        if (tempCache != null)
                                cache = tempCache;
                }
                cache.updateLastOprateTime();
                return cache.lock;
        }

        public void clearTimeOutLock() {
                ExactLockCache cache = null;
                long now = System.currentTimeMillis();
                for (ConcurrentHashMap<ILockKey, ExactLockCache> map : lockMaps.values()) {
                        Iterator<ExactLockCache> it = map.values().iterator();
                        while (it.hasNext()) {
                                cache = it.next();
                                if (cache.getLastOprateTime() - now >= TIME_OUT) {
                                        it.remove();
                                }
                        }
                }
        }

        /**
         * 精确对象锁缓存类
         * 
         * @author Neo
         * @descirption
         */
        private class ExactLockCache {
                /**
                 * 对象锁
                 */
                private Lock lock;
                /**
                 * 最后操作时间
                 */
                private long lastOprateTime;

                private ExactLockCache() {
                        lock = new ReentrantLock();
                        lastOprateTime = System.currentTimeMillis();
                }

                public void updateLastOprateTime() {
                        this.lastOprateTime = System.currentTimeMillis();
                }

                public long getLastOprateTime() {
                        return lastOprateTime;
                }

        }
}
