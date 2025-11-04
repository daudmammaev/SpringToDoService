package com.emobile.springtodo.paginations;

import com.emobile.springtodo.dto.DtoToDo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationMeta meta;

    public static <T> PaginatedResponse<T> of(List<T> data, int limit, int offset, long total) {
        return new PaginatedResponse<>(data, new PaginationMeta(limit, offset, total));
    }
}