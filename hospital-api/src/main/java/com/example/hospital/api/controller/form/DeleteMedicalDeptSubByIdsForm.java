package com.example.hospital.api.controller.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DeleteMedicalDeptSubByIdsForm {
    @NotEmpty(message = "ids can not be null")
    private Integer[] ids;
}

