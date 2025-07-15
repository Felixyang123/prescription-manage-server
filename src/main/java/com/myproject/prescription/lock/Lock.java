package com.myproject.prescription.lock;

public interface Lock {

    boolean tryLock(String key);

    void lock(String key);

    void release(String key);
}
