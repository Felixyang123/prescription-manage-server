package com.myproject.prescription.handler;

import com.myproject.prescription.pojo.command.PrescriptionCreateCmd;
import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeductStockContext {
    private DeductStockTypeEnum deductType;

    private PrescriptionCreateCmd cmd;

    private List<PrescriptionDrugValidationResultDTO> validationResults;

}
