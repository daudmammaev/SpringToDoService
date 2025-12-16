package com.emobile.springtodo.services;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.exceptions.ToDoNotFoundException;
import com.emobile.springtodo.mappers.ToDoMapRow;
import com.emobile.springtodo.models.ToDo;
import com.emobile.springtodo.paginations.PaginatedResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.emobile.springtodo.mappers.ToDoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ToDoServicesImpl implements ToDoServices {

    private final Counter orderCounter;

    @PersistenceContext
    private EntityManager entityManager;

    public ToDoServicesImpl(MeterRegistry meterRegistry, ToDoMapRow toDoMapRow) {
        this.orderCounter = meterRegistry.counter("orders");
    }

    @Override
    public DtoToDo addItem(DtoToDo dtoToDo) {
        ToDo toDo = ToDoMapper.INSTANCE.dtoToToDo(dtoToDo);
        entityManager.persist(toDo);

        orderCounter.increment();

        return ToDoMapper.INSTANCE.toDoToDto(toDo);
    }

    @Override
    public List<DtoToDo> allItem() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ToDo> cq = cb.createQuery(ToDo.class);
        Root<ToDo> root = cq.from(ToDo.class);
        cq.select(root);

        List<ToDo> todos = entityManager.createQuery(cq).getResultList();

        return todos.stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<DtoToDo> searchItems(String searchText, int limit, int offset) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ToDo> cq = cb.createQuery(ToDo.class);
        Root<ToDo> root = cq.from(ToDo.class);

        Predicate textPredicate = cb.equal(root.get("text"), searchText);
        cq.where(textPredicate);

        TypedQuery<ToDo> query = entityManager.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<ToDo> todos = query.getResultList();

        // Получаем общее количество
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ToDo> countRoot = countQuery.from(ToDo.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(countRoot.get("text"), searchText));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<DtoToDo> dtoTodos = todos.stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtoTodos, limit, offset, total);
    }

    @Override
    public PaginatedResponse<DtoToDo> allItemWithPagination(int limit, int offset) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ToDo> cq = cb.createQuery(ToDo.class);
        Root<ToDo> root = cq.from(ToDo.class);
        cq.select(root);
        cq.orderBy(cb.asc(root.get("id")));

        TypedQuery<ToDo> query = entityManager.createQuery(cq);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<ToDo> todos = query.getResultList();

        // Получаем общее количество
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ToDo> countRoot = countQuery.from(ToDo.class);
        countQuery.select(cb.count(countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<DtoToDo> dtoTodos = todos.stream()
                .map(ToDoMapper.INSTANCE::toDoToDto)
                .collect(Collectors.toList());

        return PaginatedResponse.of(dtoTodos, limit, offset, total);
    }

    @Override
    public long deleteItem(long id) {
        ToDo toDo = entityManager.find(ToDo.class, id);
        if (toDo == null) {
            throw new ToDoNotFoundException(id);
        }

        entityManager.remove(toDo);
        return 1;
    }

    @Override
    public DtoToDo updateItem(DtoToDo dtoToDo) {
        ToDo existingToDo = entityManager.find(ToDo.class, dtoToDo.getId());
        if (existingToDo == null) {
            throw new ToDoNotFoundException(dtoToDo.getId());
        }

        existingToDo.setText(dtoToDo.getText());
        entityManager.merge(existingToDo);

        return ToDoMapper.INSTANCE.toDoToDto(existingToDo);
    }

    @Override
    public DtoToDo getItem(long id) {
        ToDo toDo = entityManager.find(ToDo.class, id);
        if (toDo == null) {
            throw new ToDoNotFoundException(id);
        }

        return ToDoMapper.INSTANCE.toDoToDto(toDo);
    }

    @Override
    public void clearAllCaches() {
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }
}