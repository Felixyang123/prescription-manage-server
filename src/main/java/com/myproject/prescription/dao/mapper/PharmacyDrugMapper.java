package com.myproject.prescription.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PharmacyDrugMapper extends BaseMapper<PharmacyDrugEntity> {

    @Update("update pharmacy_drug set locked_stock=locked_stock+#{delta}, current_stock=current_stock-#{delta} where id=#{id}")
    void lockStock(Long id, Integer delta);

    @Update("update pharmacy_drug set stock=stock-#{delta}, locked_stock=locked_stock-#{delta}, allocated_stock=allocated_stock+#{delta} where id=#{id}")
    void reduceStock(Long id, Integer delta);
}
