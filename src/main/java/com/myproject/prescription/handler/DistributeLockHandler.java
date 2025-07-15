package com.myproject.prescription.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.mapper.DrugMapper;
import com.myproject.prescription.dao.mapper.PharmacyDrugMapper;
import com.myproject.prescription.lock.LockSupport;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.service.PharmacyDrugService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 悲观锁预扣库存
 */
@Component
@RequiredArgsConstructor
public class DistributeLockHandler implements DeductStockHandler {
    private final PharmacyDrugService pharmacyDrugService;
    private final DrugMapper drugMapper;
    private final PharmacyDrugMapper pharmacyDrugMapper;
    @Override
    public boolean deductStock(DeductStockContext ctx) {
        PrescriptionCreateCmd cmd = ctx.getCmd();
        List<PrescriptionDrugValidationResultDTO> validationResults = validateDrugsLockStock(cmd.getPharmacyId(), cmd.getDrugs());
        ctx.setValidationResults(validationResults);
        boolean invalid = validationResults.stream().anyMatch(result -> CollectionUtils.isNotEmpty(result.getFailures()));
        if (invalid) {
            return false;
        }
        lockDrugStocks(cmd.getPharmacyId(), cmd.getDrugs(), false);
        return true;
    }

    @Override
    public DeductStockTypeEnum getDeductType() {
        return DeductStockTypeEnum.DISTRIBUTE_LOCK;
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

    private List<PrescriptionDrugValidationResultDTO> validateDrugsLockStock(Long pharmacyId, List<PrescriptionItemDTO> drugsAdd) {
        // 将处方单药品排序， 按顺序加锁，避免死锁
        drugsAdd.sort(Comparator.comparing(PrescriptionItemDTO::getDrugId));
        List<PrescriptionDrugValidationResultDTO> results = new ArrayList<>();
        Date now = new Date();

        for (PrescriptionItemDTO prescriptionItemDTO : drugsAdd) {
            Long drugId = prescriptionItemDTO.getDrugId();
            Integer quantity = prescriptionItemDTO.getQuantity();
            LockSupport.lockAndExecute("LOCK:PRESCRIPTION:CREATE:" + drugId, () -> {
                PrescriptionDrugValidationResultDTO result = new PrescriptionDrugValidationResultDTO();
                result.setDrugId(drugId);

                // 检查药品有效期
                DrugEntity drug = drugMapper.selectById(drugId);
                if (drug == null) {
                    result.addFailure("药品不存在");
                    results.add(result);
                    return null;
                }
                if (drug.getExpiryDate().before(now)) {
                    result.addFailure("药品已过期");
                }

                // 检查全局库存
                if (drug.getCurrentStock() < quantity) {
                    result.addFailure("药品库存不足");
                }

                // 检查药房分配
                PharmacyDrugEntity pharmacyDrug = pharmacyDrugService.getOne(Wrappers.<PharmacyDrugEntity>lambdaQuery()
                        .eq(PharmacyDrugEntity::getPharmacyId, pharmacyId).eq(PharmacyDrugEntity::getDrugId, drugId));
                if (pharmacyDrug == null) {
                    result.addFailure("药房未分配该药品");
                } else if (pharmacyDrug.getCurrentStock() < quantity) {
                    result.addFailure("药房分配库存不足");
                }

                result.setDrugName(drug.getName());
                results.add(result);
                return null;
            });
        }
        return results;
    }
}
