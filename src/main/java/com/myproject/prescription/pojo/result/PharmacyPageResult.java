package com.myproject.prescription.pojo.result;

import com.myproject.prescription.pojo.dto.DrugDTO;
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
public class PharmacyPageResult implements Serializable {
    private static final long serialVersionUID = 4706879144610757684L;

    private Long pharmacyId;

    private String pharmacyName;

    private String pharmacyAddress;

    private List<DrugDTO> drugs;
}
