package com.myproject.prescription.token;

import lombok.Data;

@Data
public class DefaultTokenContext implements TokenContext {
    private String token;
}
