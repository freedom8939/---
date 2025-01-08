package com.example.hospital.api.service.impl;

import cn.hutool.core.map.MapUtil;
import com.example.hospital.api.db.dao.DoctorDao;
import com.example.hospital.api.db.dao.MisUserDao;
import com.example.hospital.api.db.dao.VideoDiagnoseDao;
import com.example.hospital.api.db.dao.VideoDiagnoseFileDao;
import com.example.hospital.api.exception.HospitalException;
import com.example.hospital.api.service.VideoDiagnoseService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Shiqi
 */
@Service
public class VideoDiagnoseServiceImpl implements VideoDiagnoseService {
    @Resource
    private DoctorDao doctorDao;

    @Resource
    private MisUserDao misUserDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private VideoDiagnoseDao videoDiagnoseDao;

    @Override
    public void online(int userId) {
        int doctorId = this.searchDoctorId(userId);

        String key = "online_doctor_" + doctorId;

        //判断是否存在医生的上线缓存
        if (redisTemplate.hasKey(key)) {
            return;
        }

        //Query doctor details
        HashMap map = doctorDao.searchDataForOnlineCache(doctorId);
        System.out.println(map);
        //创建医生缓存
        redisTemplate.opsForHash().putAll(key, new HashMap<String, Object>() {{
            put("doctorId", MapUtil.getInt(map, "doctorId"));
            put("name", MapUtil.getStr(map, "name"));
            put("photo", MapUtil.getStr(map, "photo"));
            put("job", MapUtil.getStr(map, "job"));
            put("price", MapUtil.getInt(map, "price"));
            put("subName", MapUtil.getStr(map, "subName"));
            put("subId", MapUtil.getStr(map, "subId"));
            put("deptId", MapUtil.getStr(map, "deptId"));
            put("deptName", MapUtil.getStr(map, "deptName"));
            put("remark", MapUtil.getStr(map, "remark"));
            put("description", MapUtil.getStr(map, "description"));

            put("open", false);
            put("currentPatient", "none"); // 当前患者id
            put("currentOrder", "none");     // 当前问诊订单号
            put("currentHandle", false);     //
            put("currentStart", "none");     // 开始时间
            put("currentEnd", "none");       // 结束时间
            put("currentPayment", false);    // Has the current consultation been paid
            put("currentStatus", 1);         // 1: Not started, 2: In consultation, 3: Finished
            put("currentNotify", false);     // Not yet pushed to the doctor's end
            //等候的那些
            put("nextPatient", "none");      // Next waiting patient ID
            put("nextOrder", "none");        // Order number for the next waiting consultation
            put("nextStart", "none");        // 等待问诊的开始时间
            put("nextEnd", "none");          // 等待问诊的结束时间
            put("nextPayment", false);       // 排队问诊是否付款
            put("nextNotify", false);        // 未推送给医生端

        }});

    }

    private int searchDoctorId(int userId) {
        HashMap<String, Object> map = misUserDao.searchRefId(userId);
        String job = MapUtil.getStr(map, "job");
        System.out.println(map);
        if (!"医生".equals(job)) {
            throw new HospitalException("当前用户不是医生");
        }

        Integer refId = MapUtil.getInt(map, "refId");
        if (refId == null) {
            throw new HospitalException("当前用户没有关联医生表.");
        }
        return refId;
    }

    @Override
    public boolean offline(int userId) {
        // Find the primary key value of the doctor
        int doctorId = this.searchDoctorId(userId);
        String key = "online_doctor_" + doctorId;
        // Check if the online cache exists
        if (!redisTemplate.hasKey(key)) {
            return true;
        }

        // 检查是否有正在执行的问诊和等候的问诊
        List<String> list = redisTemplate.opsForHash().multiGet(key, new ArrayList<>() {{
            add("currentOrder");
            add("nextOrder");
        }});

        String currentOrder = list.get(0);
        String nextOrder = list.get(1);

        //存在当前问诊  不能下线！
        if (!"none".equals(currentOrder) || !"none".equals(nextOrder)) {
            return false;
        }

        // Delete the doctor's consultation cache
        redisTemplate.delete(key);
        return true;
    }

    @Override
    public void updateOpenFlag(int userId, boolean open, String roomId) {
        int doctorId = this.searchDoctorId(userId);
        String key = "online_doctor_" + doctorId;
        if (!redisTemplate.hasKey(key)) {
            return;
        }
        redisTemplate.opsForHash().put(key, "open", open);
        redisTemplate.opsForHash().put(key, "roomId", roomId);
    }

