package com.myproject.prescription.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest<T> implements Serializable {
    private static final long serialVersionUID = 2338286696041208069L;

    private Integer page;

    private Integer size;

    private T req;
}
