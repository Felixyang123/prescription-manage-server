package com.myproject.prescription.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.mapper.PharmacyDrugMapper;
import com.myproject.prescription.service.PharmacyDrugService;
import org.springframework.stereotype.Service;

@Service
public class PharmacyDrugServiceImpl extends ServiceImpl<PharmacyDrugMapper, PharmacyDrugEntity> implements PharmacyDrugService {
}
