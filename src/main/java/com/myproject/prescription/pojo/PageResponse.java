package com.myproject.prescription.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 5122384427426123999L;

    private Integer total;

    private Integer page;

    private Integer pages;

    private Integer size;

    private List<T> data;
}
