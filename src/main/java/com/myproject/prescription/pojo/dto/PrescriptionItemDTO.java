package com.myproject.prescription.pojo.dto;

import com.myproject.prescription.pojo.InsertGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItemDTO {
    private Long id;
    /**
     * 处方ID
     */
    private Long prescriptionId;
    /**
     * 药品ID
     */
    @NotNull(message = "药品ID不能为空", groups = InsertGroup.class)
    private Long drugId;
    /**
     * 药品名称
     */
    @NotBlank(message = "药品名称不能为空", groups = InsertGroup.class)
    private String drugName;
    /**
     * 药品剂量
     */
    @NotBlank(message = "药品剂量不能为空", groups = InsertGroup.class)
    private String dosage;
    /**
     * 药品数量
     */
    @NotNull(message = "药品数量不能为空", groups = InsertGroup.class)
    private Integer quantity;
}