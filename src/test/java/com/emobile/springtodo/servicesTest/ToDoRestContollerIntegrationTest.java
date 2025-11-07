package com.emobile.springtodo.servicesTest;

import com.emobile.springtodo.dto.DtoToDo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Интеграционные тесты REST контроллера задач")
class ToDoRestContollerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Получение всех задач - должен вернуть пустой список при отсутствии данных")
    void getAllItems_WhenNoData_ShouldReturnEmptyList() throws Exception {

        ResponseEntity<String> response = restTemplate.getForEntity("/getAll", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JSONAssert.assertEquals("[]", response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Получение всех задач - должен вернуть список задач")
    @Sql(scripts = "/sql/insert-test-data.sql")
    void getAllItems_WhenDataExists_ShouldReturnList() throws Exception {

        ResponseEntity<String> response = restTemplate.getForEntity("/getAll", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedJson = """
            [
                {"id": 1, "text": "Первая тестовая задача"},
                {"id": 2, "text": "Вторая тестовая задача"},
                {"id": 3, "text": "Третья тестовая задача"}
            ]
        """;
        JSONAssert.assertEquals(expectedJson, response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Создание новой задачи - должен успешно создать задачу")
    void addItem_WhenValidData_ShouldCreateItem() throws Exception {

        DtoToDo newTask = new DtoToDo();
        newTask.setText("Новая задача для тестирования");


        ResponseEntity<DtoToDo> response = restTemplate.postForEntity(
                "/addItem", newTask, DtoToDo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новая задача для тестирования", response.getBody().getText());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM to_do WHERE text = ?", Integer.class,
                "Новая задача для тестирования");
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Получение задачи по ID - должен вернуть задачу")
    @Sql(scripts = "/sql/insert-single-task.sql")
    void getItem_WhenItemExists_ShouldReturnItem() throws Exception {

        DtoToDo request = new DtoToDo();
        request.setId(10L);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/getItem", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedJson = """
            {"id": 10, "text": "Одиночная тестовая задача"}
        """;
        JSONAssert.assertEquals(expectedJson, response.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    @DisplayName("Получение задачи по ID - несуществующий ID, должен вернуть 404")
    void getItem_WhenItemNotExists_ShouldReturn404() throws Exception {

        DtoToDo request = new DtoToDo();
        request.setId(999L);


        ResponseEntity<String> response = restTemplate.postForEntity(
                "/getItem", request, String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String expectedJson = """
            {
                "status": 404,
                "error": "Задача не найдена",
                "message": "Задача с ID 999 не найдена"
            }
        """;
        JSONAssert.assertEquals(expectedJson, response.getBody(), JSONCompareMode.LENIENT);
    }

    @Test
    @DisplayName("Обновление задачи - должен успешно обновить задачу")
    @Sql(scripts = "/sql/insert-single-task.sql")
    void updateItem_WhenItemExists_ShouldUpdateItem() throws Exception {

        DtoToDo updateRequest = new DtoToDo();
        updateRequest.setId(10L);
        updateRequest.setText("Обновленный текст задачи");


        ResponseEntity<DtoToDo> response = restTemplate.exchange(
                "/updateItem", HttpMethod.PUT,
                new HttpEntity<>(updateRequest), DtoToDo.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Обновленный текст задачи", response.getBody().getText());

        String text = jdbcTemplate.queryForObject(
                "SELECT text FROM to_do WHERE id = ?", String.class, 10L);
        assertEquals("Обновленный текст задачи", text);
    }

    @Test
    @DisplayName("Удаление задачи - должен успешно удалить задачу")
    @Sql(scripts = "/sql/insert-single-task.sql")
    void deleteItem_WhenItemExists_ShouldDeleteItem() throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
                "/deleteItem/10", HttpMethod.DELETE, null, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Задача успешно удалена", response.getBody());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM to_do WHERE id = ?", Integer.class, 10L);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Получение задач с пагинацией - должен вернуть пагинированный результат")
    @Sql(scripts = "/sql/insert-test-data.sql")
    void getAllItemsWithPagination_ShouldReturnPaginatedResult() throws Exception {

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/getAllWithPaginatuion/2/0", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedJson = """
            {
                "data": [
                    {"id": 1, "text": "Первая тестовая задача"},
                    {"id": 2, "text": "Вторая тестовая задача"}
                ],
                "meta": {
                    "limit": 2,
                    "offset": 0,
                    "total": 3,
                    "hasNext": true,
                    "hasPrevious": false
                }
            }
        """;
        JSONAssert.assertEquals(expectedJson, response.getBody(), JSONCompareMode.LENIENT);
    }
}