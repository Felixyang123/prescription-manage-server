package com.myproject.prescription.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PageUtils {

    public static <R, T> PageResponse<T> emptyPage(PageRequest<R> pageRequest) {
        return PageResponse.<T>builder().page(pageRequest.getPage()).size(pageRequest.getSize())
                .data(Collections.emptyList()).pages(0).build();
    }

    public static <R, T> PageResponse<T> build(Page<R> page, Function<R, T> function) {
        PageResponse<T> pageResponse = new PageResponse<>();
        pageResponse.setPage((int) page.getCurrent());
        pageResponse.setSize((int) page.getSize());
        pageResponse.setPages((int) page.getPages());
        pageResponse.setTotal((int) page.getTotal());
        if (page.getRecords() != null) {
            List<T> records = page.getRecords().stream().map(function).collect(Collectors.toList());
            pageResponse.setData(records);
        }
        return pageResponse;
    }
}
