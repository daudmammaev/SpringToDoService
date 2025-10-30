package com.emobile.springtodo.controllers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.services.ToDoServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToDoRestContoller {

    @Autowired
    public ToDoServicesImpl toDoServices;

    @RequestMapping("/getAll")
    public ResponseEntity<?> getAllItem() {
        return ResponseEntity.ok().body(toDoServices.allItem());
    }
    @RequestMapping("/getItem")
    public ResponseEntity<?> getItem(@RequestBody DtoToDo dtoToDo){
        return ResponseEntity.ok().body(toDoServices.getItem(dtoToDo.getId()));
    }
    @RequestMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestBody DtoToDo dtoToDo){
        return ResponseEntity.ok().body(toDoServices.addItem(dtoToDo));
    }

}
