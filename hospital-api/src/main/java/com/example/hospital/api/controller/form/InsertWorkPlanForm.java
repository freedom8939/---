package com.example.hospital.api.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;

@Data
public class InsertWorkPlanForm {
    @NotNull(message = "deptSubId can not be null")
    @Min(value = 1, message = "deptSubId can not less than 1")
    private Integer deptSubId;

    @NotNull(message = "doctorId can not be null")
    @Min(value = 1, message = "doctorId can not less than 1")
    private Integer doctorId;

    @NotBlank(message = "date can not be null")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-))$",
            message = "date is not correct")
    private String date;

    @NotNull(message = "totalMaximum can not be null")
    @Range(min = 1, max = 150, message = "totalMaximum is not correct")
    private Integer totalMaximum;

    @NotNull(message = "slotMaximum can not be null")
    @Range(min = 1, max = 10, message = "slotMaximum is not correct")
    private Integer slotMaximum;

    @NotEmpty(message = "slots can not be null")
    private Integer[] slots;
}
