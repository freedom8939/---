package com.example.hospital.api.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.example.hospital.api.common.R;
import com.example.hospital.api.controller.form.LoginForm;
import com.example.hospital.api.service.MisUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/mis_user")
public class MisUserController {
    @Resource
    private MisUserService misUserService;

    @PostMapping("/login")
    @ApiOperation("登录")
    public R login(@RequestBody @Valid LoginForm form) {
        //转成map 因为要给login传递map
        Map param = BeanUtil.beanToMap(form);
        Integer userId = misUserService.login(param);
        if (userId != null) {
            StpUtil.login(userId);
            String token = StpUtil.getTokenValue();
            List<String> permissions = StpUtil.getPermissionList();
            return R.ok().put("result", true).put("token", token)
                    .put("permissions", permissions);

        }
        return R.ok().put("result", false);

    }

    @ApiOperation("退出")
    @GetMapping("/logout")
    @SaCheckLogin
    //登录才可以退出
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }
}

