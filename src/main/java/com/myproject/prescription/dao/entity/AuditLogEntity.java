package com.myproject.prescription.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "audit_log", autoResultMap = true)
public class AuditLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private String patientId;
    private Long pharmacyId;
    /**
     * 0-失败 1-成功
     * @see com.myproject.prescription.enums.YesOrNoEnum
     */
    private Integer status;
    /**
     * 请求的药品
     */
    private String requestedDrugs;
    /**
     * 配发的药品
     */
    private String dispensedDrugs;
    private String failureReason;
    private Date operationTime;
}
