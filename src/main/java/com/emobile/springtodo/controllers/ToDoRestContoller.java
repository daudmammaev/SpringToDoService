package com.emobile.springtodo.controllers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.services.ToDoServicesImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ToDoRestContoller {

    @Autowired
    public ToDoServicesImpl toDoServices;

    @RequestMapping("/getAll")
    public List<DtoToDo> getAllItem() {
        return toDoServices.allItem();
    }
    @RequestMapping("/getItem")
    public DtoToDo getItem(@RequestBody @Valid DtoToDo dtoToDo){
        return toDoServices.getItem(dtoToDo.getId());
    }
    @RequestMapping("/addItem")
    public DtoToDo addItem(@RequestBody @Valid DtoToDo dtoToDo){
        return toDoServices.addItem(dtoToDo);
    }

}
