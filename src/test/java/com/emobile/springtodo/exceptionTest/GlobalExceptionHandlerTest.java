package com.emobile.springtodo.exceptionTest;

import com.emobile.springtodo.exceptions.ErrorResponse;
import com.emobile.springtodo.exceptions.GlobalExceptionHandler;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Тесты глобального обработчика исключений")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Обработка ToDoNotFoundException - должен вернуть 404")
    void handleToDoNotFoundException_ShouldReturn404() {

        long taskId = 1L;
        ToDoNotFoundException exception = new ToDoNotFoundException(taskId);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleToDoNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Задача не найдена", response.getBody().getError());
        assertEquals("Задача с ID 1 не найдена", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Обработка общего исключения - должен вернуть 500")
    void handleGlobalException_ShouldReturn500() {

        Exception exception = new RuntimeException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Внутренняя ошибка сервера", response.getBody().getError());
        assertEquals("Test error", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Обработка IllegalArgumentException - должен вернуть 400")
    void handleIllegalArgumentException_ShouldReturn400() {

        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Неверные параметры запроса", response.getBody().getError());
        assertEquals("Invalid parameter", response.getBody().getMessage());
    }
}
