package com.example.hospital.api.controller.form.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class DoctorScheduleSlotVO {
    @Min(value = 1, message = "scheduleId can not lesst than1")
    private Integer scheduleId;

    @NotNull(message = "slot can not be null")
    @Range(min = 1, max = 15, message = "slot is not correct")
    private Integer slot;

    @NotNull(message = "maximum")
    @Range(min = 1, max = 10, message = "maximum is not correct")
    private Integer maximum;

    @NotNull(message = "operate can not be null")
    @Pattern(regexp = "^insert$|^delete$", message = "operate isn not correct")
    private String operate;
}
