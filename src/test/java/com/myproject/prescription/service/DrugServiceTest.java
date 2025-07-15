package com.myproject.prescription.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.pojo.command.DrugAddCmd;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class DrugServiceTest {
    @Autowired
    private DrugService drugService;

    @Test
    @DisplayName("测试添加药品")
    @Transactional
    void addDrug_test() {
        DrugAddCmd cmd = new DrugAddCmd();
        cmd.setName(UUID.randomUUID().toString());
        cmd.setStock(100);
        cmd.setManufacturer("吉吉哈儿制药商");
        cmd.setBatchNumber("102");
        Date date = new Date();
        cmd.setExpiryDate(DateUtils.addMonths(date, 1));
        drugService.addDrug(cmd);

        DrugEntity drug = drugService.getOne(Wrappers.<DrugEntity>lambdaQuery().eq(DrugEntity::getName, cmd.getName()));
        assertNotNull(drug);
        assertTrue(cmd.getStock().equals(drug.getStock()) && cmd.getBatchNumber().equals(drug.getBatchNumber())
                && cmd.getManufacturer().equals(drug.getManufacturer()));
        assertTrue(drug.getStock() == 100 && drug.getCurrentStock() == 100 && drug.getLockedStock() == 0 && drug.getAllocatedStock() == 0);
    }

}
