package com.example.hospital.api.controller.form;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class InsertMedicalDeptForm {
    @NotBlank(message = "name can not be null")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,10}$",message = "name is not correct")
    private String name;

    @NotNull(message = "outpatient can not be null")
    private Boolean outpatient;

    @NotNull(message = "recommended can not be null")
    private Boolean recommended;

    @NotNull(message = "description can not be null")
    private String description;
}
