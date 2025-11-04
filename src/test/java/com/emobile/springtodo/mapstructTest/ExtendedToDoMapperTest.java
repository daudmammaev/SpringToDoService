package com.emobile.springtodo.mapstructTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.mappers.ToDoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ExtendedToDoMapperTest {

    private ToDoMapper mapper = ToDoMapper.INSTANCE;

    @Nested
    @DisplayName("Тесты маппинга ToDo -> DtoToDo")
    class ToDoToDtoTests {

        @Test
        @DisplayName("Должен корректно маппить ToDo с максимальным ID")
        void shouldMapToDoWithMaxLongId() {

            ToDo toDo = new ToDo();
            toDo.setId(Long.MAX_VALUE);
            toDo.setText("Max ID task");

            DtoToDo dto = mapper.toDoToDto(toDo);

            assertThat(dto.getId()).isEqualTo(Long.MAX_VALUE);
            assertThat(dto.getText()).isEqualTo("Max ID task");
        }

        @Test
        @DisplayName("Должен корректно маппить ToDo с минимальным ID")
        void shouldMapToDoWithMinLongId() {

            ToDo toDo = new ToDo();
            toDo.setId(0L);
            toDo.setText("Min ID task");

            DtoToDo dto = mapper.toDoToDto(toDo);

            assertThat(dto.getId()).isEqualTo(0L);
            assertThat(dto.getText()).isEqualTo("Min ID task");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  "})
        @DisplayName("Должен корректно маппить ToDo с пустым текстом")
        void shouldMapToDoWithEmptyText(String text) {
            ToDo toDo = new ToDo();
            toDo.setId(5L);
            toDo.setText(text);

            DtoToDo dto = mapper.toDoToDto(toDo);

            assertThat(dto.getId()).isEqualTo(5L);
            assertThat(dto.getText()).isEqualTo(text);
        }

        @Test
        @DisplayName("Должен корректно маппить ToDo с очень длинным текстом")
        void shouldMapToDoWithLongText() {

            String longText = "A".repeat(1000);
            ToDo toDo = new ToDo();
            toDo.setId(10L);
            toDo.setText(longText);

            DtoToDo dto = mapper.toDoToDto(toDo);

            assertThat(dto.getId()).isEqualTo(10L);
            assertThat(dto.getText()).isEqualTo(longText);
        }
    }

    @Nested
    @DisplayName("Тесты маппинга DtoToDo -> ToDo")
    class DtoToToDoTests {

        @Test
        @DisplayName("Должен корректно маппить DtoToDo с отрицательным ID")
        void shouldMapDtoWithNegativeId() {

            DtoToDo dto = new DtoToDo();
            dto.setId(-1L);
            dto.setText("Negative ID task");

            ToDo toDo = mapper.dtoToToDo(dto);

            assertThat(toDo.getId()).isEqualTo(-1L);
            assertThat(toDo.getText()).isEqualTo("Negative ID task");
        }

        @Test
        @DisplayName("Должен корректно маппить DtoToDo со специальными символами")
        void shouldMapDtoWithSpecialCharacters() {
            DtoToDo dto = new DtoToDo();
            dto.setId(15L);
            dto.setText("Task with spéciäl chäräctérs! @#$%^&*()");

            ToDo toDo = mapper.dtoToToDo(dto);

            assertThat(toDo.getId()).isEqualTo(15L);
            assertThat(toDo.getText()).isEqualTo("Task with spéciäl chäräctérs! @#$%^&*()");
        }
    }

    @Nested
    @DisplayName("Тесты двустороннего маппинга")
    class BidirectionalMappingTests {

        @Test
        @DisplayName("Должен сохранять данные при двустороннем преобразовании")
        void shouldPreserveDataInBidirectionalMapping() {
            ToDo originalToDo = new ToDo();
            originalToDo.setId(100L);
            originalToDo.setText("Original task");

            DtoToDo dto = mapper.toDoToDto(originalToDo);
            ToDo convertedToDo = mapper.dtoToToDo(dto);

            assertThat(convertedToDo.getId()).isEqualTo(originalToDo.getId());
            assertThat(convertedToDo.getText()).isEqualTo(originalToDo.getText());
        }

        @Test
        @DisplayName("Должен создавать разные объекты при преобразовании")
        void shouldCreateDifferentObjects() {

            ToDo originalToDo = new ToDo();
            originalToDo.setId(200L);
            originalToDo.setText("Test task");

            DtoToDo dto = mapper.toDoToDto(originalToDo);
            ToDo convertedToDo = mapper.dtoToToDo(dto);

            assertThat(convertedToDo).isNotSameAs(originalToDo);
            assertThat(dto).isNotSameAs(originalToDo);
        }
    }
}
