package com.myproject.prescription.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.prescription.dao.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}
