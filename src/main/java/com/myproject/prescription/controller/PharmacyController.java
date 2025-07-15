package com.myproject.prescription.controller;

import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import com.myproject.prescription.pojo.query.PharmacyPageQuery;
import com.myproject.prescription.pojo.result.PharmacyPageResult;
import com.myproject.prescription.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 药房控制器
 */
@RestController
@RequestMapping("/pharmacies")
@RequiredArgsConstructor
public class PharmacyController {
    private final PharmacyService pharmacyService;

    /**
     * 分页查询药房及其分配的药品
     * @param pageQuery
     * @return
     */
    @PostMapping("/page")
    public Result<PageResponse<PharmacyPageResult>> listPharmacies(@RequestBody PageRequest<PharmacyPageQuery> pageQuery) {
        return Results.ok(pharmacyService.pageQueryPharmacyWithDrugs(pageQuery));
    }
}
