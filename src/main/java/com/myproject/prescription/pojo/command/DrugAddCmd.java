package com.myproject.prescription.pojo.command;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
