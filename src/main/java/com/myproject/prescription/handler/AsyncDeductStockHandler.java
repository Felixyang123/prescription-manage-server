package com.myproject.prescription.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis+Lua预扣库存，发送MQ，消费者异步扣减库存落库
 */
@Component
@RequiredArgsConstructor
public class AsyncDeductStockHandler implements DeductStockHandler, InitializingBean {
    private static final String DEDUCT_STOCK_LUA = "local stock = tonumber(redis.call('get', KEYS[1])) if stock >= tonumber(ARGV[1]) then redis.call('decrby', KEYS[1], ARGV[1]) return true else return false end;";
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean deductStock(DeductStockContext ctx) {
        return true;
    }

    @Override
    public DeductStockTypeEnum getDeductType() {
        return DeductStockTypeEnum.ASYNC;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //TODO 预加载库存到Redis
    }
}
