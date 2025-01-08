package com.example.hospital.api.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UpdateMedicalDeptSubForm {
    @NotNull(message = "id can not be null")
    @Min(value = 1, message = "id can not less than 1")
    private Integer id;

    @NotBlank(message = "name can not be null")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,10}$", message = "name is not correct")
    private String name;

    @NotNull(message = "deptId can not be null")
    @Min(value = 1, message = "deptId can not less than 1")
    private Integer deptId;

    @NotNull(message = "location can not be null")
    private String location;
}
