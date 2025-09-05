package com.myproject.prescription.pojo.command;

import com.myproject.prescription.pojo.InsertGroup;
import com.myproject.prescription.pojo.dto.PrescriptionItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PrescriptionCreateCmd implements Serializable {
    private static final long serialVersionUID = -4006889628115656453L;

    @NotBlank(message = "患者ID不能为空", groups = InsertGroup.class)
    private String patientId;

    @NotNull(message = "药房ID不能为空", groups = InsertGroup.class)
    private Long pharmacyId;

    @Valid
    @Size(message = "处方药品不能为空", min = 1, groups = InsertGroup.class)
    @NotNull(message = "处方药品不能为空", groups = InsertGroup.class)
    private List<PrescriptionItemDTO> drugs;

    @NotBlank(message = "Token不能为空", groups = InsertGroup.class)
    private String token;
}
