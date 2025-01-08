package com.example.hospital.api.config;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.map.MapUtil;
import com.example.hospital.api.db.dao.MisUserDao;
import com.example.hospital.api.db.dao.VideoDiagnoseDao;
import com.example.hospital.api.socket.WebSocketService;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.trtc.v20190722.TrtcClient;
import com.tencentcloudapi.trtc.v20190722.models.DismissRoomByStrRoomIdRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KeyExpiredListener extends KeyExpirationEventMessageListener {

    //前两个是腾讯云的secretyId和secretKey 销毁用的
    @Value("${tencent.cloud.secretId}")
    private String cloudSecretId;

    @Value("${tencent.cloud.secretKey}")
    private String cloudSecretKey;

    @Value("${tencent.trtc.appId}")
    private String trtcAppId;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private MisUserDao misUserDao;

    @Resource
    private VideoDiagnoseDao videoDiagnoseDao;

    public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {

        String key = message.toString();
        if (key.startsWith("video_diagnose_")) {

            String temp = key.substring(key.lastIndexOf("_") + 1);
            int currentOrder = Integer.parseInt(temp.split("#")[0]);
            int doctorId = Integer.parseInt(temp.split("#")[1]);

       /*
//            int userId = misUserDao.searchUserId(new HashMap() {{
//                put("refId", doctorId);
//                put("job", "医生");
//            }});
            int userId = 1;*/
            int userId = 1;
            String onlineKey = "online_doctor_" + doctorId;

            if (key.startsWith("video_diagnose_start_")) {
                redisTemplate.opsForHash().put(onlineKey, "currentStatus", 2);
                String roomId = redisTemplate.opsForHash().get(onlineKey, "roomId").toString();
                //更新数据库记录
                HashMap param = new HashMap() {{
                    put("id", currentOrder);
                    put("status", 2);
                    put("realStart", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                }};
                videoDiagnoseDao.updateStatus(param);
                WebSocketService.sendInfo("StartDiagnose" + "#" +
                        roomId + "&" + currentOrder, userId + "");
            } else if (key.startsWith("video_diagnose_end_")) {
                //问诊结束缓存
                redisTemplate.opsForHash().put(onlineKey, "currentStatus", 3);
                String roomId = redisTemplate.opsForHash().get(onlineKey, "roomId").toString();

                //更新数据库记录
                HashMap param = new HashMap() {{
                    put("id", currentOrder);
                    put("status", 3);
                    put("realEnd", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
                }};
                videoDiagnoseDao.updateStatus(param);

                //看是否有排队的
                Map entries = redisTemplate.opsForHash().entries(onlineKey);
                String nextPatient = MapUtil.getStr(entries, "nextPatient");
                String nextOrder = MapUtil.getStr(entries, "nextOrder");
                String nextStart = MapUtil.getStr(entries, "nextStart");
                String nextEnd = MapUtil.getStr(entries, "nextEnd");
                boolean nextPayment = MapUtil.getBool(entries, "nextPayment");

                if (!"none".equals(nextOrder)) {
//把等候的问诊换成当前问诊
                    entries.replace("currentPatient", nextPatient);
                    entries.replace("currentOrder", nextOrder);
                    entries.replace("currentHandle", false);
                    entries.replace("currentStart", nextStart);
                    entries.replace("currentEnd", nextEnd);
                    entries.replace("currentPayment", nextPayment);
                    entries.replace("currentNotify", false);
                    entries.replace("currentStatus", 1);
//清理等侯问诊的缓存
                    entries.replace("nextPatient", "none");
                    entries.replace("nextOrder", "none");
                    entries.replace("nextStart", null);
                    entries.replace("nextEnd", null);
                    entries.replace("nextPayment", false);
                    entries.replace("nextNotify", false);
                } else {
                    //不存在后续问诊
                    entries.replace("currentPatient", "none");
                    entries.replace("currentOrder", "none");
                    entries.replace("currentHandle", false);
                    entries.replace("currentStart", null);
                    entries.replace("currentEnd", null);
                    entries.replace("currentPayment", false);
                    entries.replace("currentNotify", false);
                    entries.replace("currentStatus", null);
                }
                //更新缓存数据
                redisTemplate.opsForHash().putAll(onlineKey, entries);
                //关闭trtc 告诉医生
                WebSocketService.sendInfo("EndDiagnose" + "#" + roomId, userId + "");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.error("thread sleep");
                }
                redisTemplate.opsForHash().put(key, "roomId", null);


            }
        } else if (key.startsWith("patient_video_diagnose_payment")) {
            //获取挂号单的id
            int id = Integer.parseInt(key.split("#")[1]);

            HashMap map = videoDiagnoseDao.searchPaymentStatus(id);
            int paymentStatus = MapUtil.getInt(map, "paymentStatus");
            int doctorId = MapUtil.getInt(map, "doctorId");
            key = "online_doctor_" + doctorId;
            if (paymentStatus != 2) {
                Map entries = redisTemplate.opsForHash().entries(key);
                //未付款关闭挂号单，清理医生上线缓存的数据
                if (id == MapUtil.getInt(entries, "nextOrder")) {
                    redisTemplate.opsForHash().putAll(key, new HashMap() {{
                        put("nextPatient", "none");
                        put("nextOrder", "none");
                        put("nextStart", "none");
                        put("nextEnd", "none");
                        put("nextPayment", false);
                        put("nextNotify", false);
                    }});
                }
                //TODO 用websocket通知视频问诊页面刷新
//                WebSocketService.sendInfo("RefreshDiagnose", 1 + "");
                videoDiagnoseDao.closePayment(new HashMap() {{
                    put("id", id);
                }});
            }
            WebSocketService.sendInfo("RefreshDiagnose", 1 + "");

  /*
            //获取挂单号id
//            int userId = misUserDao.searchUserId(new HashMap() {{
//                put("refId", 1);
//                put("job", "医生");
//            }});
//            int userId = misUserDao.searchUserId(null);

         String[] split = key.split("#");
            for (String s : split) {
                System.out.println(s);
            }
            int id = Integer.parseInt(key.split("#")[1]);

            HashMap map = videoDiagnoseDao.searchPaymentStatus(id);
            int paymentStatus = MapUtil.getInt(map, "paymentStatus");

            int doctorId = MapUtil.getInt(map, "doctorId");
            key = "online_doctor_" + doctorId;





            //如果没有付款成功 销毁患者问诊缓存
            //此时2
            if (paymentStatus != 1) {
                Map entries = redisTemplate.opsForHash().entries(key);
                if (id == MapUtil.getInt(entries, "nextOrder")) {
                    redisTemplate.opsForHash().putAll(key, new HashMap() {{
                        put("nextPatient", "none");
                        put("nextOrder", "none");
                        put("nextStart", "none");
                        put("nextEnd", "none");
                        put("nextPayment", false);
                        put("nextNotify", false);
                    }});
                }


                //关闭视频问诊订单
                videoDiagnoseDao.closePayment(new HashMap() {{
                    put("id", id);
                }});
            } else {
                int userId = misUserDao.searchUserId(new HashMap() {{
                    put("refId", doctorId);
                    put("job", "医生");
                }});
                WebSocketService.sendInfo("RefreshDiagnose", userId + "");

            }*/
        }
    }
}