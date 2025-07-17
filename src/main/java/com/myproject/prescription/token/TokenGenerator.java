package com.myproject.prescription.token;

public interface TokenGenerator<T extends TokenContext> {
    String generateToken(T ctx);
}
