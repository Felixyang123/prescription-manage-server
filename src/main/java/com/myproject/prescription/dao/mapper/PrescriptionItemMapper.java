package com.myproject.prescription.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.prescription.dao.entity.PrescriptionItemEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItemEntity> {
}
