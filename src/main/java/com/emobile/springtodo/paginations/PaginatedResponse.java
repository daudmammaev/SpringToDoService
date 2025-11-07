package com.emobile.springtodo.paginations;

import com.emobile.springtodo.dto.DtoToDo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationMeta meta;

    public static <T> PaginatedResponse<T> of(List<T> data, int limit, int offset, long total) {
        return new PaginatedResponse<>(data, new PaginationMeta(limit, offset, total));
    }
}