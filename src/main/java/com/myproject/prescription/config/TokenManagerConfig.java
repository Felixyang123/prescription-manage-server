package com.myproject.prescription.config;

import com.myproject.prescription.token.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class TokenManagerConfig {

    @Bean
    public TokenManager<DefaultTokenContext> tokenManager(TokenStorage<String, String> tokenStorage,
                                                          TokenGenerator<DefaultTokenContext> tokenGenerator) {
        DefaultTokenManager tokenManager = new DefaultTokenManager();
        tokenManager.setTokenStorage(tokenStorage);
        tokenManager.setTokenGenerator(tokenGenerator);
        return tokenManager;
    }

    @Bean
    public TokenStorage<String, String> tokenStorage(RedisTemplate<String, Object> redisTemplate) {
        return new RedisRemoteTokenStorage(redisTemplate);
    }

    @Bean
    public TokenGenerator<DefaultTokenContext> tokenGenerator() {
        return new UUIDTokenGenerator();
    }
}
