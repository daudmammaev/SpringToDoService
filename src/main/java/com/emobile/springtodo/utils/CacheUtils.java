package com.emobile.springtodo.utils;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CacheUtils {

    private final CacheManager cacheManager;

    public CacheUtils(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Получить статистику по всем кэшам
     */
    public CacheStats getCacheStats() {
        List<CacheInfo> cacheInfos = new ArrayList<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cacheInfos.add(new CacheInfo(cacheName, getCacheSize(cache)));
            }
        }

        return new CacheStats(cacheInfos);
    }

    /**
     * Очистить конкретный кэш
     */
    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Очистить все кэши
     */
    public void clearAllCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    /**
     * Получить элемент из кэша
     */
    public Object getFromCache(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            return valueWrapper != null ? valueWrapper.get() : null;
        }
        return null;
    }

    /**
     * Получить размер кэша (приблизительный)
     */
    private int getCacheSize(Cache cache) {

        if (cache.getNativeCache() instanceof java.util.concurrent.ConcurrentMap) {
            java.util.concurrent.ConcurrentMap<?, ?> map =
                    (java.util.concurrent.ConcurrentMap<?, ?>) cache.getNativeCache();
            return map.size();
        }
        return -1;
    }

    public static class CacheStats {
        private final List<CacheInfo> caches;

        public CacheStats(List<CacheInfo> caches) {
            this.caches = caches;
        }

        public List<CacheInfo> getCaches() {
            return caches;
        }

        public int getTotalSize() {
            return caches.stream()
                    .mapToInt(CacheInfo::getSize)
                    .sum();
        }
    }

    public static class CacheInfo {
        private final String name;
        private final int size;

        public CacheInfo(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }
    }
}
