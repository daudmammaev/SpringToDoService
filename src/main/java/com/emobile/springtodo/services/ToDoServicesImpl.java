package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import mappers.ToDoMapper;
import org.apache.el.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static mappers.ToDoMapper.DtodotoByTodo;
import static mappers.ToDoMapper.ToDoByDtodoto;
@Service
public class ToDoServicesImpl implements ToDoServices{

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Override
    public DtoToDo addItem(DtoToDo dtoToDo) {
        ToDo toDo = DtodotoByTodo(dtoToDo);
        jdbcTemplate.update("insert into to_do (id, text) values(?,?)",
                toDo.getId(),
                toDo.getText());
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
        return ToDoByDtodoto(jdbcTemplate.queryForObject("select * from to_do where id = ?", new ToDoMapper(), id));
    }

    @Override
    public List<DtoToDo> allItem() {
        List<DtoToDo> dtoToDoList = new ArrayList<>();
        List<ToDo> ToDoList = jdbcTemplate.query(("select * from to_do"), new ToDoMapper());
        ToDoList.forEach(e -> dtoToDoList.add(ToDoByDtodoto(e)));
        return dtoToDoList;
    }
}
