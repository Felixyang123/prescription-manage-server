package com.myproject.prescription.lock;

import com.myproject.prescription.enums.BizExceptionEnum;
import com.myproject.prescription.utils.AssertUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现分布式锁
 */
@Slf4j
@RequiredArgsConstructor
public class RedisDistributeLock implements Lock {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private static ThreadLocal<Map<String, Integer>> lockCounts = ThreadLocal.withInitial(HashMap::new);

    private final String instanceId = UUID.randomUUID().toString();

    @Override
    public boolean tryLock(String key) {
        Map<String, Integer> lockCntMap = lockCounts.get();
        Integer lockCnt = lockCntMap.get(key);
        if (lockCnt != null) {
            lockCntMap.put(key, ++lockCnt);
            return true;
        }
        String lockVal = instanceId + ":" + Thread.currentThread().getId();
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, lockVal, 90000, TimeUnit.MILLISECONDS);
        if (Boolean.TRUE.equals(set)) {
            lockCntMap.put(key, 1);
            return true;
        }
        return false;
    }

    @Override
    public void lock(String key) {
        Map<String, Integer> lockCntMap = lockCounts.get();
        Integer lockCnt = lockCntMap.get(key);
        if (lockCnt != null) {
            lockCntMap.put(key, ++lockCnt);
            return;
        }
        while (!tryLock(key)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warn("Lock failed, cause: ", e);
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void release(String key) {
        Map<String, Integer> lockCntMap = lockCounts.get();
        Integer lockCnt = lockCntMap.get(key);
        // TODO 优化异常，抽象锁，区分业务异常和系统异常
        AssertUtils.notNull(lockCnt, BizExceptionEnum.RELEASE_OTHER_LOCK_ERROR.getException());

        lockCnt--;
        if (lockCnt < 1) {
            lockCntMap.remove(key);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            redisTemplate.execute(script, Collections.singletonList(key), instanceId + Thread.currentThread().getId());
        } else {
            lockCntMap.put(key, lockCnt);
        }

    }
}
