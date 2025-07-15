package com.myproject.prescription.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PrescriptionDrugValidationResultDTO implements Serializable {
    private static final long serialVersionUID = 5597449145288187872L;

    private Long drugId;

    private String drugName;

    private List<String> failures;

    public void addFailure(String failure) {
        if (failures == null) {
            failures = new ArrayList<>();
        }
        failures.add(failure);
    }
}
