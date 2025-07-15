package com.myproject.prescription.controller;

import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.Result;
import com.myproject.prescription.pojo.Results;
import com.myproject.prescription.pojo.query.AuditLogPageQuery;
import com.myproject.prescription.pojo.result.AuditLogPageResult;
import com.myproject.prescription.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志控制器
 */
@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;

    /**
     * 分页查询处方单操作日志
     * @param pageRequest
     * @return
     */
    @PostMapping("/page")
    public Result<PageResponse<AuditLogPageResult>> queryAuditLogs(@RequestBody PageRequest<AuditLogPageQuery> pageRequest) {
        return Results.ok(auditLogService.pageQueryAuditLogs(pageRequest));
    }
}