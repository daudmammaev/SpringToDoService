package com.emobile.springtodo.cacheTest;

import com.emobile.springtodo.configurations.CacheConfig;
import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import com.emobile.springtodo.services.ToDoServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ToDoServiceCacheTest {

    @Autowired
    private ToDoServicesImpl toDoService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {

        cacheManager.getCacheNames()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Transactional
    @Test
    @DisplayName("Должен кэшировать получение задачи по ID")
    void shouldCacheGetItemById() {

        DtoToDo todo = new DtoToDo(1, "Test todo");
        DtoToDo savedTodo = toDoService.addItem(todo);
        long todoId = savedTodo.getId();

        DtoToDo result1 = toDoService.getItem(todoId);

        DtoToDo result2 = toDoService.getItem(todoId);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getText()).isEqualTo("Test todo");
        assertThat(result2.getText()).isEqualTo("Test todo");

        Cache cache = cacheManager.getCache(CacheConfig.TODO_CACHE);
        assertThat(cache).isNotNull();

        Object cachedValue = cache.get("todo_" + todoId).get();
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue).isInstanceOf(DtoToDo.class);
    }

    @Transactional
    @Test
    @DisplayName("Должен инвалидировать кэш при обновлении задачи")
    void shouldEvictCacheOnUpdate() {

        DtoToDo todo = new DtoToDo(1, "Original");
        DtoToDo savedTodo = toDoService.addItem(todo);
        Long todoId = savedTodo.getId();

        toDoService.getItem(todoId);

        savedTodo.setText("Updated");
        toDoService.updateItem(savedTodo);

        Cache cache = cacheManager.getCache(CacheConfig.TODO_CACHE);
        DtoToDo cachedTodo = cache.get("todo_" + todoId, DtoToDo.class);
        assertThat(cachedTodo).isNotNull();
        assertThat(cachedTodo.getText()).isEqualTo("Updated");
    }

    @Transactional
    @Test
    @DisplayName("Должен инвалидировать кэш списков при создании новой задачи")
    void shouldEvictListCachesOnCreate() {

        toDoService.allItem();
        toDoService.allItemWithPagination(10, 0);

        DtoToDo newTodo = new DtoToDo(1, "New task");
        toDoService.addItem(newTodo);

        Cache todosCache = cacheManager.getCache(CacheConfig.TODOS_CACHE);
        Cache paginatedCache = cacheManager.getCache(CacheConfig.TODOS_PAGINATED_CACHE);

        assertThat(todosCache.get("all_todos")).isNull();
        assertThat(paginatedCache.get("todos_paginated_10_0")).isNull();
    }

    @Transactional
    @Test
    @DisplayName("Должен кэшировать результаты поиска")
    void shouldCacheSearchResults() {

        toDoService.addItem(new DtoToDo(1, "Test search item"));

        PaginatedResponse<DtoToDo> result1 = toDoService.searchItems("test", 10, 0);
        PaginatedResponse<DtoToDo> result2 = toDoService.searchItems("test", 10, 0);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getData().size()).isEqualTo(result2.getData().size());

        Cache cache = cacheManager.getCache(CacheConfig.TODOS_PAGINATED_CACHE);
        Object cachedResult = cache.get("todos_search_test_10_0");
        assertThat(cachedResult).isNotNull();
    }
}