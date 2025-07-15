package com.myproject.prescription.exception;

import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class BizExceptionHandler {

    @ExceptionHandler(PrescriptionBizException.class)
    public Result<Void> handleBizException(PrescriptionBizException bizException) {
        return Results.error(bizException.getErrorCode(), bizException.getErrorMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handlerMethodException(MethodArgumentNotValidException exception) {
        return Results.error(Optional.ofNullable(exception.getBindingResult().getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage).orElse("参数异常"));
    }
}
