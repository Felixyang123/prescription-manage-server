package com.myproject.prescription.controller;

import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import com.myproject.prescription.token.DefaultTokenContext;
import com.myproject.prescription.token.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 防重Token控制器
 */
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {
    private final TokenManager<DefaultTokenContext> tokenManager;

    /**
     * 生成防重Token
     *
     * @return token
     */
    @PostMapping("/generator")
    public Result<String> generateToken() {
        return Results.ok(tokenManager.generateToken(new DefaultTokenContext()));
    }
}
