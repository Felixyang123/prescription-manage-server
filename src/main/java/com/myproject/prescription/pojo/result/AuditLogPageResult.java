package com.myproject.prescription.pojo.result;

import com.myproject.prescription.pojo.dto.PrescriptionDrugValidationResultDTO;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogPageResult implements Serializable {
    private static final long serialVersionUID = 368888041552251121L;

    private Long id;
    /**
     * 处方ID
     */
    private Long prescriptionId;

    /**
     * 患者ID
     */
    private String patientId;

    /**
     * 药房ID
     */
    private Long pharmacyId;
    /**
     * 0-失败 1-成功
     * @see com.myproject.prescription.enums.YesOrNoEnum
     */
    private Integer status;
    /**
     * 请求的药品
     */
    private List<PrescriptionItemDTO> requestedDrugs;
    /**
     * 配发的药品
     */
    private List<PrescriptionItemDTO> dispensedDrugs;
    /**
     * 失败原因
     */
    private List<PrescriptionDrugValidationResultDTO> failureReasons;
}
