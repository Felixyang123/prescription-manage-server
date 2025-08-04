package com.myproject.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;

import java.util.List;

public interface PharmacyDrugService extends IService<PharmacyDrugEntity> {

    void lockDrugStocks(Long pharmacyId, List<PrescriptionItemDTO> drugs);
}
