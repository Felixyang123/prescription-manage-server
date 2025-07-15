package com.myproject.prescription.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "prescription_item", autoResultMap = true)
public class PrescriptionItemEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long prescriptionId;
    private Long drugId;
    private String dosage;
    private Integer quantity;
}
