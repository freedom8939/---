package com.example.hospital.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.example.hospital.api.common.R;
import com.example.hospital.api.config.tencent.TrtcUtil;
import com.example.hospital.api.controller.form.UpdateCanRegisterForm;
import com.example.hospital.api.controller.form.searchImageByVideoDiagnoseId;
import com.example.hospital.api.controller.form.vo.SearchOnlineDectorListForm;
import com.example.hospital.api.service.VideoDiagnoseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Shiqi
 */
@RestController
@RequestMapping("/video_diagnose")
@Slf4j
public class VideoDiagnoseController {
    @Value("${tencent.trtc.appId}")
    private int appId;

    @Resource
    private TrtcUtil trtcUtil;

    @GetMapping("/searchMyUserSig")
    @ApiOperation("数字签名字符串")
    @SaCheckLogin
    public R searchMyUserSig() {
        int userId = StpUtil.getLoginIdAsInt();
        String userSig = trtcUtil.genUserSig(userId + "");
        return R.ok().put("userSig", userSig).put("userId", userId).put("appId", appId);
    }

    @Resource
    private VideoDiagnoseService videoDiagnoseService;

    @PostMapping("searchOnlineDoctorList")
    @ApiOperation("搜索在线医生列表")
    @SaCheckLogin
    public R searchOnlineDoctorList(@RequestBody @Valid SearchOnlineDectorListForm form) {
        ArrayList<HashMap> list = videoDiagnoseService.searchOnlineDoctorList(form.getSubName(), form.getJob());
        return R.ok().put("result", list);
    }


    @GetMapping("/online")
    @SaCheckLogin
    @ApiOperation("医生上线")
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"}, mode = SaMode.OR)
    public R online() {
        int userId = StpUtil.getLoginIdAsInt();
        videoDiagnoseService.online(userId);
        return R.ok();
    }

    @GetMapping("/offline")
    @SaCheckLogin
    @ApiOperation("医生下线")
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"}, mode = SaMode.OR)
    public R offline() {
        int userId = StpUtil.getLoginIdAsInt();
        boolean bool = videoDiagnoseService.offline(userId);
        return R.ok().put("result", bool);
    }

    @PostMapping("/updateOpenFlag")
    @SaCheckLogin
    @ApiOperation("医生开放挂号")
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"}, mode = SaMode.OR)
    public R updateOpenFlag(@RequestBody @Valid UpdateCanRegisterForm form) {
        int userId = StpUtil.getLoginIdAsInt();
        videoDiagnoseService.updateOpenFlag(userId, form.getOpen(),form.getRoomId());
        return R.ok();
    }

    @GetMapping("/refreshInfo")
    @SaCheckLogin
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"},mode = SaMode.OR)
    public R refreshInfo() {
        int userId = StpUtil.getLoginIdAsInt();
        HashMap map = videoDiagnoseService.refreshInfo(userId);
        return R.ok().put("result",map);
    }

    @GetMapping("/searchVideoDiagnoseInfo")
    @SaCheckLogin
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"},mode = SaMode.OR)
    public R searchVideoDiagnoseInfo(String currentOrder) {
        int userId = StpUtil.getLoginIdAsInt();  //用户id
        HashMap map = videoDiagnoseService.searchVideoDiagnoseInfo(userId);  //搜索 视频诊断信息
        return R.ok().put("result",map);
    }

    @PostMapping("/searchImageByVideoDiagnoseId")
    @SaCheckLogin
    @SaCheckPermission(value = {"VIDEO_DIAGNOSE:DIAGNOSE"}, mode = SaMode.OR)
    public R searchImageByVideoDiagnoseId(@RequestBody @Valid searchImageByVideoDiagnoseId form) {
        ArrayList<HashMap> list = videoDiagnoseService.searchImageByVideoDiagnosedId(form.getVideoDiagnoseId());
        return R.ok().put("result",list);
    }


}

