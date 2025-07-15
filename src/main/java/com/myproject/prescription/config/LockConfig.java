package com.myproject.prescription.config;

import com.myproject.prescription.lock.LocalLock;
import com.myproject.prescription.lock.Lock;
import com.myproject.prescription.lock.LockSupport;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 加载锁相关组件
 */
@Configuration
public class LockConfig {

    @Bean
    public LockSupport lockSupport(Lock lock) {
        return new LockSupport(lock);
    }

    @Bean
    @ConditionalOnMissingBean(value = Lock.class)
    public Lock lock() {
        return new LocalLock();
    }
}
