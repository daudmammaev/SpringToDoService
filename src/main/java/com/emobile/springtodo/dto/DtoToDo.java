package com.emobile.springtodo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DtoToDo {

    @NotNull(message = "ID не может быть null")
    @PositiveOrZero(message = "ID должен быть положительным числом или нулем")
    long id;

    @NotBlank(message = "Текст задачи не может быть пустым")
    @Size(min = 1, max = 500, message = "Текст задачи должен содержать от 1 до 500 символов")
    String text;
}
