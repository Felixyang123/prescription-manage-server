package com.myproject.prescription.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrescriptionStatusEnum {
    CREATED(0, "已创建"),
    SUCCESS(1, "成功"),
    FAIL(2, "失败");

    private final Integer code;

    private final String desc;
}
