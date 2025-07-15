package com.myproject.prescription.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.mapper.DrugMapper;
import com.myproject.prescription.pojo.command.DrugAddCmd;
import com.myproject.prescription.service.DrugService;
import org.springframework.stereotype.Service;

@Service
public class DrugServiceImpl extends ServiceImpl<DrugMapper, DrugEntity> implements DrugService {
    @Override
    public void addDrug(DrugAddCmd cmd) {
        DrugEntity drugAddEntity = DrugEntity.builder().name(cmd.getName()).batchNumber(cmd.getBatchNumber()).manufacturer(cmd.getManufacturer())
                .expiryDate(cmd.getExpiryDate()).stock(cmd.getStock()).lockedStock(0).allocatedStock(0)
                .currentStock(cmd.getStock()).build();
        save(drugAddEntity);
    }

    @Override
    public boolean deductStockWithOptimisticLock() {
        return false;
    }
}
