package com.emobile.springtodo.repositories;

import com.emobile.springtodo.models.ToDo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepoToDo extends JpaRepository<ToDo, Long> {

    Page<ToDo> findAll(Pageable pageable);

    @Query("SELECT t FROM ToDo t WHERE t.text = :searchText")
    Page<ToDo> findByText(@Param("searchText") String searchText, Pageable pageable);

    @Query("SELECT COUNT(t) FROM ToDo t WHERE t.text = :searchText")
    long countByText(@Param("searchText") String searchText);
}