package com.myproject.prescription.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.PrescriptionItemEntity;
import com.myproject.prescription.dao.mapper.PrescriptionItemMapper;
import com.myproject.prescription.service.PrescriptionItemService;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionItemServiceImpl extends ServiceImpl<PrescriptionItemMapper, PrescriptionItemEntity> implements PrescriptionItemService {
}
