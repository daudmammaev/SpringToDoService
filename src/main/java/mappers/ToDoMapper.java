package mappers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;


@Mapper(componentModel = "spring")
public interface ToDoMapper {

    ToDoMapper INSTANCE = Mappers.getMapper(ToDoMapper.class);

    DtoToDo toDoToDto(ToDo toDo);

    ToDo dtoToToDo(DtoToDo dto);

}
