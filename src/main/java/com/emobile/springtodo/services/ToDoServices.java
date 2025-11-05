package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static com.emobile.springtodo.configurations.CacheConfig.*;


/**
 * Сервис для управления задачами (ToDo) с кэшированием
 */
@CacheConfig(cacheNames = TODOS_CACHE)
public interface ToDoServices {

    /**
     * Создать новую задачу и инвалидировать кэш списков
     */
    @CacheEvict(value = {
            TODOS_CACHE,
            TODOS_PAGINATED_CACHE
    }, allEntries = true)
    DtoToDo addItem(DtoToDo dtoToDo);

    /**
     * Получить задачи с пагинацией с кэшированием
     */
    @Cacheable(value = TODOS_PAGINATED_CACHE,
            key = PAGINATED_TODOS_KEY)
    PaginatedResponse<DtoToDo> allItemWithPagination(int limit, int offset);

    /**
     * Удалить задачу и инвалидировать кэши
     */
    @CacheEvict(value = {
            TODO_CACHE,
            TODOS_CACHE,
            TODOS_PAGINATED_CACHE
    }, allEntries = true)
    long deleteItem(long id);

    /**
     * Обновить задачу и обновить кэш
     */
    @CachePut(value = TODO_CACHE, key = TODO_BY_ID_KEY)
    @CacheEvict(value = {
            TODOS_CACHE,
            TODOS_PAGINATED_CACHE
    }, allEntries = true)
    DtoToDo updateItem(DtoToDo dtoToDo);

    /**
     * Получить задачу по ID с кэшированием
     */
    @Cacheable(value = TODO_CACHE, key = TODO_BY_ID_KEY)
    DtoToDo getItem(long id);

    /**
     * Получить все задачи с кэшированием
     */
    @Cacheable(value = TODOS_CACHE, key = ALL_TODOS_KEY)
    List<DtoToDo> allItem();

    /**
     * Поиск задач с пагинацией и кэшированием
     */
    @Cacheable(value = TODOS_PAGINATED_CACHE,
            key = SEARCH_TODOS_KEY)
    PaginatedResponse<DtoToDo> searchItems(String searchText, int limit, int offset);

    /**
     * Очистить все кэши
     */
    @CacheEvict(value = {
            TODO_CACHE,
            TODOS_CACHE,
            TODOS_PAGINATED_CACHE
    }, allEntries = true)
    void clearAllCaches();
}
