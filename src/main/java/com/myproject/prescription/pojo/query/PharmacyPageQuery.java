package com.myproject.prescription.pojo.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class PharmacyPageQuery implements Serializable {
    private static final long serialVersionUID = 4610172911765522053L;

    /**
     * 药房名称
     */
    private String name;
}
