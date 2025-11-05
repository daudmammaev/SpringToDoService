package com.emobile.springtodo.controllers;

import com.emobile.springtodo.utils.CacheUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheManagementController {

    private final CacheUtils cacheUtils;

    public CacheManagementController(CacheUtils cacheUtils) {
        this.cacheUtils = cacheUtils;
    }

    @Operation(summary = "Получить статистику кэша")
    @GetMapping("/stats")
    public ResponseEntity<CacheUtils.CacheStats> getCacheStats() {
        CacheUtils.CacheStats stats = cacheUtils.getCacheStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Очистить конкретный кэш")
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> clearCache(String cacheName) {
        cacheUtils.clearCache(cacheName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить элемент из кэша")
    @GetMapping("/{cacheName}/{key}")
    public ResponseEntity<Void> clearAllCaches() {
        cacheUtils.clearAllCaches();
        return ResponseEntity.ok().build();
    }
}