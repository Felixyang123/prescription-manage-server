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
@TableName(value = "pharmacy_drug", autoResultMap = true)
public class PharmacyDrugEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pharmacyId;
    private Long drugId;
    private Integer stock;
    private Integer lockedStock;
    private Integer allocatedStock;
    private Integer currentStock;
    private Integer version;
    private Date createAt;
    private Date updateAt;
}
