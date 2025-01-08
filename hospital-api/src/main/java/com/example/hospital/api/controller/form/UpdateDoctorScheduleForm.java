package com.example.hospital.api.controller.form;

import com.example.hospital.api.controller.form.vo.DoctorScheduleSlotVO;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
public class UpdateDoctorScheduleForm {
    @NotNull(message = "workPlanId can not be null")
    @Min(value = 1, message = "workPlanId is not correct")
    private Integer workPlanId;

    @NotNull(message = "maximum can not be null")
    @Range(min = 1, max = 150, message = "maximum is not correct")
    private Integer maximum;

    @Valid
    @NotEmpty(message = "slots can not be null")
    private ArrayList<DoctorScheduleSlotVO> slots;

}
