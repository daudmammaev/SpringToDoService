package com.emobile.springtodo.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheUtils.class);

    private final RedisTemplate redisTemplate;

    public RedisCacheUtils(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Получить статистику по всем кэшам приложения
     */
    public RedisCacheStats getCacheStats() {
        List<RedisCacheInfo> cacheInfos = new ArrayList<>();

        String[] patterns = {"todo:*", "todos:*", "todos_paginated:*"};

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null) {
                cacheInfos.add(new RedisCacheInfo(pattern, keys.size(), getMemoryUsage(keys)));
            }
        }

        return new RedisCacheStats(cacheInfos);
    }

    /**
     * Очистить кэш по паттерну
     */
    public void clearCacheByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            logger.info("Cleared {} keys for pattern: {}", keys.size(), pattern);
        }
    }

    /**
     * Очистить все кэши приложения
     */
    public void clearAllApplicationCaches() {
        String[] patterns = {"todo:*", "todos:*", "todos_paginated:*"};
        long totalDeleted = 0;

        for (String pattern : patterns) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                totalDeleted += keys.size();
                redisTemplate.delete(keys);
            }
        }

        logger.info("Cleared total {} cache keys", totalDeleted);
    }

    /**
     * Получить TTL для ключа
     */
    public Long getTtl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Установить TTL для ключа
     */
    public Boolean setTtl(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * Получить значение по ключу
     */
    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Получить все ключи с значениями по паттерну
     */
    public List<RedisKeyValue> getKeysWithValues(String pattern) {
        List<RedisKeyValue> result = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null) {
            for (String key : keys) {
                Object value = redisTemplate.opsForValue().get(key);
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                result.add(new RedisKeyValue(key, value, ttl));
            }
        }

        return result;
    }

    /**
     * Получить информацию о памяти (приблизительно)
     */
    private long getMemoryUsage(Set<String> keys) {
        return keys.size() * 100L;
    }

    /**
     * Получить информацию о Redis сервере
     */
    public RedisServerInfo getServerInfo() {
        return new RedisServerInfo("localhost", 6379, "standalone");
    }

    public static class RedisCacheStats {
        private final List<RedisCacheInfo> caches;
        private final long totalKeys;
        private final long estimatedMemory;

        public RedisCacheStats(List<RedisCacheInfo> caches) {
            this.caches = caches;
            this.totalKeys = caches.stream().mapToLong(RedisCacheInfo::getKeyCount).sum();
            this.estimatedMemory = caches.stream().mapToLong(RedisCacheInfo::getEstimatedMemory).sum();
        }

        public List<RedisCacheInfo> getCaches() { return caches; }
        public long getTotalKeys() { return totalKeys; }
        public long getEstimatedMemory() { return estimatedMemory; }
    }

    public static class RedisCacheInfo {
        private final String pattern;
        private final long keyCount;
        private final long estimatedMemory;

        public RedisCacheInfo(String pattern, long keyCount, long estimatedMemory) {
            this.pattern = pattern;
            this.keyCount = keyCount;
            this.estimatedMemory = estimatedMemory;
        }

        public String getPattern() { return pattern; }
        public long getKeyCount() { return keyCount; }
        public long getEstimatedMemory() { return estimatedMemory; }
    }

    public static class RedisKeyValue {
        private final String key;
        private final Object value;
        private final Long ttl;

        public RedisKeyValue(String key, Object value, Long ttl) {
            this.key = key;
            this.value = value;
            this.ttl = ttl;
        }

        public String getKey() { return key; }
        public Object getValue() { return value; }
        public Long getTtl() { return ttl; }
    }

    public static class RedisServerInfo {
        private final String host;
        private final int port;
        private final String mode;

        public RedisServerInfo(String host, int port, String mode) {
            this.host = host;
            this.port = port;
            this.mode = mode;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getMode() { return mode; }
    }
}
