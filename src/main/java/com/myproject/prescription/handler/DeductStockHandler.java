package com.myproject.prescription.handler;

/**
 * 预扣库存处理器
 */
public interface DeductStockHandler {

    boolean deductStock(DeductStockContext ctx);

    DeductStockTypeEnum getDeductType();
}
