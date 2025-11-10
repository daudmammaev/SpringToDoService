package com.emobile.springtodo.controllers;

import com.emobile.springtodo.services.ToDoServices;
import com.emobile.springtodo.utils.RedisCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class RedisCacheManagementController {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheManagementController.class);
    @Autowired
    private final RedisCacheUtils redisCacheUtils;
    private final ToDoServices toDoService;

    public RedisCacheManagementController(RedisCacheUtils redisCacheUtils, ToDoServices toDoService) {
        this.redisCacheUtils = redisCacheUtils;
        this.toDoService = toDoService;
    }

    public ResponseEntity<RedisCacheUtils.RedisCacheStats> getRedisCacheStats() {
        RedisCacheUtils.RedisCacheStats stats = redisCacheUtils.getCacheStats();
        logger.info("Redis cache stats requested - total keys: {}", stats.getTotalKeys());
        return ResponseEntity.ok(stats);
    }

    public ResponseEntity<RedisCacheUtils.RedisServerInfo> getServerInfo() {
        RedisCacheUtils.RedisServerInfo serverInfo = redisCacheUtils.getServerInfo();
        return ResponseEntity.ok(serverInfo);
    }

    public ResponseEntity<List<RedisCacheUtils.RedisKeyValue>> getKeysWithValues(String pattern) {
        List<RedisCacheUtils.RedisKeyValue> keysWithValues = redisCacheUtils.getKeysWithValues(pattern);
        return ResponseEntity.ok(keysWithValues);
    }

    public ResponseEntity<Object> getValueByKey(String key) {
        Object value = redisCacheUtils.getValue(key);
        if (value != null) {
            return ResponseEntity.ok(value);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Void> setKeyTtl(String key, long ttl, TimeUnit timeUnit) {
        Boolean result = redisCacheUtils.setTtl(key, ttl, timeUnit);
        if (Boolean.TRUE.equals(result)) {
            logger.info("Set TTL for key {}: {} {}", key, ttl, timeUnit);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Void> clearCacheByPattern(String pattern) {
        redisCacheUtils.clearCacheByPattern(pattern);
        logger.info("Cleared cache for pattern: {}", pattern);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> clearAllRedisCaches() {
        redisCacheUtils.clearAllApplicationCaches();
        logger.info("Cleared all Redis caches");
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> reloadCache() {
        redisCacheUtils.clearAllApplicationCaches();

        toDoService.allItemWithPagination(10, 0);
        toDoService.allItem();

        logger.info("Cache reload completed");
        return ResponseEntity.ok().build();
    }
}