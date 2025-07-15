package com.myproject.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myproject.prescription.dao.entity.AuditLogEntity;
import com.myproject.prescription.dao.entity.PrescriptionEntity;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.query.AuditLogPageQuery;
import com.myproject.prescription.pojo.result.AuditLogPageResult;

public interface AuditLogService extends IService<AuditLogEntity> {
    PageResponse<AuditLogPageResult> pageQueryAuditLogs(PageRequest<AuditLogPageQuery> pageRequest);

    void logSuccess(PrescriptionEntity prescription, String drugsInfo);

    void logFailure(PrescriptionEntity prescription, String drugsInfo, String reason);
}
