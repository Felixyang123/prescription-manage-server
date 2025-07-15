package com.myproject.prescription.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myproject.prescription.dao.entity.DrugEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DrugMapper extends BaseMapper<DrugEntity> {

    @Update("update drug set locked_stock=locked_stock+#{delta}, current_stock=current_stock-#{delta} where id=#{drugId}")
    void lockStock(Long drugId, Integer delta);

    @Update("update drug set stock=stock-#{delta}, locked_stock=locked_stock-#{delta}, allocated_stock=allocated_stock+#{delta} where id=#{drugId}")
    void reduceStock(Long drugId, Integer delta);
}
