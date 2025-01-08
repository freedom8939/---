package com.example.hospital.api.service.impl;



import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.hospital.api.db.dao.DoctorWorkPlanDao;
import com.example.hospital.api.db.pojo.DoctorWorkPlanEntity;
import com.example.hospital.api.db.pojo.DoctorWorkPlanScheduleEntity;
import com.example.hospital.api.exception.HospitalException;
import com.example.hospital.api.service.DoctorWorkPlanScheduleService;
import com.example.hospital.api.service.MedicalDeptSubWorkPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MedicalDeptSubWorkPlanServiceImpl implements MedicalDeptSubWorkPlanService {
    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;

    @Override
    public JSONArray searchWorkPlanInRange(Map param, ArrayList dateList) {
        ArrayList<HashMap> list = doctorWorkPlanDao.searchWorkPlanInRange(param);
        Integer tempSubId = null;
        String tempDate = null;
        HashMap tempResult = new HashMap();
        for (HashMap one : list) {
            String deptName = MapUtil.getStr(one, "deptName");
            int deptSubId = MapUtil.getInt(one, "deptSubId");
            String deptSubName = MapUtil.getStr(one, "deptSubName");
            String doctorName = MapUtil.getStr(one, "doctorName");
            int workPlanId = MapUtil.getInt(one, "workPlanId");
            String date = MapUtil.getStr(one, "date");

            // Determine whether it is the first record
            if (tempSubId == null) {
                tempSubId = deptSubId;
                tempDate = date;
                // Treat the first record as a clinic
                HashMap temp = new HashMap() {{
                    put("deptName", deptName);
                    put("deptSubId", deptSubId);
                    put("deptSubName", deptSubName);
                    /*
                     * The clinic visit plan
                     * In order to ensure that the order of addition is not disrupted, LinkedHashMap must be used, not HashMap
                     */
                    put("plan", new LinkedHashMap<>() {{
                        put(date, new ArrayList<>() {{
                            add(doctorName);
                        }});
                    }});
                }};
                tempResult.put(deptSubId, temp);
            }
            // Not the first record, but this record is in the same clinic as the previous record and is on the same day.
            else if (tempSubId == deptSubId && tempDate.equals(date)) {
                // Take out the clinic
                HashMap map = (HashMap) tempResult.get(deptSubId);
                //Retrieve the house call plan from the clinic
                LinkedHashMap plan = (LinkedHashMap) map.get("plan");
                // Find the list of doctors visiting on that day
                ArrayList doctors = (ArrayList) plan.get(date);
                //Add the doctor's name to the list
                doctors.add(doctorName);
            }
            // Not the first record, but this record is in the same clinic as the previous record, but not on the same day.
            else if (tempSubId == deptSubId && !tempDate.equals(date)) {
                tempDate = date; // Updated
                // Take out the clinic
                HashMap map = (HashMap) tempResult.get(deptSubId);
                //Retrieve the house call plan from the clinic
                LinkedHashMap plan = (LinkedHashMap) map.get("plan");
                //Create a new visit date list and add the doctor's name
                plan.put(date, new ArrayList<>() {{
                    add(doctorName);
                }});
            }
            // If this record is not in the same clinic as the previous record
            else if (tempSubId != deptSubId) {
                tempSubId = deptSubId;
                tempDate = date;
                //Create a new clinic object
                HashMap temp = new HashMap() {{
                    put("deptName", deptName);
                    put("deptSubId", deptSubId);
                    put("deptSubName", deptSubName);
                    //出诊计划
                    put("plan", new LinkedHashMap<>() {{
                        //添加出诊列表
                        put(date, new ArrayList<>() {{
                            add(doctorName);
                        }});
                    }});
                }};
                //Add the new clinic object to the result set
                tempResult.put(deptSubId, temp);
            }
        }

        // In order to loop through the elements in the HashMap, extract all elements
        Set<Map.Entry> set = tempResult.entrySet();

        // Loop through each element
        set.forEach(one -> {
            // Clinic object
            HashMap map = (HashMap) one.getValue();
            // The clinic visit plan
            LinkedHashMap plan = (LinkedHashMap) map.get("plan");
            /*
             * The second parameter of the business method is to extract each date and determine whether the date is included in the visit plan.
             * If there is no such date in the medical visit plan, it means that there will be no doctor visiting on another day
             */
            dateList.forEach(date -> {
                if (!plan.containsKey(date)) {
                    // If there is no doctor visiting on a certain day, add an empty list to the visiting plan.
                    plan.put(date, new ArrayList<>());
                }
            });

            // Due to the new elements added to LinkedHashMap (empty visit list), all elements need to be sorted
            TreeMap sort = MapUtil.sort(plan, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    String key1 = (String) o1;
                    String key2 = (String) o2;
                    boolean bool = new DateTime(key1).isAfter(new DateTime(key2));
                    return bool ? 1 : -1;
                }
            });
            //Update the sorted visit plan to the clinic object
            map.replace("plan", sort);
        });

        //The plan of each clinic is a TreeMap. We need to convert it into a list form before it can be turned into a JSON array in the future.
        Collection<HashMap> values = tempResult.values();
        values.forEach(one -> {
            TreeMap plan = (TreeMap) one.get("plan");
            // Take out each element of TreeMap
            Set<Map.Entry> tempSet = plan.entrySet();
            ArrayList temp = new ArrayList();
            //Save the visit plan to the list
            tempSet.forEach(entry -> {
                temp.add(new HashMap<>() {{
                    put("date", entry.getKey());
                    put("doctors", entry.getValue());
                }});
            });
            //Update the plan of the clinic object
            one.replace("plan", temp);
        });
        return JSONUtil.parseArray(values);
    }

    @Resource
    private DoctorWorkPlanScheduleService doctorWorkPlanScheduleService;

    @Override
    public String insert(Map param) {
        //Check whether the doctor has a visit record on that day
        Integer id = doctorWorkPlanDao.searchId(param);
        if (id != null) {
            return "The visit plan already exists and cannot be added again.";
        }

        DoctorWorkPlanEntity entity_1 = BeanUtil.toBean(param, DoctorWorkPlanEntity.class);
        int totalMaximum=MapUtil.getInt(param,"totalMaximum");
        entity_1.setMaximum(totalMaximum);
        //Save visit plan
        doctorWorkPlanDao.insert(entity_1);

        //Query the primary key value of the visit plan
        id = doctorWorkPlanDao.searchId(param);

        Integer[] slots = (Integer[]) param.get("slots");
        ArrayList<DoctorWorkPlanScheduleEntity> list = new ArrayList<>();
        int slotMaximum=MapUtil.getInt(param,"slotMaximum");
        for (Integer slot : slots) {
            DoctorWorkPlanScheduleEntity entity_2 = BeanUtil.toBean(param, DoctorWorkPlanScheduleEntity.class);
            entity_2.setWorkPlanId(id);
            entity_2.setSlot(slot);
            entity_2.setMaximum(slotMaximum);
            list.add(entity_2);
        }
        doctorWorkPlanScheduleService.insert(list);
        return "Query the primary key value of the visit plan";
    }

    @Override
    @Transactional
    public void deleteWorkPlan(int workPlanId) {
        //Check the number of people registered in the clinic plan
        Integer num =doctorWorkPlanDao.searchNumById(workPlanId);
        if (num > 0) {
            throw new HospitalException("There are already patients registered for this visit plan and deletion is prohibited.");
        }
        doctorWorkPlanDao.deleteById(workPlanId);
        doctorWorkPlanScheduleService.deleteByWorkPlanId(workPlanId);
    }
}
