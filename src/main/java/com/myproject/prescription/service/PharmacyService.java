package com.myproject.prescription.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.myproject.prescription.dao.entity.PharmacyEntity;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.query.PharmacyPageQuery;
import com.myproject.prescription.pojo.result.PharmacyPageResult;

public interface PharmacyService extends IService<PharmacyEntity> {
    PageResponse<PharmacyPageResult> pageQueryPharmacyWithDrugs(PageRequest<PharmacyPageQuery> pageQuery);
}
