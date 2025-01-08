package com.example.hospital.api.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.example.hospital.api.db.dao.MisUserDao;
import com.example.hospital.api.service.MisUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service
public class MisUserServiceImpl implements MisUserService {

    @Resource
    private MisUserDao misUserDao;

    @Override
    public Integer login(Map param) {
        String username = MapUtil.getStr(param, "username");
        String password = MapUtil.getStr(param, "password");
        MD5 md5 = MD5.create();
        String temp = md5.digestHex(username);  //给username创建md5加密
        //加盐
        String tempStart = StrUtil.subWithLength(temp, 0, 6);
        String tempEnd = StrUtil.subSuf(temp, temp.length() - 3);
        password = md5.digestHex(tempStart + password + tempEnd);
        param.replace("password", password);
        Integer userId = misUserDao.login(param);
        return userId;
    }
}

