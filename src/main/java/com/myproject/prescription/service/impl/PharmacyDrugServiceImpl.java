package com.myproject.prescription.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myproject.prescription.dao.entity.PharmacyDrugEntity;
import com.myproject.prescription.dao.mapper.DrugMapper;
import com.myproject.prescription.dao.mapper.PharmacyDrugMapper;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import com.myproject.prescription.service.PharmacyDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PharmacyDrugServiceImpl extends ServiceImpl<PharmacyDrugMapper, PharmacyDrugEntity> implements PharmacyDrugService {
    private final DrugMapper drugMapper;

    /**
     * 预扣库存
     * 当前采用同步扣减库存，如果系统并发量很高，同步模式达到性能瓶颈，可采用异步模式
     * 异步模式需要Redis+MQ实现，Redis预加载药品库存，创建处方单时结合Lua脚本实现库存预扣
     * 库存预扣成功则发送MQ消息，返回前端处方单ID，MQ消费者处理数据库库存扣减
     * 前端根据返回的处方单ID查询处方单状态，如果状态为成功则返回给用户，如果状态为失败则返回失败原因
     *
     * @param pharmacyId
     * @param drugs
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void lockDrugStocks(Long pharmacyId, List<PrescriptionItemDTO> drugs) {
        List<PharmacyDrugEntity> pharmacyDrugEntities = list(Wrappers.<PharmacyDrugEntity>lambdaQuery().eq(PharmacyDrugEntity::getPharmacyId, pharmacyId)
                .in(PharmacyDrugEntity::getDrugId, drugs.stream().map(PrescriptionItemDTO::getDrugId).collect(Collectors.toSet())));
        Map<Long, PharmacyDrugEntity> pharmacyDrugMap = pharmacyDrugEntities.stream().collect(Collectors.toMap(PharmacyDrugEntity::getDrugId, Function.identity()));
        for (PrescriptionItemDTO prescriptionItemDTO : drugs) {
            drugMapper.lockStock(prescriptionItemDTO.getDrugId(), prescriptionItemDTO.getQuantity());
            // 查询出药房对应的药瓶品，然后跟pharmacyDrug.id更新，避免因为mysql index merge 在同时使用pharmacyId&drugId索引更新时导致的死锁
            PharmacyDrugEntity pharmacyDrugEntity = pharmacyDrugMap.get(prescriptionItemDTO.getDrugId());
            if (pharmacyDrugEntity != null) {
                getBaseMapper().lockStock(pharmacyDrugEntity.getId(), prescriptionItemDTO.getQuantity());
            }
        }
    }
}
