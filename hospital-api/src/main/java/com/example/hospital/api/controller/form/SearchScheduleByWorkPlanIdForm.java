package com.example.hospital.api.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchScheduleByWorkPlanIdForm {
    @NotNull(message = "workPlanId can not be null")
    @Min(value = 1, message = "workPlanId can not less than 1")
    private Integer workPlanId;
}
