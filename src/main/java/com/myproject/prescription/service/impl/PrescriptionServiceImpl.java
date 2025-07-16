package com.myproject.prescription.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.entity.PrescriptionEntity;
import com.myproject.prescription.dao.entity.PrescriptionItemEntity;
import com.myproject.prescription.dao.mapper.DrugMapper;
import com.myproject.prescription.dao.mapper.PharmacyDrugMapper;
import com.myproject.prescription.dao.mapper.PrescriptionMapper;
import com.myproject.prescription.enums.BizExceptionEnum;
import com.myproject.prescription.enums.PrescriptionStatusEnum;
import com.myproject.prescription.handler.DeductHandlerFactory;
import com.myproject.prescription.handler.DeductStockContext;
import com.myproject.prescription.handler.DeductStockHandler;
import com.myproject.prescription.handler.DeductStockTypeEnum;
import com.myproject.prescription.lock.LockSupport;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.service.*;
import com.myproject.prescription.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, PrescriptionEntity> implements PrescriptionService {
    private final PrescriptionItemService prescriptionItemService;
    private final DrugService drugService;
    private final PharmacyDrugService pharmacyDrugService;
    private final AuditLogService auditLogService;
    private final DrugMapper drugMapper;
    private final PharmacyDrugMapper pharmacyDrugMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public long createPrescription(PrescriptionCreateCmd cmd) {
        // 创建处方单
        PrescriptionEntity prescriptionAdd = PrescriptionEntity.builder().pharmacyId(cmd.getPharmacyId()).patientId(cmd.getPatientId()).status(PrescriptionStatusEnum.CREATED.getCode()).build();
        save(prescriptionAdd);

        // 创建处方单明细
        List<PrescriptionItemEntity> prescriptionItemsAdd = cmd.getDrugs().stream().map(drugCmd -> PrescriptionItemEntity.builder().prescriptionId(prescriptionAdd.getId())
                .drugId(drugCmd.getDrugId()).dosage(drugCmd.getDosage()).quantity(drugCmd.getQuantity()).build()).collect(Collectors.toList());
        prescriptionItemService.saveBatch(prescriptionItemsAdd);

        DeductStockContext deductStockContext = DeductStockContext.builder().deductType(DeductStockTypeEnum.DISTRIBUTE_LOCK).cmd(cmd).build();
        DeductStockHandler deductStockHandler = DeductHandlerFactory.select(deductStockContext);
        boolean deducted = deductStockHandler.deductStock(deductStockContext);

        if (!deducted) {
            // 更新预扣库存失败
            PrescriptionEntity prescriptionUpdate = PrescriptionEntity.builder().id(prescriptionAdd.getId()).status(PrescriptionStatusEnum.FAIL.getCode()).build();
            updateById(prescriptionUpdate);
            auditLogService.logFailure(prescriptionAdd, JSON.toJSONString(cmd.getDrugs()), JSON.toJSONString(deductStockContext.getValidationResults()));
            return prescriptionAdd.getId();
        }

        // 更新预扣库存成功
        auditLogService.logSuccess(prescriptionAdd, JSON.toJSONString(cmd.getDrugs()));
        return prescriptionAdd.getId();
    }


    private List<PrescriptionDrugValidationResultDTO> validateDrugsReduceStock(Long pharmacyId, List<PrescriptionItemEntity> items) {
        // 将处方单药品排序， 按顺序加锁，避免死锁
        items.sort(Comparator.comparing(PrescriptionItemEntity::getDrugId));
        List<PrescriptionDrugValidationResultDTO> results = new ArrayList<>();
        Date now = new Date();

        for (PrescriptionItemEntity item : items) {
            Long drugId = item.getDrugId();
            Integer quantity = item.getQuantity();
            LockSupport.lockAndExecute("LOCK:PRESCRIPTION:STOCK:" + drugId, () -> {
                PrescriptionDrugValidationResultDTO result = new PrescriptionDrugValidationResultDTO();
                result.setDrugId(drugId);

                // 检查药品有效期
                DrugEntity drug = drugService.getById(drugId);
                if (drug == null) {
                    result.addFailure("药品不存在");
                    results.add(result);
                    return null;
                }
                if (drug.getExpiryDate().before(now)) {
                    result.addFailure("药品已过期");
                }

                // 检查全局库存
                if (drug.getStock() < quantity) {
                    result.addFailure("药品库存不足");
                }

                // 检查药房分配
                PharmacyDrugEntity pharmacyDrug = pharmacyDrugService.getOne(Wrappers.<PharmacyDrugEntity>lambdaQuery()
                        .eq(PharmacyDrugEntity::getPharmacyId, pharmacyId).eq(PharmacyDrugEntity::getDrugId, drugId));
                if (pharmacyDrug == null) {
                    result.addFailure("药房未分配该药品");
                } else if (pharmacyDrug.getStock() < quantity) {
                    result.addFailure("药房分配库存不足");
                }

                result.setDrugName(drug.getName());
                results.add(result);
                return null;
            });
        }
        return results;
    }

    /**
     * 预扣库存
     * 当前采用同步扣减库存，如果系统并发量很高，同步模式达到性能瓶颈，可采用异步模式
     * 异步模式需要Redis+MQ实现，Redis预加载药品库存，创建处方单时结合Lua脚本实现库存预扣
     * 库存预扣成功则发送MQ消息，返回前端处方单ID，MQ消费者处理数据库库存扣减
     * 前端根据返回的处方单ID查询处方单状态，如果状态为成功则返回给用户，如果状态为失败则返回失败原因
     *
     * @param pharmacyId
     * @param drugsAdd
     * @param reverse
     */
    private void lockDrugStocks(Long pharmacyId, List<PrescriptionItemDTO> drugsAdd, boolean reverse) {
        List<PharmacyDrugEntity> pharmacyDrugEntities = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacyId)
                .in(PharmacyDrugEntity::getDrugId, drugsAdd.stream().map(PrescriptionItemDTO::getDrugId).collect(Collectors.toSet())));
        Map<Long, PharmacyDrugEntity> pharmacyDrugMap = pharmacyDrugEntities.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        for (PrescriptionItemDTO prescriptionItemDTO : drugsAdd) {
            Integer quantity = reverse ? -prescriptionItemDTO.getQuantity() : prescriptionItemDTO.getQuantity();
            drugMapper.lockStock(prescriptionItemDTO.getDrugId(), quantity);
            // 查询出药房对应的药瓶品，然后跟pharmacyDrug.id更新，避免因为mysql index merge 在同时使用pharmacyId&drugId索引更新时导致的死锁
            PharmacyDrugEntity pharmacyDrugEntity = pharmacyDrugMap.get(prescriptionItemDTO.getDrugId());
            if (pharmacyDrugEntity != null) {
                pharmacyDrugMapper.lockStock(pharmacyDrugEntity.getId(), quantity);
            }
        }
    }

    /**
     * 释放预扣库存
     *
     * @param pharmacyId
     * @param prescriptionItems
     */
    private void releaseDrugStocks(Long pharmacyId, List<PrescriptionItemEntity> prescriptionItems) {
        Set<Long> drugIds = prescriptionItems.stream().map(PrescriptionItemEntity::getDrugId).collect(Collectors.toSet());
        List<PharmacyDrugEntity> pharmacyDrugEntities = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacyId)
                .in(PharmacyDrugEntity::getDrugId, drugIds));
        Map<Long, PharmacyDrugEntity> pharmacyDrugMap = pharmacyDrugEntities.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        for (PrescriptionItemEntity prescriptionItem : prescriptionItems) {
            Integer quantity = -prescriptionItem.getQuantity();
            drugMapper.lockStock(prescriptionItem.getDrugId(), quantity);
            // 查询出药房对应的药瓶品，然后跟pharmacyDrug.id更新，避免因为mysql index merge 在同时使用pharmacyId&drugId索引更新时导致的死锁
            PharmacyDrugEntity pharmacyDrugEntity = pharmacyDrugMap.get(prescriptionItem.getDrugId());
            if (pharmacyDrugEntity != null) {
                pharmacyDrugMapper.lockStock(pharmacyDrugEntity.getId(), quantity);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void fulfillPrescription(Long prescriptionId) {
        LockSupport.lockAndExecute("LOCK:PRESCRIPTION:FULFILL:" + prescriptionId, () -> {
            PrescriptionEntity prescriptionEntity = getById(prescriptionId);
            // 校验处方单存在
            AssertUtils.notNull(prescriptionEntity, BizExceptionEnum.PRESCRIPTION_NOT_EXISTS.getException());
            // 使用状态幂等，同一个处方单只会处理一次
            AssertUtils.eq(PrescriptionStatusEnum.CREATED.getCode(), prescriptionEntity.getStatus(), BizExceptionEnum.PRESCRIPTION_FULFILL_STATUS_ERROR.getException());

            List<PrescriptionItemEntity> prescriptionItemEntities = prescriptionItemService.list(Wrappers.<PrescriptionItemEntity>lambdaQuery()
                    .eq(PrescriptionItemEntity::getPrescriptionId, prescriptionId));
            // 校验处方单药品明细存在
            AssertUtils.assertTrue(CollectionUtils.isNotEmpty(prescriptionItemEntities), BizExceptionEnum.PRESCRIPTION_DRUGS_NOT_EXIST.getException());

            // 不需要校验药品当前可用库存，因为可用库存已经在创建的时候预扣了，这里理论上不需要校验，只需要校验真实库存是否足够预扣的库存
            List<PrescriptionDrugValidationResultDTO> validationResultDTOS = validateDrugsReduceStock(prescriptionEntity.getPharmacyId(), prescriptionItemEntities);
            boolean invalid = validationResultDTOS.stream().anyMatch(result -> CollectionUtils.isNotEmpty(result.getFailures()));
            if (invalid) {
                PrescriptionEntity prescriptionUpdate = PrescriptionEntity.builder().id(prescriptionId).status(PrescriptionStatusEnum.FAIL.getCode()).build();
                updateById(prescriptionUpdate);
                releaseDrugStocks(prescriptionEntity.getPharmacyId(), prescriptionItemEntities);
                auditLogService.logFailure(prescriptionEntity, JSON.toJSONString(prescriptionItemEntities), JSON.toJSONString(validationResultDTOS));
                return null;
            }

            PrescriptionEntity prescriptionUpdate = PrescriptionEntity.builder().id(prescriptionId).status(PrescriptionStatusEnum.SUCCESS.getCode()).build();
            updateById(prescriptionUpdate);
            // 扣减库存，释放预扣库存
            reduceDrugStocks(prescriptionEntity.getPharmacyId(), prescriptionItemEntities);
            auditLogService.logSuccess(prescriptionEntity, JSON.toJSONString(prescriptionItemEntities));
            return null;
        });
    }

    private void reduceDrugStocks(Long pharmacyId, List<PrescriptionItemEntity> items) {
        List<PharmacyDrugEntity> pharmacyDrugEntities = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacyId)
                .in(PharmacyDrugEntity::getDrugId, items.stream().map(PrescriptionItemEntity::getDrugId).collect(Collectors.toSet())));
        Map<Long, PharmacyDrugEntity> pharmacyDrugMap = pharmacyDrugEntities.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        for (PrescriptionItemEntity item : items) {
            drugMapper.reduceStock(item.getDrugId(), item.getQuantity());
            // 查询出药房对应的药瓶品，然后跟pharmacyDrug.id更新，避免因为mysql index merge 在同时使用pharmacyId&drugId索引更新时导致的死锁
            PharmacyDrugEntity pharmacyDrugEntity = pharmacyDrugMap.get(item.getDrugId());
            if (pharmacyDrugEntity != null) {
                pharmacyDrugMapper.reduceStock(pharmacyDrugEntity.getId(), item.getQuantity());
            }
        }
    }
}
