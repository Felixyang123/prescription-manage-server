package com.myproject.prescription.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.DrugEntity;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.entity.PharmacyEntity;
import com.myproject.prescription.dao.mapper.PharmacyMapper;
import com.myproject.prescription.pojo.PageRequest;
import com.myproject.prescription.pojo.PageResponse;
import com.myproject.prescription.pojo.dto.DrugDTO;
import com.myproject.prescription.pojo.query.PharmacyPageQuery;
import com.myproject.prescription.pojo.result.PharmacyPageResult;
import com.myproject.prescription.service.DrugService;
import com.myproject.prescription.service.PharmacyDrugService;
import com.myproject.prescription.service.PharmacyService;
import com.myproject.prescription.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyServiceImpl extends ServiceImpl<PharmacyMapper, PharmacyEntity> implements PharmacyService {
    private final PharmacyDrugService pharmacyDrugService;
    private final DrugService drugService;

    @Override
    public PageResponse<PharmacyPageResult> pageQueryPharmacyWithDrugs(PageRequest<PharmacyPageQuery> pageQuery) {
        PharmacyPageQuery req = pageQuery.getReq();
        Page<PharmacyEntity> page = page(new Page<>(pageQuery.getPage(), pageQuery.getSize()), Wrappers.<PharmacyEntity>lambdaQuery()
                .likeRight(StringUtils.isNotBlank(req.getName()), PharmacyEntity::getName, req.getName())
                .orderByDesc(PharmacyEntity::getId));
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageUtils.emptyPage(pageQuery);
        }

        List<PharmacyEntity> records = page.getRecords();
        List<Long> pharmacyIds = records.stream().map(PharmacyEntity::getId).collect(Collectors.toList());
        List<PharmacyDrugEntity> pharmacyDrugs = pharmacyDrugService.list(Wrappers.<PharmacyDrugEntity>lambdaQuery().in(PharmacyDrugEntity::getPharmacyId, pharmacyIds));
        if (CollectionUtils.isEmpty(pharmacyDrugs)) {
            return PageUtils.build(page, pharmacyEntity -> PharmacyPageResult.builder().pharmacyId(pharmacyEntity.getId())
                    .pharmacyName(pharmacyEntity.getName()).pharmacyAddress(pharmacyEntity.getAddress()).build());
        }

        Set<Long> drugIds = pharmacyDrugs.stream().map(PharmacyDrugEntity::getDrugId).collect(Collectors.toSet());
        List<DrugEntity> drugs = drugService.list(Wrappers.<DrugEntity>lambdaQuery().in(DrugEntity::getId, drugIds));
        Map<Long, DrugEntity> drugMap = drugs.stream().collect(Collectors.toMap(DrugEntity::getId, Function.identity()));
        Map<Long, List<PharmacyDrugEntity>> pharmacyDrugsMap = pharmacyDrugs.stream().collect(Collectors.groupingBy(PharmacyDrugEntity::getPharmacyId));
        return PageUtils.build(page, pharmacyEntity -> {
            PharmacyPageResult pharmacyPageResult = PharmacyPageResult.builder().pharmacyId(pharmacyEntity.getId())
                    .pharmacyName(pharmacyEntity.getName()).pharmacyAddress(pharmacyEntity.getAddress()).build();
            List<PharmacyDrugEntity> pharmacyDrugEntities = pharmacyDrugsMap.get(pharmacyEntity.getId());
            List<DrugDTO> drugDTOS = new ArrayList<>();
            for (PharmacyDrugEntity pharmacyDrugEntity : pharmacyDrugEntities) {
                DrugEntity drugEntity = drugMap.get(pharmacyDrugEntity.getDrugId());
                if (drugEntity != null) {
                    drugDTOS.add(DrugDTO.builder().id(drugEntity.getId()).name(drugEntity.getName()).batchNumber(drugEntity.getBatchNumber())
                            .expiryDate(drugEntity.getExpiryDate()).manufacturer(drugEntity.getManufacturer()).stock(pharmacyDrugEntity.getCurrentStock()).build());
                }
            }
            pharmacyPageResult.setDrugs(drugDTOS);
            return pharmacyPageResult;
        });
    }
}
