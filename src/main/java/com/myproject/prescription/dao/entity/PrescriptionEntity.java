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
@TableName(value = "prescription", autoResultMap = true)
public class PrescriptionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String patientId;
    private Long pharmacyId;
    /**
     * 0-created, 1-success, 2-fail
     * @see com.myproject.prescription.enums.PrescriptionStatusEnum
     */
    private Integer status;
    private Date createAt;
    private Date updateAt;
}
