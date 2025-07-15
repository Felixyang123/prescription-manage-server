package com.myproject.prescription.controller;

import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import com.myproject.prescription.pojo.command.DrugAddCmd;
import com.myproject.prescription.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 药品控制器
 */
@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
public class DrugController {
    private final DrugService drugService;

    /**
     * 添加药品
     * @param cmd
     * @return
     */
    @PostMapping("/add")
    public Result<Void> addDrug(@RequestBody @Validated DrugAddCmd cmd) {
        drugService.addDrug(cmd);
        return Results.ok();
    }
}