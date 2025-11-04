package com.emobile.springtodo.controllers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import com.emobile.springtodo.services.ToDoServicesImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ToDoRestContoller {

    @Autowired
    public ToDoServicesImpl toDoServices;

    @Operation(
            summary = "Получить все задачи",
            description = "Возвращает список всех задач без пагинации"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение списка задач",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DtoToDo.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @RequestMapping("/getAll")
    public List<DtoToDo> getAllItem() {
        return toDoServices.allItem();
    }

    @Operation(
            summary = "Получить задачи с пагинацией",
            description = "Возвращает список задач с использованием limit-offset пагинации"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение пагинированного списка",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные параметры пагинации"
            )
    })
    @RequestMapping("/getAllWithPaginatuion/{limit}/{offset}")
    public PaginatedResponse<DtoToDo> getAllItem(@PathVariable int limit,
                                                 @PathVariable int offset) {
        return toDoServices.allItemWithPagination(limit,offset);
    }

    @Operation(
            summary = "Получить задачу по ID",
            description = "Возвращает задачу по её идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Задача найдена",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DtoToDo.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидный ID задачи"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Задача не найдена"
            )
    })
    @RequestMapping("/getItem")
    public DtoToDo getItem(@RequestBody @Valid DtoToDo dtoToDo){
        return toDoServices.getItem(dtoToDo.getId());
    }

    @Operation(
            summary = "Создать новую задачу",
            description = "Создаёт новую задачу и возвращает её с присвоенным ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Задача успешно создана",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DtoToDo.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные задачи"
            )
    })
    @RequestMapping("/addItem")
    public DtoToDo addItem(@RequestBody @Valid DtoToDo dtoToDo){
        return toDoServices.addItem(dtoToDo);
    }

}
