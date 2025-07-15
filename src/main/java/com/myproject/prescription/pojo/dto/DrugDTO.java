package com.myproject.prescription.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugDTO implements Serializable {
    private static final long serialVersionUID = -432416939952075610L;

    private Long id;

    /**
     * 药品名称
     */
    private String name;
    /**
     * 制造商
     */
    private String manufacturer;
    /**
     * 批次号
     */
    private String batchNumber;
    /**
     * 有效日期
     */
    private Date expiryDate;
    /**
     * 当前可用库存
     */
    private Integer stock;
}
