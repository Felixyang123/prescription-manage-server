package com.myproject.prescription.utils;

import com.myproject.prescription.exception.PrescriptionBizException;

import java.util.Objects;

public class AssertUtils {

    public static <T> void notNull(T data, PrescriptionBizException exception) {
        if (data == null) {
            throw exception;
        }
    }

    public static <T> void eq(T first, T second, PrescriptionBizException exception) {
        if (!Objects.equals(first, second)) {
            throw exception;
        }
    }

    public static void assertTrue(boolean check, PrescriptionBizException exception) {
        if (!check) {
            throw exception;
        }
    }


}
