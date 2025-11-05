
package com.emobile.springtodo.servicesTest;

import com.emobile.springtodo.models.ToDo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Data JPA тесты для работы с БД")
class ToDoRepositoryDataJpaTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Проверка подключения к тестовой БД")
    void contextLoads() {
        assertNotNull(dataSource);
        assertNotNull(jdbcTemplate);
    }

    @Test
    @DisplayName("Вставка и выборка данных - должен корректно работать с БД")
    @Sql(scripts = "/insert-test-data.sql")
    void whenInsertData_thenCanSelectIt() {

        List<ToDo> todos = jdbcTemplate.query(
                "SELECT * FROM to_do ORDER BY id",
                (rs, rowNum) -> {
                    ToDo toDo = new ToDo();
                    toDo.setId(rs.getLong("id"));
                    toDo.setText(rs.getString("text"));
                    return toDo;
                });

        assertEquals(3, todos.size());
        assertEquals("Первая тестовая задача", todos.get(0).getText());
        assertEquals("Вторая тестовая задача", todos.get(1).getText());
        assertEquals("Третья тестовая задача", todos.get(2).getText());
    }

    @Test
    @DisplayName("Очистка БД - должен удалять все данные")
    @Sql(scripts = "/insert-test-data.sql")
    void whenDeleteAll_thenTableIsEmpty() {

        Integer countBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM to_do", Integer.class);
        assertEquals(3, countBefore);

        jdbcTemplate.execute("DELETE FROM to_do");

        Integer countAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM to_do", Integer.class);
        assertEquals(0, countAfter);
    }
}