package com.emobile.springtodo.services;

import com.emobile.springtodo.utils.RedisCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RedisHealthService {

    private static final Logger logger = LoggerFactory.getLogger(RedisHealthService.class);

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisCacheUtils redisCacheUtils;

    public RedisHealthService(RedisConnectionFactory redisConnectionFactory, RedisCacheUtils redisCacheUtils) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisCacheUtils = redisCacheUtils;
    }

    /**
     * Проверка здоровья Redis каждые 30 секунд
     */
    @Scheduled(fixedRate = 30000)
    public void checkRedisHealth() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResponse = connection.ping();
            RedisCacheUtils.RedisCacheStats stats = redisCacheUtils.getCacheStats();

            logger.debug("Redis health check - Ping: {}, Total keys: {}",
                    pingResponse, stats.getTotalKeys());

        } catch (Exception e) {
            logger.error("Redis health check failed: {}", e.getMessage());
        }
    }

    /**
     * Очистка устаревших кэшей каждые 10 минут
     */
    @Scheduled(fixedRate = 600000)
    public void cleanupExpiredCaches() {
        try {
            RedisCacheUtils.RedisCacheStats statsBefore = redisCacheUtils.getCacheStats();

            RedisCacheUtils.RedisCacheStats statsAfter = redisCacheUtils.getCacheStats();
            logger.info("Cache cleanup completed - Before: {} keys, After: {} keys",
                    statsBefore.getTotalKeys(), statsAfter.getTotalKeys());

        } catch (Exception e) {
            logger.error("Cache cleanup failed: {}", e.getMessage());
        }
    }

    /**
     * Получить статус Redis
     */
    public RedisHealthStatus getHealthStatus() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String ping = connection.ping();
            RedisCacheUtils.RedisCacheStats stats = redisCacheUtils.getCacheStats();

            return new RedisHealthStatus(
                    "UP",
                    ping,
                    stats.getTotalKeys(),
                    stats.getEstimatedMemory()
            );
        } catch (Exception e) {
            return new RedisHealthStatus("DOWN", e.getMessage(), 0, 0);
        }
    }

    public static class RedisHealthStatus {
        private final String status;
        private final String ping;
        private final long totalKeys;
        private final long estimatedMemory;

        public RedisHealthStatus(String status, String ping, long totalKeys, long estimatedMemory) {
            this.status = status;
            this.ping = ping;
            this.totalKeys = totalKeys;
            this.estimatedMemory = estimatedMemory;
        }

        public String getStatus() { return status; }
        public String getPing() { return ping; }
        public long getTotalKeys() { return totalKeys; }
        public long getEstimatedMemory() { return estimatedMemory; }
    }
}