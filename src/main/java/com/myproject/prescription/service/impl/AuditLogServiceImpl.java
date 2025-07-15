package com.myproject.prescription.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.AuditLogEntity;
import com.myproject.prescription.dao.entity.PrescriptionEntity;
import com.myproject.prescription.dao.mapper.AuditLogMapper;
import com.myproject.prescription.enums.PrescriptionOperateStatusEnum;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.pojo.query.AuditLogPageQuery;
import com.myproject.prescription.pojo.result.AuditLogPageResult;
import com.myproject.prescription.service.AuditLogService;
import com.myproject.prescription.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLogEntity> implements AuditLogService {

    @Override
    public PageResponse<AuditLogPageResult> pageQueryAuditLogs(PageRequest<AuditLogPageQuery> pageRequest) {
        AuditLogPageQuery req = pageRequest.getReq();
        Page<AuditLogEntity> page = page(new Page<AuditLogEntity>(pageRequest.getPage(), pageRequest.getSize()),
                Wrappers.<AuditLogEntity>lambdaQuery().eq(StringUtils.isNotBlank(req.getPatientId()), AuditLogEntity::getPatientId, req.getPatientId())
                        .eq(req.getPharmacyId() != null, AuditLogEntity::getPharmacyId, req.getPharmacyId())
                        .eq(req.getStatus() != null, AuditLogEntity::getStatus, req.getStatus())
                        .orderByDesc(AuditLogEntity::getId));

        return PageUtils.build(page, auditLogEntity -> {
            AuditLogPageResult auditLogPageResult = AuditLogPageResult.builder().id(auditLogEntity.getId()).patientId(auditLogEntity.getPatientId()).pharmacyId(auditLogEntity.getPharmacyId())
                    .prescriptionId(auditLogEntity.getPrescriptionId()).status(auditLogEntity.getStatus()).build();
            if (StringUtils.isNotBlank(auditLogEntity.getRequestedDrugs())) {
                List<PrescriptionItemDTO> requestedDrugs = JSON.parseArray(auditLogEntity.getRequestedDrugs(), PrescriptionItemDTO.class);
                auditLogPageResult.setRequestedDrugs(requestedDrugs);
            }
            if (StringUtils.isNotBlank(auditLogEntity.getDispensedDrugs())) {
                List<PrescriptionItemDTO> dispensedDrugs = JSON.parseArray(auditLogEntity.getDispensedDrugs(), PrescriptionItemDTO.class);
                auditLogPageResult.setDispensedDrugs(dispensedDrugs);
            }
            if (StringUtils.isNotBlank(auditLogEntity.getFailureReason())) {
                List<PrescriptionDrugValidationResultDTO> drugFailures = JSON.parseArray(auditLogEntity.getFailureReason(), PrescriptionDrugValidationResultDTO.class);
                auditLogPageResult.setFailureReasons(drugFailures);
            }
            return auditLogPageResult;
        });
    }

    @Override
    @Async(value = "asyncDefaultExecutor")
    public void logSuccess(PrescriptionEntity prescription, String drugsInfo) {
        AuditLogEntity auditLogEntity = AuditLogEntity.builder().prescriptionId(prescription.getId()).patientId(prescription.getPatientId())
                .pharmacyId(prescription.getPharmacyId()).status(PrescriptionOperateStatusEnum.SUCCESS.getCode()).requestedDrugs(drugsInfo)
                .dispensedDrugs(drugsInfo).build();
        save(auditLogEntity);
    }

    @Override
    @Async(value = "asyncDefaultExecutor")
    public void logFailure(PrescriptionEntity prescription, String drugsInfo, String reason) {
        AuditLogEntity auditLogEntity = AuditLogEntity.builder().prescriptionId(prescription.getId()).patientId(prescription.getPatientId())
                .pharmacyId(prescription.getPharmacyId()).status(PrescriptionOperateStatusEnum.FAIL.getCode())
                .requestedDrugs(drugsInfo).failureReason(reason).build();
        save(auditLogEntity);
    }

}
