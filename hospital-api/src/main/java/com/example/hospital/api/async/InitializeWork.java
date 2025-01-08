package com.example.hospital.api.async;

import com.example.hospital.api.quartz.QuartzUtil;
import com.example.hospital.api.quartz.VideoDiagnoseJob;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InitializeWork {
    @Resource
    private QuartzUtil quartzUtil;

    @Async("AsyncTaskExecutor")
    public void init() {
        String cron = "*/3 * * * * ?";
        JobDetail jobDetail = JobBuilder.newJob(VideoDiagnoseJob.class).build();
        try {
            quartzUtil.addJob(jobDetail,"视频问诊定时器","任务组",
                    cron);
            //TODO 关闭装填不正确的视频问诊挂号单
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
