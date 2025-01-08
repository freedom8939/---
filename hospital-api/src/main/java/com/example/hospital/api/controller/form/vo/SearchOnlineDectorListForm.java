package com.example.hospital.api.controller.form.vo;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class SearchOnlineDectorListForm {

    private String subName;

    @Pattern(regexp = "^主治医师$|^副主治医师$|^主任医师$|^副主任医师$", message = "job 不正确")
    private String job;
}
