package com.myproject.prescription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrescriptionOperateStatusEnum {
    FAIL(0, "失败"),
    SUCCESS(1, "成功");

    private final Integer code;

    private final String desc;
}
