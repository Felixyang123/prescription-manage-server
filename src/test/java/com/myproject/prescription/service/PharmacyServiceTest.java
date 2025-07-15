package com.myproject.prescription.service;

import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.entity.PharmacyEntity;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.dto.DrugDTO;
import com.myproject.prescription.pojo.query.PharmacyPageQuery;
import com.myproject.prescription.pojo.result.PharmacyPageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class PharmacyServiceTest {
    @Autowired
    private PharmacyService pharmacyService;
    @Autowired
    private PharmacyDrugService pharmacyDrugService;
    @Autowired
    private DrugService drugService;

    @Test
    @DisplayName("测试查询药房")
    void pageQueryPharmacyWithDrugs_test() {
        List<PharmacyEntity> pharmacyEntities = new ArrayList<>();
        Date date = new Date();
        PharmacyEntity pharmacy = PharmacyEntity.builder().name(UUID.randomUUID().toString()).address("药房大楼").createAt(date).updateAt(date).build();
        pharmacyEntities.add(pharmacy);
        pharmacyService.saveBatch(pharmacyEntities);

        List<DrugEntity> drugs = new ArrayList<>();
        DrugEntity drug1 = DrugEntity.builder().name("维生素A1").batchNumber("101").manufacturer("吉吉哈儿制药商").version(0)
                .stock(100).lockedStock(0).allocatedStock(0).currentStock(100)
                .expiryDate(DateUtils.addMonths(date, 1)).build();
        DrugEntity drug2 = DrugEntity.builder().name("维生素A2").batchNumber("101").manufacturer("吉吉哈儿制药商").version(0)
                .stock(100).lockedStock(0).allocatedStock(0).currentStock(100)
                .expiryDate(DateUtils.addMonths(date, 1)).build();
        drugs.add(drug1);
        drugs.add(drug2);
        drugService.saveBatch(drugs);

        PharmacyDrugEntity pharmacyDrug1 = PharmacyDrugEntity.builder().drugId(drug1.getId()).pharmacyId(pharmacy.getId()).version(0).stock(50)
                .lockedStock(0).allocatedStock(0).currentStock(50).build();

        PharmacyDrugEntity pharmacyDrug2 = PharmacyDrugEntity.builder().drugId(drug2.getId()).pharmacyId(pharmacy.getId()).version(0).stock(60)
                .lockedStock(0).allocatedStock(0).currentStock(50).build();
        List<PharmacyDrugEntity> pharmacyDrugEntities = new ArrayList<>();
        pharmacyDrugEntities.add(pharmacyDrug1);
        pharmacyDrugEntities.add(pharmacyDrug2);
        pharmacyDrugService.saveBatch(pharmacyDrugEntities);

        PageRequest<PharmacyPageQuery> pageRequest = new PageRequest<>();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        PharmacyPageQuery pharmacyPageQuery = new PharmacyPageQuery();
        pharmacyPageQuery.setName(pharmacy.getName());
        pageRequest.setReq(pharmacyPageQuery);
        PageResponse<PharmacyPageResult> response = pharmacyService.pageQueryPharmacyWithDrugs(pageRequest);
        Assertions.assertEquals(1, (int) response.getTotal());
        List<PharmacyPageResult> data = response.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(data) && 1 == data.size());
        PharmacyPageResult pharmacyPageResult = data.get(0);
        Assertions.assertTrue(pharmacy.getId().equals(pharmacyPageResult.getPharmacyId()) && pharmacy.getName().equals(pharmacyPageResult.getPharmacyName())
                && pharmacy.getAddress().equals(pharmacyPageResult.getPharmacyAddress()));

        List<DrugDTO> drugDTOS = pharmacyPageResult.getDrugs();
        Assertions.assertEquals(2, drugDTOS.size());
        Map<Long, DrugDTO> drugMap = drugDTOS.stream().collect(Collectors.toMap(DrugDTO::getId, Function.identity()));
        Assertions.assertTrue(drugMap.containsKey(drug1.getId()) && drugMap.containsKey(drug2.getId()));
    }
}
