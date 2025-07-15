package com.myproject.prescription.pojo.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuditLogPageQuery implements Serializable {
    private static final long serialVersionUID = -8589842959126468429L;

    /**
     * 病人ID
     */
    private String patientId;

    /**
     * 药房ID
     */
    private Long pharmacyId;

    /**
     * 处方操作结果状态
     * 0-失败，1-成功
     */
    private Integer status;
}
