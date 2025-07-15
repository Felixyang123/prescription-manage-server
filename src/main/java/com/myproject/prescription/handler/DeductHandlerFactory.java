package com.myproject.prescription.handler;

import com.myproject.prescription.utils.ApplicationContextUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeductHandlerFactory {
    private static final Map<DeductStockTypeEnum, DeductStockHandler> HANDLER_MAP = new ConcurrentHashMap<>();

    static {
        List<DeductStockHandler> handlers = ApplicationContextUtils.getBeanList(DeductStockHandler.class);
        handlers.forEach(handler -> HANDLER_MAP.putIfAbsent(handler.getDeductType(), handler));
    }

    public static DeductStockHandler select(DeductStockContext ctx) {
        return ctx.getDeductType() == null ? ApplicationContextUtils.getBean(DistributeLockHandler.class) :
                HANDLER_MAP.getOrDefault(ctx.getDeductType(), ApplicationContextUtils.getBean(DistributeLockHandler.class));
    }
}
