package com.myproject.prescription.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.prescription.dao.entity.PrescriptionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionMapper extends BaseMapper<PrescriptionEntity> {
}
