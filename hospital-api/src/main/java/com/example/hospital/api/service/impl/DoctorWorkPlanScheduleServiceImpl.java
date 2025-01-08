package com.example.hospital.api.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.BiMap;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.api.controller.form.vo.DoctorScheduleSlotVO;
import com.example.hospital.api.db.dao.DoctorWorkPlanDao;
import com.example.hospital.api.db.dao.DoctorWorkPlanScheduleDao;
import com.example.hospital.api.db.pojo.DoctorWorkPlanScheduleEntity;
import com.example.hospital.api.exception.HospitalException;
import com.example.hospital.api.service.DoctorWorkPlanScheduleService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class DoctorWorkPlanScheduleServiceImpl implements DoctorWorkPlanScheduleService {
    @Resource
    private DoctorWorkPlanScheduleDao doctorWorkPlanScheduleDao;

    @Resource
    private DoctorWorkPlanDao doctorWorkPlanDao;
    @Resource
    private RedisTemplate redisTemplate;

    //Map provided by hutool library that can be searched in both directions
    private BiMap<String, Integer> range = new BiMap<>(new HashMap() {{
        put("08:00", 1);
        put("08:30", 2);
        put("09:00", 3);
        put("09:30", 4);
        put("10:00", 5);
        put("10:30", 6);
        put("11:00", 7);
        put("11:30", 8);
        put("13:00", 9);
        put("13:30", 10);
        put("14:00", 11);
        put("14:30", 12);
        put("15:00", 13);
        put("15:30", 14);
        put("16:00", 15);
    }});
    @Override
    public void insert(ArrayList<DoctorWorkPlanScheduleEntity> list) {
        insertScheduleHandle(list);
        //TODO Set up Redis cache to prevent overbooking of registrations
        this.addScheduleCache(list);
    }

    @Transactional
    public void insertScheduleHandle(ArrayList<DoctorWorkPlanScheduleEntity> list) {
        for (DoctorWorkPlanScheduleEntity entity : list) {
            doctorWorkPlanScheduleDao.insert(entity);
        }
    }
    // Encapsulate the process of creating a cache and provide reuse for other business methods of the current class
    private void addScheduleCache(ArrayList<DoctorWorkPlanScheduleEntity> list) {
        // If there are no elements in the list, there is no need to create a cache
        if (list == null || list.size() == 0) {
            return;
        }
        // The foreign key values of the time period records inserted into the schedule data table in a medical visit plan are the same, and the first element of the list is taken out to obtain the foreign key value.
        int workPlanId = list.get(0).getWorkPlanId();

        // Query database records
        ArrayList<HashMap> newList = doctorWorkPlanScheduleDao.searchNewSchedule(workPlanId);

        for (HashMap one : newList) {
            int id = MapUtil.getInt(one, "id");
            int slot = MapUtil.getInt(one, "slot");

            //Define the Key of the cache record
            String key = "doctor_schedule_" + id;

            // Cache the visiting time period to Redis
            redisTemplate.opsForHash().putAll(key, one);

            // Date of visit
            String date = MapUtil.getStr(one, "date");
            //Start time of this time period
            String time = range.getKey(slot);

            //Set cache expiration time
            redisTemplate.expireAt(key, DateUtil.parse(date + " " + time));
        }
    }

    @Override
    public ArrayList searchDeptSubSchedule(Map param) {
        ArrayList<HashMap> list = doctorWorkPlanScheduleDao.searchDeptSubSchedule(param);
        ArrayList<HashMap> result = new ArrayList();

        int tempDoctorId = 0;
        HashMap doctor = new HashMap();

        for (HashMap map : list) {
            int doctorId = MapUtil.getInt(map, "doctorId");
            int slot = MapUtil.getInt(map, "slot");
            // If the doctor of the current record and the previous record are not the same person
            if (tempDoctorId != doctorId) {
                tempDoctorId = doctorId;
                doctor = map;
                doctor.replace("slot", new ArrayList<Integer>() {{
                    add(slot);
                }});
                result.add(doctor);
            }
            // If the current record and the previous record are the same doctor
            else if (tempDoctorId == doctorId) {
                ArrayList<Integer> slotList = (ArrayList) doctor.get("slot");
                slotList.add(slot);
            }
        }
        // Filter out which time periods to provide medical consultation and which time periods to not provide medical consultation
        for (HashMap map : result) {
            ArrayList<Integer> slot = (ArrayList) map.get("slot");
            ArrayList tempSlot = new ArrayList();
            for (int i = 1; i <= 15; i++) {
                tempSlot.add(slot.contains(i));
            }
            map.replace("slot", tempSlot);
        }
        return result;
    }

    @Override
    public HashMap searchByWorkPlanId(int workPlanId) {
        ArrayList<HashMap> list = doctorWorkPlanScheduleDao.searchByWorkPlanId(workPlanId);

        HashMap result = new HashMap();
        ArrayList temp = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            HashMap map = list.get(i);
            int scheduleId = MapUtil.getInt(map, "scheduleId");
            int doctorId = MapUtil.getInt(map, "doctorId");
            int maximum = MapUtil.getInt(map, "maximum");
            int num = MapUtil.getInt(map, "num");
            int slot = MapUtil.getInt(map, "slot");

            if (i == 0) {
                result.put("doctorId", doctorId);
                result.put("maximum", maximum);
            }
            temp.add(new HashMap<>() {{
                put("scheduleId", scheduleId);
                put("slot", slot);
                put("num", num);
            }});
        }
        result.put("slots", temp);
        return result;
    }
    @Override
    public void updateSchedule(Map param) {
        //Delete or add new clinic time slots
        ArrayList<DoctorWorkPlanScheduleEntity> addList = this.insertOrDeleteScheduleHandle(param);
        //Cache the newly added visiting time slot to Redis
        this.addScheduleCache(addList);
    }

    @Transactional
    ArrayList<DoctorWorkPlanScheduleEntity> insertOrDeleteScheduleHandle(Map param) {
        //Update the maximum number of patients in the schedule
        doctorWorkPlanDao.updateMaximum(param);

        Integer workPlanId = MapUtil.getInt(param, "workPlanId");
        Integer maximum = MapUtil.getInt(param, "maximum");

        //List of time periods to be processed (add or delete)
        ArrayList<DoctorScheduleSlotVO> slots = (ArrayList<DoctorScheduleSlotVO>) param.get("slots");
        //List used to store added time periods
        ArrayList<DoctorWorkPlanScheduleEntity> addList = new ArrayList();
        //List used to store deleted time periods
        ArrayList<Integer> removeList = new ArrayList();

        //Analyze which time periods are added and which are deleted.
        slots.forEach(one -> {
            //Determine adding records
            if (one.getOperate().equals("insert")) {
                DoctorWorkPlanScheduleEntity entity = new DoctorWorkPlanScheduleEntity();
                entity.setWorkPlanId(workPlanId);
                entity.setMaximum(maximum);
                entity.setSlot(one.getSlot());
                addList.add(entity); //Save the time period to the add list
            } else {
                removeList.add(one.getScheduleId()); //Save the time period to the delete list
            }
        });

        //Determine whether the deleted list has elements
        if (removeList.size() > 0) {
            //Query the total number of registered people in these time periods to be deleted
            long sum = doctorWorkPlanScheduleDao.searchSumNumByIds(removeList);
            //If the total number of registered patients in these time periods to be deleted is greater than 0, it means that there are patients registered in some of these time periods, so it cannot be deleted and an exception will be thrown directly.
            if (sum > 0) {
                throw new HospitalException("Modification failed. This modification conflicts with the registered record.");
            }

            //If no patient is registered, these time period records will be deleted.
            doctorWorkPlanScheduleDao.deleteByIds(removeList);

            //Delete the time period cache on Redis
            removeList.forEach(one -> {
                String key = "doctor_schedule_" + one;
                redisTemplate.delete(key);
            });
        }

        //Process adding time period list
        addList.forEach(one -> {
            //Add time period record
            doctorWorkPlanScheduleDao.insert(one);
        });

        return addList;
    }

    @Override
    @Transactional
    public void deleteByWorkPlanId(int workPlanId) {
        ArrayList<HashMap> list = doctorWorkPlanScheduleDao.searchByWorkPlanId(workPlanId);
        doctorWorkPlanScheduleDao.deleteByWorkPlanId(workPlanId);
        list.forEach(one -> {
            int scheduleId = MapUtil.getInt(one, "scheduleId");
            String key = "doctor_schedule_" + scheduleId;
            redisTemplate.delete(key);
        });
    }

}

