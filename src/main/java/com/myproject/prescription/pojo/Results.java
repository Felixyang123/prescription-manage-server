package com.myproject.prescription.pojo;

public class Results {

    public static <T> Result<T> ok(T data) {
        return Result.<T>builder().data(data).code("1").message("success").build();
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> error(String code, String errorMsg) {
        return Result.<T>builder().code(code).message(errorMsg).build();
    }

    public static <T> Result<T> error(String errorMsg) {
        return Result.<T>builder().code("-1").message(errorMsg).build();
    }

    public static <T> Result<T> error() {
        return Result.<T>builder().code("-1").message("fail").build();
    }
}
