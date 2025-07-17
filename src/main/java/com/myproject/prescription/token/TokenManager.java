package com.myproject.prescription.token;

public interface TokenManager<T extends TokenContext> {

    String generateToken(T ctx);

    boolean checkToken(T ctx);

    String deleteToken(T ctx);
}
