package com.skillpath.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Transport-friendly wrapper around a Spring Data {@link Page}.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <E, T> PageResponse<T> from(Page<E> source, Function<E, T> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getNumber(),
                source.getSize(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast()
        );
    }
}
