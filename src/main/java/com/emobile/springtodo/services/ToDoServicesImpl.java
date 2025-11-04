package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.emobile.springtodo.mappers.ToDoMapRow;
import com.emobile.springtodo.mappers.ToDoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ToDoServicesImpl implements ToDoServices{

    private final Counter orderCounter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ToDoServicesImpl(MeterRegistry meterRegistry) {
        this.orderCounter = meterRegistry.counter("orders");
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
    public long deleteItem(long id) {
        return 0;
    }

    @Override
    public DtoToDo updateItem(DtoToDo dtoToDo) {
        return null;
    }

    @Override
    public DtoToDo getItem(long id) {
        return ToDoMapper.INSTANCE.toDoToDto(jdbcTemplate.queryForObject(
                "select * from to_do where id = ?", new ToDoMapRow(), id));
    }

    @Override
    public List<DtoToDo> allItem() {
        List<DtoToDo> dtoToDoList = new ArrayList<>();
        List<ToDo> ToDoList = jdbcTemplate.query(("select * from to_do"), new ToDoMapRow());
        ToDoList.forEach(e -> dtoToDoList.add(ToDoMapper.INSTANCE.toDoToDto(e)));
        return dtoToDoList;
    }
}
