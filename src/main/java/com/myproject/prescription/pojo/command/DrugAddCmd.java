package com.myproject.prescription.pojo.command;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class DrugAddCmd implements Serializable {
    private static final long serialVersionUID = 6871737521111607682L;

    @NotBlank(message = "药品名称不能为空")
    private String name;
    @NotBlank(message = "药品制造商不能为空")
    private String manufacturer;
    @NotBlank(message = "药品批次号不能为空")
    private String batchNumber;
    @NotNull(message = "药品过期日期不能为空")
    private Date expiryDate;
    @NotNull(message = "药品库存不能为空")
    @Min(value = 0, message = "药品库存不为负")
    private Integer stock;
}
