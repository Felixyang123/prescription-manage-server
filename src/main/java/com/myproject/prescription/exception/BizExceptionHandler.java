package com.myproject.prescription.exception;

import com.alibaba.fastjson.JSON;
import com.myproject.prescription.enums.BizExceptionEnum;
import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class BizExceptionHandler {

    @ExceptionHandler(PrescriptionBizException.class)
    public Result<Void> handleBizException(PrescriptionBizException bizException) {
        return Results.error(bizException.getErrorCode(), bizException.getErrorMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handlerMethodException(MethodArgumentNotValidException exception) {
        Map<String, String> result = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError -> result.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return Results.error(BizExceptionEnum.PARAM_VALIDATE_ERROR.getException().getErrorCode(), JSON.toJSONString(result));
    }
}
