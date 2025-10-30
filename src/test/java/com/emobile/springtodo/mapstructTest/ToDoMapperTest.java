package com.emobile.springtodo.mapstructTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import mappers.ToDoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;

class ToDoMapperTest {

    private ToDoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = ToDoMapper.INSTANCE;
    }

    @Test
    @DisplayName("Должен корректно маппить ToDo ")
    void shouldMapToDoToDto() {

        ToDo toDo = new ToDo();
        toDo.setId(1L);
        toDo.setText("Test task");

        DtoToDo dto = mapper.toDoToDto(toDo);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Test task");
    }

    @Test
    @DisplayName("Должен корректно маппить DtoToDo ")
    void shouldMapDtoToToDo() {

        DtoToDo dto = new DtoToDo();
        dto.setId(2L);
        dto.setText("DTO task");

        ToDo toDo = mapper.dtoToToDo(dto);

        assertThat(toDo).isNotNull();
        assertThat(toDo.getId()).isEqualTo(2L);
        assertThat(toDo.getText()).isEqualTo("DTO task");
    }

    @Test
    @DisplayName("Должен корректно маппить ToDo c null")
    void shouldMapNullToDoToNullDto() {

        DtoToDo dto = mapper.toDoToDto(null);

        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("Должен корректно маппить DtoToDo c null")
    void shouldMapNullDtoToNullToDo() {

        ToDo toDo = mapper.dtoToToDo(null);

        assertThat(toDo).isNull();
    }
}