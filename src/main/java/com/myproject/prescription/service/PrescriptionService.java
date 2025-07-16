package com.myproject.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myproject.prescription.dao.entity.PrescriptionEntity;
import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;

public interface PrescriptionService extends IService<PrescriptionEntity> {
    long createPrescription(PrescriptionCreateCmd cmd);

    void fulfillPrescription(Long prescriptionId);
}
