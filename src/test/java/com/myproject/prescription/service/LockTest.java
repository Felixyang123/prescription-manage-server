package com.myproject.prescription.service;

import com.myproject.prescription.lock.RedisDistributeLock;
import com.myproject.prescription.lock.WatchDogExecutor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class LockTest {

    @Autowired
    private RedisDistributeLock redisDistributeLock;
    @Autowired
    private WatchDogExecutor watchDogExecutor;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @SneakyThrows
    @Test
    void lockTest() {
        String key = "testlock001";
        boolean lock = redisDistributeLock.tryLock(key, 6000);
        if (lock) {
            Thread.sleep(7000);
            Object val = redisTemplate.opsForValue().get(key);
            Assertions.assertNotNull(val);
            Long ttl = redisTemplate.getExpire(key);
            Assertions.assertTrue(ttl > 0);
        }
    }

    @SneakyThrows
    @Test
    void releaseLockTest() {
        String key = "testlock002";
        boolean lock = redisDistributeLock.tryLock(key, 6000);
        if (lock) {
            Thread.sleep(3000);
            redisDistributeLock.release(key);
            Object val = redisTemplate.opsForValue().get(key);
            Assertions.assertNull(val);
            Thread.sleep(6000);
        }
    }
}
