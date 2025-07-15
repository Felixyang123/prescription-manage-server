package com.myproject.prescription.lock;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.function.Supplier;

public class LockSupport {
    private static Lock lock;

    public LockSupport(Lock lock) {
        LockSupport.lock = lock;
    }

    public static <T> T lockAndExecute(String key, Supplier<T> supplier) {
        lock.lock(key);
        try {
            return supplier.get();
        } finally {
            // 如果有事务，事务完成后再释放锁
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        lock.release(key);
                    }
                });
            } else {
                lock.release(key);
            }
        }
    }
}
