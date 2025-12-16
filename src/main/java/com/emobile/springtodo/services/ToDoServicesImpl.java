package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import com.emobile.springtodo.repositories.RepoToDo;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.emobile.springtodo.mappers.ToDoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ToDoServicesImpl implements ToDoServices {

    private final Counter orderCounter;
    private final RepoToDo repoToDo;

    @Autowired
    public ToDoServicesImpl(MeterRegistry meterRegistry, RepoToDo repoToDo) {
        this.orderCounter = meterRegistry.counter("orders");
        this.repoToDo = repoToDo;
    }

    @Override
    public DtoToDo addItem(DtoToDo dtoToDo) {
        ToDo toDo = ToDoMapper.INSTANCE.dtoToToDo(dtoToDo);
        ToDo savedToDo = repoToDo.save(toDo);

        orderCounter.increment();

        return ToDoMapper.INSTANCE.toDoToDto(savedToDo);
    }

    @Override
    public List<DtoToDo> allItem() {
        List<ToDo> todos = repoToDo.findAll();

        return todos.stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<DtoToDo> searchItems(String searchText, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<ToDo> page = repoToDo.findByText(searchText, pageable);

        List<DtoToDo> dtoTodos = page.getContent().stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtoTodos, limit, offset, page.getTotalElements());
    }

    @Override
    public PaginatedResponse<DtoToDo> allItemWithPagination(int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<ToDo> page = repoToDo.findAll(pageable);

        List<DtoToDo> dtoTodos = page.getContent().stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtoTodos, limit, offset, page.getTotalElements());
    }

    @Override
    public long deleteItem(long id) {
        if (!repoToDo.existsById(id)) {
            throw new ToDoNotFoundException(id);
        }

        repoToDo.deleteById(id);
        return 1;
    }

    @Override
    public DtoToDo updateItem(DtoToDo dtoToDo) {
        ToDo existingToDo = repoToDo.findById(dtoToDo.getId())
                .orElseThrow(() -> new ToDoNotFoundException(dtoToDo.getId()));

        existingToDo.setText(dtoToDo.getText());
        ToDo updatedToDo = repoToDo.save(existingToDo);

        return ToDoMapper.INSTANCE.toDoToDto(updatedToDo);
    }

    @Override
    public DtoToDo getItem(long id) {
        ToDo toDo = repoToDo.findById(id)
                .orElseThrow(() -> new ToDoNotFoundException(id));

        return ToDoMapper.INSTANCE.toDoToDto(toDo);
    }

    @Override
    public void clearAllCaches() {

    }
}