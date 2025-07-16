package com.myproject.prescription.controller;

import com.myproject.prescription.pojo.InsertGroup;
import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 处方单控制器
 */
@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    /**
     * 创建处方单
     *
     * @param cmd
     * @return prescriptionId
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody @Validated(value = InsertGroup.class) PrescriptionCreateCmd cmd) {
        return Results.ok(prescriptionService.createPrescription(cmd));
    }

    /**
     * 履约处方单
     *
     * @param prescriptionId
     * @return
     */
    @PostMapping("/fulfill")
    public Result<Void> fulfill(@RequestParam(value = "prescriptionId") Long prescriptionId) {
        prescriptionService.fulfillPrescription(prescriptionId);
        return Results.ok();
    }
}
