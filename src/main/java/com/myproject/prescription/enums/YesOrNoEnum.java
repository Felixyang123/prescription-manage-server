package com.myproject.prescription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum YesOrNoEnum {
    NO(0),
    YES(1);

    private final int code;
}
