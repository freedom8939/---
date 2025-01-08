package com.example.hospital.api.service;

import cn.hutool.json.JSONArray;
import com.example.hospital.api.common.PageUtils;
import com.example.hospital.api.db.pojo.MedicalDeptSubEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface MedicalDeptSubService {
    public PageUtils searchByPage(Map param);
    public void insert(MedicalDeptSubEntity entity);
    public HashMap searchById(int id);
    public void update(MedicalDeptSubEntity entity);
    public void deleteByIds(Integer[] ids);



}
