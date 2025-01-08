package com.example.hospital.api.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchMedicalDeptSubByIdForm {
    @NotNull(message = "id can not be null")
    @Min(value = 1, message = "id can not less than 1")
    private Integer id;
}
