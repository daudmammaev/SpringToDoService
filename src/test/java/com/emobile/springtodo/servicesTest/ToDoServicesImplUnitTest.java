package com.emobile.springtodo.servicesTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.mappers.ToDoMapRow;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.services.ToDoServicesImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Юнит тесты сервиса задач")
class ToDoServicesImplUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ToDoMapRow toDoMapRow;

    private ToDoServicesImpl toDoServices;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        toDoServices = new ToDoServicesImpl(meterRegistry, toDoMapRow);

        // Устанавливаем мок JdbcTemplate через reflection
        var jdbcTemplateField = Arrays.stream(ToDoServicesImpl.class.getDeclaredFields())
                .filter(field -> field.getType().equals(JdbcTemplate.class))
                .findFirst()
                .orElseThrow();
        jdbcTemplateField.setAccessible(true);
        try {
            jdbcTemplateField.set(toDoServices, jdbcTemplate);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Получение задачи по ID - задача существует, должен вернуть DTO")
    void getItem_WhenItemExists_ShouldReturnDto() {

        long id = 1L;
        ToDo mockToDo = new ToDo();
        mockToDo.setId(id);
        mockToDo.setText("Test task");

        when(jdbcTemplate.queryForObject(anyString(), any(ToDoMapRow.class), eq(id)))
                .thenReturn(mockToDo);

        DtoToDo result = toDoServices.getItem(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(jdbcTemplate).queryForObject(anyString(), any(ToDoMapRow.class), eq(id));
    }

    @Test
    @DisplayName("Получение задачи по ID - задача не существует, должен выбросить исключение")
    void getItem_WhenItemNotExists_ShouldThrowException() {

        long nonExistentId = 999L;
        when(jdbcTemplate.queryForObject(anyString(), any(ToDoMapRow.class), eq(nonExistentId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ToDoNotFoundException exception = assertThrows(
                ToDoNotFoundException.class,
                () -> toDoServices.getItem(nonExistentId)
        );
        assertEquals("Задача с ID 999 не найдена", exception.getMessage());
    }

    @Test
    @DisplayName("Создание задачи - валидные данные, должен создать и инкрементировать счетчик")
    void addItem_WhenValidData_ShouldCreateAndIncrementCounter() {

        DtoToDo dto = new DtoToDo();
        dto.setText("New task");
        double initialCount = meterRegistry.counter("orders").count();

        when(jdbcTemplate.update(anyString(), anyLong(), anyString()))
                .thenReturn(1);

        DtoToDo result = toDoServices.addItem(dto);

        assertNotNull(result);
        assertEquals("New task", result.getText());
        double finalCount = meterRegistry.counter("orders").count();
        assertEquals(initialCount + 1, finalCount);
        verify(jdbcTemplate).update(anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Удаление задачи - задача существует, должен вернуть количество удаленных строк")
    void deleteItem_WhenItemExists_ShouldReturnDeletedCount() {

        long id = 1L;
        when(jdbcTemplate.update(anyString(), eq(id)))
                .thenReturn(1);

        long result = toDoServices.deleteItem(id);

        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), eq(id));
    }

    @Test
    @DisplayName("Получение всех задач - должен вернуть список DTO")
    void allItem_WhenCalled_ShouldReturnDtoList() {

        List<ToDo> mockToDos = Arrays.asList(
                createToDo(1L, "Task 1"),
                createToDo(2L, "Task 2")
        );

        when(jdbcTemplate.query(anyString(), any(ToDoMapRow.class)))
                .thenReturn(mockToDos);

        List<DtoToDo> result = toDoServices.allItem();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jdbcTemplate).query(anyString(), any(ToDoMapRow.class));
    }

    private ToDo createToDo(long id, String text) {
        ToDo toDo = new ToDo();
        toDo.setId(id);
        toDo.setText(text);
        return toDo;
    }
}