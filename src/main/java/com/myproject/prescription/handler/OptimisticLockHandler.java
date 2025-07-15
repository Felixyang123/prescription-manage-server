package com.myproject.prescription.handler;

import com.myproject.prescription.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 乐观锁预扣库存
 */
@Component
@RequiredArgsConstructor
public class OptimisticLockHandler implements DeductStockHandler {
    private static final int MAX_SPIN_CNT = 10;
    private final DrugService drugService;

    @Override
    public boolean deductStock(DeductStockContext ctx) {
        int spinCnt = 0;
        while (spinCnt++ < MAX_SPIN_CNT && !drugService.deductStockWithOptimisticLock()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                if (Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return spinCnt < MAX_SPIN_CNT;
    }

    @Override
    public DeductStockTypeEnum getDeductType() {
        return DeductStockTypeEnum.OPTIMIZE_LOCK;
    }
}
