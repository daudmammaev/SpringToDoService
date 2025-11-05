package com.emobile.springtodo.configurations;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    public static final String TODOS_CACHE = "todos";
    public static final String TODO_CACHE = "todo";
    public static final String TODOS_PAGINATED_CACHE = "todos_paginated";

    // Ключи для кэша
    public static final String ALL_TODOS_KEY = "'all_todos'";
    public static final String TODO_BY_ID_KEY = "'todo_' + #id";
    public static final String PAGINATED_TODOS_KEY = "'todos_paginated_' + #limit + '_' + #offset";
    public static final String SEARCH_TODOS_KEY = "'todos_search_' + #searchText + '_' + #limit + '_' + #offset";
    public static final String STATUS_TODOS_KEY = "'todos_status_' + #completed + '_' + #limit + '_' + #offset";

    @Bean
    @Override
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
                TODOS_CACHE,
                TODO_CACHE,
                TODOS_PAGINATED_CACHE
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}