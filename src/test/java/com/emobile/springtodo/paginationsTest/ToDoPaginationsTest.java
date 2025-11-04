package com.emobile.springtodo.paginationsTest;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import com.emobile.springtodo.services.ToDoServicesImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ToDoPaginationsTest {

    @Autowired
    private ToDoServicesImpl toDoService;

    @Sql("/Test_schema_for_pagination.sql")
    @Test
    @DisplayName("Сервис должен возвращать пагинированные данные)")
    void serviceShouldReturnPaginatedDataPostgres() {

        PaginatedResponse<DtoToDo> response = toDoService.allItemWithPagination(2, 1);

        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getMeta().getTotal()).isEqualTo(5);
        assertThat(response.getMeta().isHasNext()).isTrue();
        assertThat(response.getMeta().isHasPrevious()).isTrue();
    }

}