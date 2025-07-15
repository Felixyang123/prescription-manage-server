package com.myproject.prescription.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PrescriptionBizException extends RuntimeException{
    private static final long serialVersionUID = 6628364517026469912L;

    private final String errorCode;

    private final String errorMsg;

    public PrescriptionBizException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}
