package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;

import java.util.List;

public interface ToDoServices {
    DtoToDo addItem(DtoToDo dtoToDo);
    PaginatedResponse<DtoToDo> allItemWithPagination(int limit, int offset);
    long deleteItem(long id);
    DtoToDo updateItem(DtoToDo dtoToDo);
    DtoToDo getItem(long id);
    List<DtoToDo> allItem();
}
