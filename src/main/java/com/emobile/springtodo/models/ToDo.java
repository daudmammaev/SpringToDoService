package com.emobile.springtodo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
public class ToDo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotBlank(message = "ID не может быть пустым")
    private long id;

    @NotBlank(message = "Текст задачи не может быть пустым")
    @Size(min = 1, max = 500, message = "Текст задачи должен содержать от 1 до 500 символов")
    private String text;

    //private boolean done;
}
