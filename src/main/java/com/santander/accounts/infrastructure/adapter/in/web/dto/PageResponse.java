package com.santander.accounts.infrastructure.adapter.in.web.dto;

import com.santander.accounts.application.port.in.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, int offset, int limit, long total) {

    public static <D, R> PageResponse<R> from(PageResult<D> page, Function<D, R> mapper) {
        return new PageResponse<>(
                page.items().stream().map(mapper).toList(),
                page.offset(), page.limit(), page.total());
    }
}