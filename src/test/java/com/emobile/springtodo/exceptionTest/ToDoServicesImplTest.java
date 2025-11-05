package com.emobile.springtodo.exceptionTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.mappers.ToDoMapRow;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.services.ToDoServicesImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса для управления задачами")
class ToDoServicesImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ToDoMapRow toDoMapRow;

    @InjectMocks
    private ToDoServicesImpl toDoServices;

    @Test
    @DisplayName("Получение задачи по ID - задача существует, должен вернуть задачу")
    void getItem_WhenItemExists_ShouldReturnItem() {

        long id = 1L;
        ToDo mockToDo = new ToDo();
        mockToDo.setId(id);
        mockToDo.setText("Test task");

        when(jdbcTemplate.queryForObject(anyString(), any(ToDoMapRow.class), eq(id)))
                .thenReturn(mockToDo);

        DtoToDo result = toDoServices.getItem(id);

        assertNotNull(result);
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
    @DisplayName("Обновление задачи - задача не существует, должен выбросить исключение")
    void updateItem_WhenItemNotExists_ShouldThrowException() {

        DtoToDo dto = new DtoToDo();
        dto.setId(999L);
        dto.setText("Test text");

        when(jdbcTemplate.update(anyString(), anyString(), anyLong()))
                .thenReturn(0);

        ToDoNotFoundException exception = assertThrows(
                ToDoNotFoundException.class,
                () -> toDoServices.updateItem(dto)
        );

        assertEquals("Задача с ID 999 не найдена", exception.getMessage());
    }

    @Test
    @DisplayName("Удаление задачи - задача не существует, должен выбросить исключение")
    void deleteItem_WhenItemNotExists_ShouldThrowException() {

        long nonExistentId = 999L;
        when(jdbcTemplate.update(anyString(), eq(nonExistentId)))
                .thenReturn(0);

        ToDoNotFoundException exception = assertThrows(
                ToDoNotFoundException.class,
                () -> toDoServices.deleteItem(nonExistentId)
        );

        assertEquals("Задача с ID 999 не найдена", exception.getMessage());
    }

    @Test
    @DisplayName("Обновление задачи - задача существует, должен обновить успешно")
    void updateItem_WhenItemExists_ShouldUpdateSuccessfully() {

        DtoToDo dto = new DtoToDo();
        dto.setId(1L);
        dto.setText("Updated text");

        when(jdbcTemplate.update(anyString(), anyString(), anyLong()))
                .thenReturn(1);

        DtoToDo result = toDoServices.updateItem(dto);

        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getText(), result.getText());
        verify(jdbcTemplate).update(anyString(), eq(dto.getText()), eq(dto.getId()));
    }

    @Test
    @DisplayName("Удаление задачи - задача существует, должен удалить успешно")
    void deleteItem_WhenItemExists_ShouldDeleteSuccessfully() {

        long existingId = 1L;
        when(jdbcTemplate.update(anyString(), eq(existingId)))
                .thenReturn(1);

        long result = toDoServices.deleteItem(existingId);

        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), eq(existingId));
    }

    @Test
    @DisplayName("Создание задачи - валидные данные, должен создать успешно")
    void addItem_WhenValidData_ShouldAddSuccessfully() {

        DtoToDo dto = new DtoToDo();
        dto.setText("New task");

        when(jdbcTemplate.update(anyString(), anyLong(), anyString()))
                .thenReturn(1);

        DtoToDo result = toDoServices.addItem(dto);

        assertNotNull(result);
        assertEquals(dto.getText(), result.getText());
        verify(jdbcTemplate).update(anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("Поиск задач - по тексту, должен вернуть результаты")
    void searchItems_WhenTextProvided_ShouldReturnResults() {

        String searchText = "test";
        int limit = 10;
        int offset = 0;

        when(jdbcTemplate.query(anyString(), any(ToDoMapRow.class), eq(searchText)))
                .thenReturn(java.util.Collections.emptyList());

        var result = toDoServices.searchItems(searchText, limit, offset);

        assertNotNull(result);
        verify(jdbcTemplate).query(anyString(), any(ToDoMapRow.class), eq(searchText));
    }

    @Test
    @DisplayName("Получение всех задач - должен вернуть список задач")
    void allItem_WhenCalled_ShouldReturnAllItems() {

        when(jdbcTemplate.query(anyString(), any(ToDoMapRow.class)))
                .thenReturn(java.util.Collections.emptyList());

        var result = toDoServices.allItem();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jdbcTemplate).query(anyString(), any(ToDoMapRow.class));
    }
}