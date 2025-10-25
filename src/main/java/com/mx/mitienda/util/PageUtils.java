package com.mx.mitienda.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {

    public static Pageable sanitize(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 20, Sort.by("id").descending());
        }

        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize() > 0 && pageable.getPageSize() <= 100
                ? pageable.getPageSize()
                : 20;

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by("id").descending();

        return PageRequest.of(page, size, sort);
    }
}