    @Resource
    private VideoDiagnoseFileDao videoDiagnoseFileDao;

    @Override
    public ArrayList<HashMap> searchImageByVideoDiagnosedId(int videoDiagnoseId) {
        ArrayList<HashMap> list = videoDiagnoseFileDao.searchImageByVideoDiagnoseId(videoDiagnoseId);
        return list;
    }

    @Override
    public ArrayList<HashMap> searchOnlineDoctorList(String subName, String job) {
        ArrayList<HashMap> list = new ArrayList<>();
        //查找缓存
        Set<String> keys = redisTemplate.keys("online_doctor_*");
        for (String key : keys) {
            Map entries = redisTemplate.opsForHash().entries(key);
            System.out.println(entries);
            String tempSubName = MapUtil.getStr(entries, "subName");
            String tempJob = MapUtil.getStr(entries, "job");
            Boolean open = MapUtil.getBool(entries, "open");
            if (!open) {
                continue;
            }
            if (subName != null && !subName.equals(tempSubName)) {
                continue;
            }
            //过滤
            if (job != null && !job.equals(tempJob)) {
                continue;
            }
            HashMap map = new HashMap<>() {{
                put("doctorId", MapUtil.getInt(entries, "doctorId"));
                put("name", MapUtil.getStr(entries, "name"));
                put("photo", MapUtil.getStr(entries, "photo"));
                put("job", MapUtil.getStr(entries, "job"));
                put("description", MapUtil.getStr(entries, "description"));
                put("remark", MapUtil.getStr(entries, "remark"));
                put("price", MapUtil.getStr(entries, "price"));
            }};
            list.add(map);
        }
        return list;
    }

    @Override
    public HashMap searchVideoDiagnoseInfo(int userId) {
        HashMap map = new HashMap();

        //根据用户id 查询对应的userid
        int doctorId = this.searchDoctorId(userId);

        String key = "online_doctor_" + doctorId;

        if (!redisTemplate.hasKey(key)) {
            return map;
        }
        Map entries = redisTemplate.opsForHash().entries(key);

        //查询表中的videoDiagnose信息的id
        String currentOrder = MapUtil.getStr(entries, "currentOrder");
        String nextOrder = MapUtil.getStr(entries, "nextOrder");

        Boolean currentPayment = MapUtil.getBool(entries, "currentPayment");
        Boolean nextPayment = MapUtil.getBool(entries, "nextPayment");
        //存在当前的视频问诊
        if (!"none".equals(currentOrder) && (currentPayment != null && currentPayment)) {
            int diagnoseId = Integer.parseInt(currentOrder);
            HashMap currentInfo = videoDiagnoseDao.searchVideoDiagnoseInfo(diagnoseId);
            map.put("currentInfo", currentInfo);
        }

        //候诊有数据
        if (!"none".equals(nextOrder) && (nextPayment != null && nextPayment)) {
            int diagnoseId = Integer.parseInt(nextOrder);
            HashMap nextInfo = videoDiagnoseDao.searchVideoDiagnoseInfo(diagnoseId);
            map.put("nextInfo", nextInfo);
        }
        return map;
    }


    //当前问诊的信息
    @Override
    public HashMap refreshInfo(int userId) {
        int doctorId = this.searchDoctorId(userId);

        String key = "online_doctor_" + doctorId;

        HashMap map = new HashMap();

        if (!redisTemplate.hasKey(key)) {
            map.put("status", "offline");
            return map;
        }

        Map entries = redisTemplate.opsForHash().entries(key);

        map.put("status", "online");
        map.put("open", MapUtil.getBool(entries, "open"));


        map.put("currentOrder", MapUtil.getStr(entries, "currentOrder"));
        Integer currentStatus = MapUtil.getInt(entries, "currentStatus");
        map.put("currentStatus", currentStatus);

        //定时程序roomid
        if (currentStatus != null && currentStatus == 2) {
            map.put("roomId", MapUtil.getStr(entries, "roomId"));
        }

        map.put("currentStart", MapUtil.getStr(entries, "currentStart"));
        map.put("currentEnd", MapUtil.getStr(entries, "currentEnd"));
        return map;
    }


}
