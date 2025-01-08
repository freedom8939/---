package com.example.hospital.api.quartz;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.example.hospital.api.db.dao.MisUserDao;
import com.example.hospital.api.socket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class VideoDiagnoseJob extends QuartzJobBean {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MisUserDao misUserDao;


    //操作缓存  每隔几秒查看缓存
    //线上问诊打开摄像头，
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        //查找所有医生缓存
        Set<String> keys = redisTemplate.keys("online_doctor_*");


        keys.forEach(key -> {
            //取出doctorId
            int doctorId = Integer.parseInt(key.substring(key.lastIndexOf("_") + 1));
//            int userId = misUserDao.searchUserId(new HashMap() {{
//                put("refId", doctorId);
//                put("job", "医生");
//            }});
            int userId = 1;

            // 优化 优先拆线呢缓存

            Map<String, Object> entries = redisTemplate.opsForHash().entries(key);


            String currentOrder = MapUtil.getStr(entries, "currentOrder");
//            String currentPatient = MapUtil.getStr(entries, "currentPatient");
            String nextOrder = MapUtil.getStr(entries, "nextOrder");
            boolean currentHandle = MapUtil.getBool(entries, "currentHandle");
            boolean currentPayment = MapUtil.getBool(entries, "currentPayment");
            boolean nextPayment = MapUtil.getBool(entries, "nextPayment");
            String nextPatient = MapUtil.getStr(entries, "nextPatient");
            String nextStart = MapUtil.getStr(entries, "nextStart");
            String nextEnd = MapUtil.getStr(entries, "nextEnd");
            boolean currentNotify = MapUtil.getBool(entries, "currentNotify");
            boolean nextNotify = MapUtil.getBool(entries, "nextNotify");


            //如果没有当前问诊，就把排队已付款的问诊提到当前问诊
            if ("none".equals(currentOrder) && !"none".equals(nextOrder) && nextPayment) {
                entries.replace("currentPatient", nextPatient);
                entries.replace("currentOrder", nextOrder);
                entries.replace("currentHandle", false);   //定时器，创建了问诊的开始和结束 handle就是false
                entries.replace("currentStart", nextStart);
                entries.replace("currentEnd", nextEnd);
                entries.replace("currentPayment", nextPayment);
                entries.replace("currentNotify", false);  //给浏览器发推送消息
                entries.replace("currentStatus", 1);

                //清楚等候的人的缓存
                entries.replace("nextPatient", "none");
                entries.replace("nextOrder", "none");
                entries.replace("nextStart", null);
                entries.replace("nextEnd", null);
                entries.replace("nextPayment", false);
                entries.replace("nextNotify", false);
                redisTemplate.opsForHash().putAll(key, entries);
            }

            //患者已经付款 并且 没有创建 开始和结束的缓存
            if (!currentHandle && currentPayment) {
                //当前问诊开始和结束的时间
                String currentStart = MapUtil.getStr(entries, "currentStart");
                String currentEnd = MapUtil.getStr(entries, "currentEnd");
                DateTime startTime = new DateTime(currentStart);
                DateTime endTime = new DateTime(currentEnd);


                //创建视频问诊开始的缓存，如果缓存到期销毁，
                String startKey = "video_diagnose_start_" + currentOrder + "#" + doctorId;
                redisTemplate.opsForValue().set(startKey, currentStart);


                //设置过期时间
                redisTemplate.expireAt(startKey, startTime);


                //生成UUID的RoomId,然后保存到Redis缓存里面

                String randomNumber = RandomStringUtils.randomNumeric(6);
                //String roomId = IdUtil.simpleUUID().toUpperCase();
                redisTemplate.opsForHash().put(key, "roomId", userId);

                //创建视频问诊结束时间的缓存。如果该缓存到期销毁，就在回调函数中关闭视频会议室的
                String endKey = "video_diagnose_end_" + currentOrder + "#" + doctorId;
                redisTemplate.opsForValue().set(endKey, currentEnd);

                //设置过期时间
                redisTemplate.expireAt(endKey, endTime);
                redisTemplate.opsForHash().put(key, "currentHandle", true);
            }

            //如果当前问诊成功，并且未推送给医生端，就发出推送消息
            if (currentPayment && !currentNotify) {
                WebSocketService.sendInfo("RefreshDiagnose", userId + "");
                redisTemplate.opsForHash().put(key, "currentNotify", true);
            }

            //如果排队问诊付款成功，并且未推送给医生端，发送推送消息
            if (nextPayment && !nextNotify) {
                WebSocketService.sendInfo("RefreshDiagnose", userId + "");
                redisTemplate.opsForHash().put(key, "nextNotify", true);
            }

        });
    }
}
