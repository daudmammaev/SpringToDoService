package com.emobile.springtodo.paginations;

import lombok.*;


@Getter
@Setter
@RequiredArgsConstructor
public class PaginationMeta {
    private int limit;
    private int offset;
    private long total;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginationMeta(int limit, int offset, long total) {
        this.limit = limit;
        this.offset = offset;
        this.total = total;
        this.hasNext = (offset + limit) < total;
        this.hasPrevious = offset > 0;
    }
}