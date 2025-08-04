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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
        pharmacyDrugService.lockDrugStocks(cmd.getPharmacyId(), cmd.getDrugs());
        return true;
    }

    @Override
    public DeductStockTypeEnum getDeductType() {
        return DeductStockTypeEnum.DISTRIBUTE_LOCK;
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
