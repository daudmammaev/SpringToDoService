package com.emobile.springtodo.redisTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.services.ToDoServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ToDoServiceRedisCacheTest {

    @Autowired
    private ToDoServices toDoService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("Должен кэшировать задачу в Redis")
    void shouldCacheItemInRedis() {

        DtoToDo todo = new DtoToDo(1, "Redis cached todo");
        DtoToDo savedTodo = toDoService.addItem(todo);
        Long todoId = savedTodo.getId();

        DtoToDo result1 = toDoService.getItem(todoId);


        DtoToDo result2 = toDoService.getItem(todoId);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        Object cachedValue = redisTemplate.opsForValue().get("todo:todo_" + todoId);
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue).isInstanceOf(DtoToDo.class);

        DtoToDo cachedTodo = (DtoToDo) cachedValue;
        assertThat(cachedTodo.getText()).isEqualTo("Redis cached todo");
    }

    @Test
    @DisplayName("Должен инвалидировать Redis кэш при обновлении")
    void shouldEvictRedisCacheOnUpdate() {

        DtoToDo todo = new DtoToDo(1, "Original");
        DtoToDo savedTodo = toDoService.addItem(todo);
        Long todoId = savedTodo.getId();

        toDoService.getItem(todoId);

        savedTodo.setText("Updated in Redis");
        toDoService.updateItem(savedTodo);

        Object cachedValue = redisTemplate.opsForValue().get("todo:todo_" + todoId);
        assertThat(cachedValue).isNotNull();

        DtoToDo cachedTodo = (DtoToDo) cachedValue;
        assertThat(cachedTodo.getText()).isEqualTo("Updated in Redis");
    }

    @Test
    @DisplayName("Должен устанавливать TTL для кэша в Redis")
    void shouldSetTtlForRedisCache() throws InterruptedException {

        DtoToDo todo = new DtoToDo(1, "TTL test");
        DtoToDo savedTodo = toDoService.addItem(todo);
        Long todoId = savedTodo.getId();

        toDoService.getItem(todoId);

        Long ttl = redisTemplate.getExpire("todo:todo_" + todoId);
        assertThat(ttl).isNotNull().isGreaterThan(0);
    }
}
