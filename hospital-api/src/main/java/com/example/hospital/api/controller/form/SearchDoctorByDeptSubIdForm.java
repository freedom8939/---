package com.example.hospital.api.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchDoctorByDeptSubIdForm {
    @NotNull(message = "deptSubId is not null")
    @Min(value = 1, message = "deptSubId less than 1")
    private Integer deptSubId;
}
