package com.myproject.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.pojo.command.DrugAddCmd;

public interface DrugService extends IService<DrugEntity> {
    void addDrug(DrugAddCmd cmd);
    boolean deductStockWithOptimisticLock();
}

