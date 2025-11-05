package com.emobile.springtodo.exceptionTest;

import com.emobile.springtodo.controllers.ToDoRestContoller;
import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.services.ToDoServicesImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ToDoRestContoller.class)
@DisplayName("Тесты REST контроллера для управления задачами")
class ToDoRestContollerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ToDoServicesImpl toDoServices;

    @Test
    @DisplayName("Получение задачи по ID - задача не найдена, должен вернуть 404")
    void getItem_WhenItemNotFound_ShouldReturn404() throws Exception {

        long nonExistentId = 999L;
        DtoToDo requestDto = new DtoToDo();
        requestDto.setId(nonExistentId);

        when(toDoServices.getItem(nonExistentId))
                .thenThrow(new ToDoNotFoundException(nonExistentId));

        mockMvc.perform(post("/getItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Задача не найдена"))
                .andExpect(jsonPath("$.message").value("Задача с ID " + nonExistentId + " не найдена"));
    }

    @Test
    @DisplayName("Получение задачи по ID - невалидный ID, должен вернуть 400")
    void getItem_WhenInvalidId_ShouldReturn400() throws Exception {

        DtoToDo requestDto = new DtoToDo(); // ID не установлен

        mockMvc.perform(post("/getItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание задачи - пустой текст, должен вернуть 400")
    void addItem_WhenInvalidText_ShouldReturn400() throws Exception {

        DtoToDo invalidDto = new DtoToDo();
        invalidDto.setText(""); // Пустой текст

        mockMvc.perform(post("/addItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }

    @Test
    @DisplayName("Создание задачи - текст превышает лимит, должен вернуть 400")
    void addItem_WhenTextTooLong_ShouldReturn400() throws Exception {

        DtoToDo invalidDto = new DtoToDo();
        String longText = "A".repeat(501); // Превышает лимит в 500 символов
        invalidDto.setText(longText);

        mockMvc.perform(post("/addItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }

    @Test
    @DisplayName("Обновление задачи - задача не найдена, должен вернуть 404")
    void updateItem_WhenItemNotFound_ShouldReturn404() throws Exception {

        long nonExistentId = 999L;
        DtoToDo updateDto = new DtoToDo();
        updateDto.setId(nonExistentId);
        updateDto.setText("Valid text");

        when(toDoServices.updateItem(any(DtoToDo.class)))
                .thenThrow(new ToDoNotFoundException(nonExistentId));

        mockMvc.perform(put("/updateItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение задач с пагинацией - невалидные параметры, должен вернуть 400")
    void getAllWithPagination_WhenInvalidParameters_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/getAllWithPaginatuion/-1/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Удаление задачи - задача не найдена, должен вернуть 404")
    void deleteItem_WhenItemNotFound_ShouldReturn404() throws Exception {

        long nonExistentId = 999L;
        doThrow(new ToDoNotFoundException(nonExistentId))
                .when(toDoServices).deleteItem(nonExistentId);

        mockMvc.perform(delete("/deleteItem/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех задач - внутренняя ошибка сервера, должен вернуть 500")
    void getAllItem_WhenServerError_ShouldReturn500() throws Exception {

        when(toDoServices.allItem())
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/getAll"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"));
    }

    @Test
    @DisplayName("Создание задачи - успешное создание, должен вернуть 200")
    void addItem_WhenValidData_ShouldReturn200() throws Exception {

        DtoToDo validDto = new DtoToDo();
        validDto.setText("Valid task text");

        when(toDoServices.addItem(any(DtoToDo.class)))
                .thenReturn(validDto);

        mockMvc.perform(post("/addItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение задачи по ID - успешное получение, должен вернуть 200")
    void getItem_WhenItemExists_ShouldReturn200() throws Exception {

        long existingId = 1L;
        DtoToDo requestDto = new DtoToDo();
        requestDto.setId(existingId);

        DtoToDo responseDto = new DtoToDo();
        responseDto.setId(existingId);
        responseDto.setText("Existing task");

        when(toDoServices.getItem(existingId))
                .thenReturn(responseDto);

        mockMvc.perform(post("/getItem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.text").value("Existing task"));
    }
}