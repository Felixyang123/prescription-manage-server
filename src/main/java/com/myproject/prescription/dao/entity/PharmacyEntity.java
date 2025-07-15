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
@TableName(value = "pharmacy", autoResultMap = true)
public class PharmacyEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private Date createAt;

    private Date updateAt;
}
