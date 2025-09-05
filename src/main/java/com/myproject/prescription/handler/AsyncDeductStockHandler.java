package com.myproject.prescription.handler;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.mapper.DrugMapper;
import com.myproject.prescription.lock.LockSupport;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.service.PharmacyDrugService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Redis+Lua预扣库存，发送MQ，消费者异步扣减库存落库
 */
@Component
@RequiredArgsConstructor
public class AsyncDeductStockHandler implements DeductStockHandler {
    private static final String DEDUCT_STOCK_LUA = "local stock = tonumber(redis.call('get', KEYS[1])) if stock >= tonumber(ARGV[1]) then redis.call('decrby', KEYS[1], ARGV[1]) return true else return false end;";

    private final StringRedisTemplate redisTemplate;
    private final DrugMapper drugMapper;
    private final PharmacyDrugService pharmacyDrugService;
    private final ThreadPoolTaskExecutor asyncDefaultExecutor;

    private DefaultRedisScript<Boolean> deductStockScript;

    @PostConstruct
    public void init() {
        deductStockScript = new DefaultRedisScript<>();
        deductStockScript.setScriptText(DEDUCT_STOCK_LUA);
        deductStockScript.setResultType(Boolean.class);

        // 加载所有药品信息到Redis
        // 加载所有药房药品库存到Redis
    }

    @Override
    public boolean deductStock(DeductStockContext ctx) {
        PrescriptionCreateCmd cmd = ctx.getCmd();
        List<PrescriptionDrugValidationResultDTO> validationResults = validateDrugsLockStock(cmd.getPharmacyId(), cmd.getDrugs());
        ctx.setValidationResults(validationResults);
        boolean invalid = validationResults.stream().anyMatch(result -> CollectionUtils.isNotEmpty(result.getFailures()));
        if (invalid) {
            return false;
        }

        // 使用Redis+Lua预扣库存
        boolean deducted = preDeductStockInRedis(cmd.getDrugs());
        if (deducted) {
            // 提交线程池，异步扣减库存
            // TODO 优化方向：使用MQ
            CompletableFuture.runAsync(() -> pharmacyDrugService.lockDrugStocks(cmd.getPharmacyId(), cmd.getDrugs()), asyncDefaultExecutor);
        }
        return deducted;
    }

    @Override
    public DeductStockTypeEnum getDeductType() {
        return DeductStockTypeEnum.ASYNC;
    }

    /**
     * 使用Redis+Lua预扣库存
     *
     * @param drugs 药品列表
     * @return 是否预扣成功
     */
    private boolean preDeductStockInRedis(List<PrescriptionItemDTO> drugs) {
        drugs.sort(Comparator.comparing(PrescriptionItemDTO::getDrugId));

        for (PrescriptionItemDTO item : drugs) {
            String stockKey = "STOCK:DRUG:" + item.getDrugId();
            Boolean success = redisTemplate.execute(
                    deductStockScript,
                    Collections.singletonList(stockKey),
                    String.valueOf(item.getQuantity())
            );

            if (!success) {
                // 如果预扣失败，需要回滚之前已预扣的库存
                rollbackPreDeductedStock(drugs, item);
                return false;
            }
        }
        return true;
    }

    /**
     * 回滚已预扣的库存
     *
     * @param drugs      药品列表
     * @param failedItem 失败的项目
     */
    private void rollbackPreDeductedStock(List<PrescriptionItemDTO> drugs, PrescriptionItemDTO failedItem) {
        for (PrescriptionItemDTO item : drugs) {
            if (item.getDrugId().equals(failedItem.getDrugId())) {
                break; // 遇到失败的项目就停止回滚
            }
            String stockKey = "STOCK:DRUG:" + item.getDrugId();
            redisTemplate.opsForValue().increment(stockKey, item.getQuantity());
        }
    }

    /**
     * 验证药品库存（与DistributeLockHandler保持一致的逻辑）
     *
     * @param pharmacyId 药房ID
     * @param drugsAdd   药品列表
     * @return 验证结果
     */
    // TODO 优化方向：redis缓存药品信息，校验时药品信息从redis查询
    private List<PrescriptionDrugValidationResultDTO> validateDrugsLockStock(Long pharmacyId, List<PrescriptionItemDTO> drugsAdd) {
        // 将处方单药品排序，按顺序加锁，避免死锁
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
