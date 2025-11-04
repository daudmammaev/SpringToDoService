package com.emobile.springtodo.mappers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface ToDoMapper {

    ToDoMapper INSTANCE = Mappers.getMapper(ToDoMapper.class);

    DtoToDo toDoToDto(ToDo toDo);

    ToDo dtoToToDo(DtoToDo dto);

}
