package com.example.hospital.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import com.example.hospital.api.common.R;
import com.example.hospital.api.controller.form.DeleteWorkPlanForm;
import com.example.hospital.api.controller.form.InsertWorkPlanForm;
import com.example.hospital.api.controller.form.SearchWorkPlanInRangeForm;
import com.example.hospital.api.service.MedicalDeptSubWorkPlanService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/medical/dept/sub/work_plan")
public class MedicalDeptSubWorkPlanController {
    @Resource
    private MedicalDeptSubWorkPlanService workPlanService;

    @PostMapping("/searchWorkPlanInRange")
    @SaCheckLogin
    @SaCheckPermission(value = {"ROOT", "SCHEDULE:SELECT"}, mode = SaMode.OR)
    public R searchWorkPlanInRange(@RequestBody @Valid SearchWorkPlanInRangeForm form) {
        Map param = BeanUtil.beanToMap(form);
        // Generate consecutive dates based on the start and end dates
        DateRange range = DateUtil.range(new DateTime(form.getStartDate()), new DateTime(form.getEndDate()), DateField.DAY_OF_MONTH);
        ArrayList dateList = new ArrayList();
        //Save consecutive dates into the collection
        range.forEach(one -> {
            dateList.add(one.toDateStr());
        });
        JSONArray array = workPlanService.searchWorkPlanInRange(param, dateList);

        dateList.clear();
        range.reset();
        //Put the formatted date into dateList, and the front end is used as header output
        range.forEach(one -> {
            dateList.add(one.toString("MM/dd") + "（" + one.dayOfWeekEnum().toChinese() + "）");
        });
        return R.ok().put("result", array).put("dateList", dateList);
    }

    @PostMapping("/insert")
    @SaCheckLogin
    @SaCheckPermission(value = {"ROOT", "SCHEDULE:INSERT"}, mode = SaMode.OR)
    public R insert(@RequestBody @Valid InsertWorkPlanForm form) {
        Map param = BeanUtil.beanToMap(form);
        String result = workPlanService.insert(param);
        return R.ok().put("result", result);
    }

    @PostMapping("/deleteWorkPlan")
    @SaCheckLogin
    @SaCheckPermission(value = {"ROOT", "SCHEDULE:DELETE"}, mode = SaMode.OR)
    public R deleteWorkPlan(@RequestBody @Valid DeleteWorkPlanForm form) {
        workPlanService.deleteWorkPlan(form.getWorkPlanId());
        return R.ok();
    }
}
