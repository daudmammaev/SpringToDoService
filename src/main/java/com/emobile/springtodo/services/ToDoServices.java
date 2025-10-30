package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;

import java.util.List;

public interface ToDoServices {
    DtoToDo addItem(DtoToDo dtoToDo);
    long deleteItem(long id);
    DtoToDo updateItem(DtoToDo dtoToDo);
    DtoToDo getItem(long id);
    List<DtoToDo> allItem();
}
