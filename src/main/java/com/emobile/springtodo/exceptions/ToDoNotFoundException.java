package com.emobile.springtodo.exceptions;

public class ToDoNotFoundException extends RuntimeException {
    public ToDoNotFoundException(String message) {
        super(message);
    }

    public ToDoNotFoundException(Long id) {
        super("Задача с ID " + id + " не найдена");
    }
}