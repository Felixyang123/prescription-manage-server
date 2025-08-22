package com.myproject.prescription.enums;

import com.myproject.prescription.exception.PrescriptionBizException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BizExceptionEnum {
    PRESCRIPTION_NOT_EXISTS(new PrescriptionBizException("1000_1", "处方单不存在")),
    PRESCRIPTION_DRUGS_NOT_EXIST(new PrescriptionBizException("1000_2", "处方单药品为空")),

    PRESCRIPTION_FULFILL_STATUS_ERROR(new PrescriptionBizException("2000_1", "处方单履约状态异常")),
    PRESCRIPTION_TOKEN_EXPIRED_ERROR(new PrescriptionBizException("3000_1", "处方单重复创建")),

    PARAM_VALIDATE_ERROR(new PrescriptionBizException("4000_0", "参数异常")),
    RELEASE_OTHER_LOCK_ERROR(new PrescriptionBizException("4000_1", "释放锁异常（当前客户端不持有锁）"))
    ;
    private final PrescriptionBizException exception;
}
