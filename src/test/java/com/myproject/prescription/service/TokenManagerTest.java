package com.myproject.prescription.service;

import com.myproject.prescription.token.DefaultTokenContext;
import com.myproject.prescription.token.TokenManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@SpringBootTest
public class TokenManagerTest {
    @Autowired
    private TokenManager<DefaultTokenContext> tokenManager;

    @Test
    @DisplayName("测试Token管理")
    void checkToken_test() {
        DefaultTokenContext context = new DefaultTokenContext();
        String token = tokenManager.generateToken(context);
        Assertions.assertNotNull(token);
        Assertions.assertEquals(token, context.getToken());

        boolean pass = tokenManager.checkToken(context);
        Assertions.assertTrue(pass);
        pass = tokenManager.checkToken(context);
        Assertions.assertFalse(pass);
    }

    @Test
    @DisplayName("测试并发Token管理")
    void checkToken_conc_test() {
        String token = tokenManager.generateToken(new DefaultTokenContext());
        AtomicInteger checkCnt = new AtomicInteger(0);
        CompletableFuture.allOf(IntStream.range(0, 10).mapToObj(i ->
                CompletableFuture.supplyAsync(() -> {
                    DefaultTokenContext context = new DefaultTokenContext();
                    context.setToken(token);
                    return tokenManager.checkToken(context);
                }).thenAccept(pass -> {
                    if (pass) {
                        checkCnt.addAndGet(1);
                    }
                })).toArray(CompletableFuture[]::new)).join();
        Assertions.assertEquals(1, checkCnt.get());
    }
}
