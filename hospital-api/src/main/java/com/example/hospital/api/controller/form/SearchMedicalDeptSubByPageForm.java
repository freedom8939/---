package com.example.hospital.api.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchMedicalDeptSubByPageForm {
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{1,50}$", message = "name is not correct")
    private String name;

    @Min(value = 1, message = "deptId can not less than 1")
    private Integer deptId;

    @Pattern(regexp = "^ASC$|^DESC$", message = "order is not correct")
    private String order;

    @NotNull(message = "page can not be nul;")
    @Min(value = 1, message = "page can not less than 1")
    private Integer page;

    @NotNull(message = "length can not be null")
    @Range(min = 10, max = 50, message = "length is not correct")
    private Integer length;
}
