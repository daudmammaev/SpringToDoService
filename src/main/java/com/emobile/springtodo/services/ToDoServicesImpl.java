package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.emobile.springtodo.mappers.ToDoMapRow;
import com.emobile.springtodo.mappers.ToDoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ToDoServicesImpl implements ToDoServices{

    private final Counter orderCounter;

    @Autowired
    private final ToDoMapRow toDoMapRow;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ToDoServicesImpl(MeterRegistry meterRegistry, ToDoMapRow toDoMapRow) {
        this.orderCounter = meterRegistry.counter("orders");
        this.toDoMapRow = toDoMapRow;
    }

    @Override
    public DtoToDo addItem(DtoToDo dtoToDo) {
        ToDo toDo = ToDoMapper.INSTANCE.dtoToToDo(dtoToDo);
        jdbcTemplate.update("insert into to_do (id, text) values(?,?)",
                toDo.getId(),
                toDo.getText());

        orderCounter.increment();

        return dtoToDo;
    }

    @Override
    public List<DtoToDo> allItem() {
        List<DtoToDo> dtoToDoList = new ArrayList<>();
        List<ToDo> ToDoList = jdbcTemplate.query(("select * from to_do"), new ToDoMapRow());
        ToDoList.forEach(e -> dtoToDoList.add(ToDoMapper.INSTANCE.toDoToDto(e)));
        return dtoToDoList;
    }

    @Override
    public PaginatedResponse<DtoToDo> searchItems(String searchText, int limit, int offset) {

        List<DtoToDo> dtoTodos = jdbcTemplate.query(("select * from to_do where text = ?"), new ToDoMapRow(), searchText)
                .stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());
        return PaginatedResponse.of(dtoTodos, limit, offset, dtoTodos.size());
    }

    @Override
    public PaginatedResponse<DtoToDo> allItemWithPagination(int limit, int offset) {
        String sql = """
        SELECT id, text
        FROM to_do 
        ORDER BY id  
        LIMIT ? OFFSET ?
        """;
        List<ToDo> todos = jdbcTemplate.query(sql, toDoMapRow, limit, offset);
        long total = allItem().size();

        List<DtoToDo> dtoTodos = todos.stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtoTodos, limit, offset, total);
    }

    @Override
    public long deleteItem(long id) {
        int deletedRows = jdbcTemplate.update("delete from to_do where id = ?", id);

        if (deletedRows == 0) {
            throw new ToDoNotFoundException(id);
        }

        return deletedRows;
    }

    @Override
    public DtoToDo updateItem(DtoToDo dtoToDo) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE to_do SET text = ? WHERE id = ?",
                dtoToDo.getText(),
                dtoToDo.getId()
        );

        if (updatedRows == 0) {
            throw new ToDoNotFoundException(dtoToDo.getId());
        }

        return dtoToDo;
    }

    @Override
    public DtoToDo getItem(long id) {
        try {
            ToDo toDo = jdbcTemplate.queryForObject(
                    "select * from to_do where id = ?",
                    new ToDoMapRow(),
                    id
            );
            return ToDoMapper.INSTANCE.toDoToDto(toDo);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            throw new ToDoNotFoundException(id);
        }
    }
    @Override
    public void clearAllCaches() {
        // Аннотация @CacheEvict выполнит очистку через AOP
    }
}
