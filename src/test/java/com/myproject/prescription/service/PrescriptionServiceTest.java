package com.myproject.prescription.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.myproject.prescription.dao.entity.*;
import com.myproject.prescription.enums.PrescriptionOperateStatusEnum;
import com.myproject.prescription.enums.PrescriptionStatusEnum;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.pojo.query.AuditLogPageQuery;
import com.myproject.prescription.pojo.result.AuditLogPageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.myproject.prescription.enums.BizExceptionEnum.PRESCRIPTION_FULFILL_STATUS_ERROR;

@SpringBootTest
public class PrescriptionServiceTest {

    @Autowired
    private PrescriptionService prescriptionService;
    @Autowired
    private PharmacyService pharmacyService;
    @Autowired
    private PharmacyDrugService pharmacyDrugService;
    @Autowired
    private DrugService drugService;
    @Autowired
    private AuditLogService auditLogService;

    @Test
    @DisplayName("测试创建处方单")
    @Transactional
    void createPrescription_test() {
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
                .lockedStock(0).allocatedStock(0).currentStock(60).build();
        List<PharmacyDrugEntity> pharmacyDrugEntities = new ArrayList<>();
        pharmacyDrugEntities.add(pharmacyDrug1);
        pharmacyDrugEntities.add(pharmacyDrug2);
        pharmacyDrugService.saveBatch(pharmacyDrugEntities);

        PrescriptionCreateCmd cmd = new PrescriptionCreateCmd();
        cmd.setPatientId(UUID.randomUUID().toString());
        cmd.setPharmacyId(pharmacy.getId());
        List<PrescriptionItemDTO> itemDTOS = new ArrayList<>();
        PrescriptionItemDTO dto1 = PrescriptionItemDTO.builder().drugId(drug1.getId()).drugName(drug1.getName()).dosage("每日三次，一次10mg").quantity(10).build();
        PrescriptionItemDTO dto2 = PrescriptionItemDTO.builder().drugId(drug2.getId()).drugName(drug2.getName()).dosage("每日三次，一次10mg").quantity(20).build();
        itemDTOS.add(dto1);
        itemDTOS.add(dto2);
        cmd.setDrugs(itemDTOS);
        prescriptionService.createPrescription(cmd);

        PrescriptionEntity prescription = prescriptionService.getOne(Wrappers.<PrescriptionEntity>lambdaQuery().eq(PrescriptionEntity::getPatientId, cmd.getPatientId()));
        Assertions.assertNotNull(prescription);
        Assertions.assertTrue(prescription.getPharmacyId().equals(pharmacy.getId()) && prescription.getStatus().equals(PrescriptionStatusEnum.CREATED.getCode()));

        List<Long> drugIds = drugs.stream().map(DrugEntity::getId).collect(Collectors.toList());
        List<DrugEntity> drugEntities = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
        Map<Long, DrugEntity> drugMap = drugEntities.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
        DrugEntity drugEntity1 = drugMap.get(drug1.getId());
        Assertions.assertNotNull(drugEntity1);
        Assertions.assertTrue(drugEntity1.getStock() == 100 && drugEntity1.getLockedStock() == 10 && drugEntity1.getCurrentStock() == 90 && drugEntity1.getAllocatedStock() == 0);
        DrugEntity drugEntity2 = drugMap.get(drug2.getId());
        Assertions.assertNotNull(drugEntity2);
        Assertions.assertTrue(drugEntity2.getStock() == 100 && drugEntity2.getLockedStock() == 20 && drugEntity2.getCurrentStock() == 80 && drugEntity2.getAllocatedStock() == 0);

        List<PharmacyDrugEntity> pharmacyDrugEntityList = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacy.getId())
                .in(PharmacyDrugEntity::getDrugId, drugIds));
        Map<Long, PharmacyDrugEntity> pharmacyDrugEntityMap = pharmacyDrugEntityList.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        PharmacyDrugEntity pharmacyDrugEntity1 = pharmacyDrugEntityMap.get(drug1.getId());
        Assertions.assertNotNull(pharmacyDrugEntity1);
        Assertions.assertTrue(pharmacyDrugEntity1.getStock() == 50 && pharmacyDrugEntity1.getLockedStock() == 10
                && pharmacyDrugEntity1.getCurrentStock() == 40 && pharmacyDrugEntity1.getAllocatedStock() == 0);
        PharmacyDrugEntity pharmacyDrugEntity2 = pharmacyDrugEntityMap.get(drug2.getId());
        Assertions.assertNotNull(pharmacyDrugEntity2);
        Assertions.assertTrue(pharmacyDrugEntity2.getStock() == 60 && pharmacyDrugEntity2.getLockedStock() == 20
                && pharmacyDrugEntity2.getCurrentStock() == 40 && pharmacyDrugEntity2.getAllocatedStock() == 0);

        // 测试履约
        prescriptionService.fulfillPrescription(prescription.getId());

        drugEntities = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
        drugMap = drugEntities.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
        drugEntity1 = drugMap.get(drug1.getId());
        Assertions.assertNotNull(drugEntity1);
        Assertions.assertTrue(drugEntity1.getStock() == 90 && drugEntity1.getLockedStock() == 0 && drugEntity1.getAllocatedStock() == 10 && drugEntity1.getCurrentStock() == 90);
        drugEntity2 = drugMap.get(drug2.getId());
        Assertions.assertNotNull(drugEntity2);
        Assertions.assertTrue(drugEntity2.getStock() == 80 && drugEntity2.getLockedStock() == 0 && drugEntity2.getCurrentStock() == 80 && drugEntity2.getAllocatedStock() == 20);

        pharmacyDrugEntityList = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacy.getId())
                .in(PharmacyDrugEntity::getDrugId, drugIds));
        pharmacyDrugEntityMap = pharmacyDrugEntityList.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        pharmacyDrugEntity1 = pharmacyDrugEntityMap.get(drug1.getId());
        Assertions.assertNotNull(pharmacyDrugEntity1);
        Assertions.assertTrue(pharmacyDrugEntity1.getStock() == 40 && pharmacyDrugEntity1.getLockedStock() == 0
                && pharmacyDrugEntity1.getCurrentStock() == 40 && pharmacyDrugEntity1.getAllocatedStock() == 10);
        pharmacyDrugEntity2 = pharmacyDrugEntityMap.get(drug2.getId());
        Assertions.assertNotNull(pharmacyDrugEntity2);
        Assertions.assertTrue(pharmacyDrugEntity2.getStock() == 40 && pharmacyDrugEntity2.getLockedStock() == 0
                && pharmacyDrugEntity2.getCurrentStock() == 40 && pharmacyDrugEntity2.getAllocatedStock() == 20);
    }

    @Test
    @DisplayName("测试库存不存创建处方单失败")
    void createPrescription_fail() {
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
                .lockedStock(0).allocatedStock(0).currentStock(60).build();
        List<PharmacyDrugEntity> pharmacyDrugEntities = new ArrayList<>();
        pharmacyDrugEntities.add(pharmacyDrug1);
        pharmacyDrugEntities.add(pharmacyDrug2);
        pharmacyDrugService.saveBatch(pharmacyDrugEntities);

        // 测试药房药品库存不足
        PrescriptionCreateCmd cmd = new PrescriptionCreateCmd();
        cmd.setPatientId(UUID.randomUUID().toString());
        cmd.setPharmacyId(pharmacy.getId());
        List<PrescriptionItemDTO> itemDTOS = new ArrayList<>();
        PrescriptionItemDTO dto1 = PrescriptionItemDTO.builder().drugId(drug1.getId()).drugName(drug1.getName()).dosage("每日三次，一次10mg").quantity(100).build();
        PrescriptionItemDTO dto2 = PrescriptionItemDTO.builder().drugId(drug2.getId()).drugName(drug2.getName()).dosage("每日三次，一次10mg").quantity(20).build();
        itemDTOS.add(dto1);
        itemDTOS.add(dto2);
        cmd.setDrugs(itemDTOS);
        prescriptionService.createPrescription(cmd);

        PrescriptionEntity prescription = prescriptionService.getOne(Wrappers.<PrescriptionEntity>lambdaQuery().eq(PrescriptionEntity::getPatientId, cmd.getPatientId()));
        Assertions.assertNotNull(prescription);
        Assertions.assertEquals(PrescriptionStatusEnum.FAIL.getCode(), prescription.getStatus());
        PageRequest<AuditLogPageQuery> pageRequest = new PageRequest<>();
        pageRequest.setPage(1);
        pageRequest.setSize(10);
        AuditLogPageQuery query = new AuditLogPageQuery();
        query.setPatientId(prescription.getPatientId());
        pageRequest.setReq(query);
        PageResponse<AuditLogPageResult> pagedQueryAuditLogs = auditLogService.pageQueryAuditLogs(pageRequest);
        Assertions.assertEquals(1, pagedQueryAuditLogs.getTotal());
        AuditLogPageResult log = pagedQueryAuditLogs.getData().get(0);
        Assertions.assertNotNull(log);
        Assertions.assertEquals(PrescriptionOperateStatusEnum.FAIL.getCode(), log.getStatus());
        List<PrescriptionDrugValidationResultDTO> resultDTOS = log.getFailureReasons();
        Optional<List<String>> failure = resultDTOS.stream().map(PrescriptionDrugValidationResultDTO::getFailures).filter(CollectionUtils::isNotEmpty).findFirst();
        Assertions.assertTrue(failure.isPresent() && failure.get().contains("药房分配库存不足"));

        // 测试药品库存不足
        dto1.setQuantity(200);
        prescriptionService.createPrescription(cmd);
        pagedQueryAuditLogs = auditLogService.pageQueryAuditLogs(pageRequest);
        log = pagedQueryAuditLogs.getData().get(0);
        Assertions.assertNotNull(log);
        Assertions.assertEquals(PrescriptionOperateStatusEnum.FAIL.getCode(), log.getStatus());
        resultDTOS = log.getFailureReasons();
        failure = resultDTOS.stream().map(PrescriptionDrugValidationResultDTO::getFailures).filter(CollectionUtils::isNotEmpty).findFirst();
        Assertions.assertTrue(failure.isPresent() && failure.get().contains("药品库存不足"));

        // 测试药品过期
        drugService.updateById(DrugEntity.builder().id(drug1.getId()).expiryDate(DateUtils.addMonths(date, -1)).build());
        dto1.setQuantity(50);
        prescriptionService.createPrescription(cmd);
        pagedQueryAuditLogs = auditLogService.pageQueryAuditLogs(pageRequest);
        log = pagedQueryAuditLogs.getData().get(0);
        Assertions.assertNotNull(log);
        Assertions.assertEquals(PrescriptionOperateStatusEnum.FAIL.getCode(), log.getStatus());
        resultDTOS = log.getFailureReasons();
        failure = resultDTOS.stream().map(PrescriptionDrugValidationResultDTO::getFailures).filter(CollectionUtils::isNotEmpty).findFirst();
        Assertions.assertTrue(failure.isPresent() && failure.get().contains("药品已过期"));
    }

    @Test
    @DisplayName("测试并发创建处方单不会超卖")
    void createPrescription_concurrent_test() {
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
                .lockedStock(0).allocatedStock(0).currentStock(60).build();
        List<PharmacyDrugEntity> pharmacyDrugEntities = new ArrayList<>();
        pharmacyDrugEntities.add(pharmacyDrug1);
        pharmacyDrugEntities.add(pharmacyDrug2);
        pharmacyDrugService.saveBatch(pharmacyDrugEntities);

        String patientId = UUID.randomUUID().toString();
        ExecutorService pool = null;
        try {
            pool = Executors.newWorkStealingPool(15);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                PrescriptionCreateCmd cmd = new PrescriptionCreateCmd();
                cmd.setPatientId(patientId);
                cmd.setPharmacyId(pharmacy.getId());
                List<PrescriptionItemDTO> itemDTOS = new ArrayList<>();
                PrescriptionItemDTO dto1 = PrescriptionItemDTO.builder().drugId(drug1.getId()).drugName(drug1.getName()).dosage("每日三次，一次10mg").quantity(10).build();
                PrescriptionItemDTO dto2 = PrescriptionItemDTO.builder().drugId(drug2.getId()).drugName(drug2.getName()).dosage("每日三次，一次10mg").quantity(20).build();
                itemDTOS.add(dto1);
                itemDTOS.add(dto2);
                cmd.setDrugs(itemDTOS);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> prescriptionService.createPrescription(cmd), pool);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            if (pool != null) {
                pool.shutdown();
            }
        }

        List<PrescriptionEntity> prescriptions = prescriptionService.list(Wrappers.<PrescriptionEntity>lambdaQuery().eq(PrescriptionEntity::getPatientId, patientId));
        List<PrescriptionEntity> createdPrescriptions = prescriptions.stream().filter(prescriptionEntity -> PrescriptionStatusEnum.CREATED.getCode().equals(prescriptionEntity.getStatus())).collect(Collectors.toList());
        Assertions.assertEquals(15, prescriptions.size());
        Assertions.assertEquals(3, createdPrescriptions.size());

        List<Long> drugIds = drugs.stream().map(DrugEntity::getId).collect(Collectors.toList());
        List<DrugEntity> drugEntities = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
        Map<Long, DrugEntity> drugMap = drugEntities.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
        DrugEntity drugEntity1 = drugMap.get(drug1.getId());
        Assertions.assertNotNull(drugEntity1);
        Assertions.assertTrue(drugEntity1.getStock() == 100 && drugEntity1.getLockedStock() == 30 && drugEntity1.getCurrentStock() == 70 && drugEntity1.getAllocatedStock() == 0);
        DrugEntity drugEntity2 = drugMap.get(drug2.getId());
        Assertions.assertNotNull(drugEntity2);
        Assertions.assertTrue(drugEntity2.getStock() == 100 && drugEntity2.getLockedStock() == 60 && drugEntity2.getCurrentStock() == 40 && drugEntity2.getAllocatedStock() == 0);

        List<PharmacyDrugEntity> pharmacyDrugEntityList = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacy.getId())
                .in(PharmacyDrugEntity::getDrugId, drugIds));
        Map<Long, PharmacyDrugEntity> pharmacyDrugEntityMap = pharmacyDrugEntityList.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        PharmacyDrugEntity pharmacyDrugEntity1 = pharmacyDrugEntityMap.get(drug1.getId());
        Assertions.assertNotNull(pharmacyDrugEntity1);
        Assertions.assertTrue(pharmacyDrugEntity1.getStock() == 50 && pharmacyDrugEntity1.getLockedStock() == 30
                && pharmacyDrugEntity1.getCurrentStock() == 20 && pharmacyDrugEntity1.getAllocatedStock() == 0);
        PharmacyDrugEntity pharmacyDrugEntity2 = pharmacyDrugEntityMap.get(drug2.getId());
        Assertions.assertNotNull(pharmacyDrugEntity2);
        Assertions.assertTrue(pharmacyDrugEntity2.getStock() == 60 && pharmacyDrugEntity2.getLockedStock() == 60
                && pharmacyDrugEntity2.getCurrentStock() == 0 && pharmacyDrugEntity2.getAllocatedStock() == 0);

        // 测试履约并发安全，库存不会超卖
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (PrescriptionEntity prescription : createdPrescriptions) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> prescriptionService.fulfillPrescription(prescription.getId()));
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();

        List<PrescriptionEntity> fulfilledPrescriptions = prescriptionService.list(Wrappers.<PrescriptionEntity>lambdaQuery().eq(PrescriptionEntity::getPatientId, patientId).eq(PrescriptionEntity::getStatus, PrescriptionStatusEnum.SUCCESS.getCode()));
        Assertions.assertEquals(3, fulfilledPrescriptions.size());

        drugEntities = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
        drugMap = drugEntities.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
        drugEntity1 = drugMap.get(drug1.getId());
        Assertions.assertNotNull(drugEntity1);
        Assertions.assertTrue(drugEntity1.getStock() == 70 && drugEntity1.getLockedStock() == 0 && drugEntity1.getCurrentStock() == 70 && drugEntity1.getAllocatedStock() == 30);
        drugEntity2 = drugMap.get(drug2.getId());
        Assertions.assertNotNull(drugEntity2);
        Assertions.assertTrue(drugEntity2.getStock() == 40 && drugEntity2.getLockedStock() == 0 && drugEntity2.getCurrentStock() == 40 && drugEntity2.getAllocatedStock() == 60);

        pharmacyDrugEntityList = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacy.getId())
                .in(PharmacyDrugEntity::getDrugId, drugIds));
        pharmacyDrugEntityMap = pharmacyDrugEntityList.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        pharmacyDrugEntity1 = pharmacyDrugEntityMap.get(drug1.getId());
        Assertions.assertNotNull(pharmacyDrugEntity1);
        Assertions.assertTrue(pharmacyDrugEntity1.getStock() == 20 && pharmacyDrugEntity1.getLockedStock() == 0
                && pharmacyDrugEntity1.getCurrentStock() == 20 && pharmacyDrugEntity1.getAllocatedStock() == 30);
        pharmacyDrugEntity2 = pharmacyDrugEntityMap.get(drug2.getId());
        Assertions.assertNotNull(pharmacyDrugEntity2);
        Assertions.assertTrue(pharmacyDrugEntity2.getStock() == 0 && pharmacyDrugEntity2.getLockedStock() == 0
                && pharmacyDrugEntity2.getCurrentStock() == 0 && pharmacyDrugEntity2.getAllocatedStock() == 60);
    }


    @Test
    @DisplayName("测试处方单履约幂等")
    void fulfill_idempotent_test() {
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
                .lockedStock(0).allocatedStock(0).currentStock(60).build();
        List<PharmacyDrugEntity> pharmacyDrugEntities = new ArrayList<>();
        pharmacyDrugEntities.add(pharmacyDrug1);
        pharmacyDrugEntities.add(pharmacyDrug2);
        pharmacyDrugService.saveBatch(pharmacyDrugEntities);

        PrescriptionCreateCmd cmd = new PrescriptionCreateCmd();
        cmd.setPatientId(UUID.randomUUID().toString());
        cmd.setPharmacyId(pharmacy.getId());
        List<PrescriptionItemDTO> itemDTOS = new ArrayList<>();
        PrescriptionItemDTO dto1 = PrescriptionItemDTO.builder().drugId(drug1.getId()).drugName(drug1.getName()).dosage("每日三次，一次10mg").quantity(10).build();
        PrescriptionItemDTO dto2 = PrescriptionItemDTO.builder().drugId(drug2.getId()).drugName(drug2.getName()).dosage("每日三次，一次10mg").quantity(20).build();
        itemDTOS.add(dto1);
        itemDTOS.add(dto2);
        cmd.setDrugs(itemDTOS);
        prescriptionService.createPrescription(cmd);

        PrescriptionEntity prescription = prescriptionService.getOne(Wrappers.<PrescriptionEntity>lambdaQuery().eq(PrescriptionEntity::getPatientId, cmd.getPatientId()));
        List<Long> drugIds = drugs.stream().map(DrugEntity::getId).collect(Collectors.toList());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    prescriptionService.fulfillPrescription(prescription.getId());

                    List<DrugEntity> drugEntities = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
                    Map<Long, DrugEntity> drugMap = drugEntities.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
                    DrugEntity drugEntity1 = drugMap.get(drug1.getId());
                    Assertions.assertNotNull(drugEntity1);
                    Assertions.assertTrue(drugEntity1.getStock() == 90 && drugEntity1.getLockedStock() == 0 && drugEntity1.getAllocatedStock() == 10 && drugEntity1.getCurrentStock() == 90);
                    DrugEntity drugEntity2 = drugMap.get(drug2.getId());
                    Assertions.assertNotNull(drugEntity2);
                    Assertions.assertTrue(drugEntity2.getStock() == 80 && drugEntity2.getLockedStock() == 0 && drugEntity2.getCurrentStock() == 80 && drugEntity2.getAllocatedStock() == 20);

                    List<PharmacyDrugEntity> pharmacyDrugEntityList = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacy.getId())
                            .in(PharmacyDrugEntity::getDrugId, drugIds));
                    Map<Long, PharmacyDrugEntity> pharmacyDrugEntityMap = pharmacyDrugEntityList.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
                    PharmacyDrugEntity pharmacyDrugEntity1 = pharmacyDrugEntityMap.get(drug1.getId());
                    Assertions.assertNotNull(pharmacyDrugEntity1);
                    Assertions.assertTrue(pharmacyDrugEntity1.getStock() == 40 && pharmacyDrugEntity1.getLockedStock() == 0
                            && pharmacyDrugEntity1.getCurrentStock() == 40 && pharmacyDrugEntity1.getAllocatedStock() == 10);
                    PharmacyDrugEntity pharmacyDrugEntity2 = pharmacyDrugEntityMap.get(drug2.getId());
                    Assertions.assertNotNull(pharmacyDrugEntity2);
                    Assertions.assertTrue(pharmacyDrugEntity2.getStock() == 40 && pharmacyDrugEntity2.getLockedStock() == 0
                            && pharmacyDrugEntity2.getCurrentStock() == 40 && pharmacyDrugEntity2.getAllocatedStock() == 20);
                } catch (Exception e) {
                    Assertions.assertEquals(PRESCRIPTION_FULFILL_STATUS_ERROR.getException().getErrorMsg(), e.getMessage());
                }
            });
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }

}
