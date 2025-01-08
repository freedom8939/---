package com.example.hospital.api.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class QuartzUtil {

    @Resource
    private Scheduler scheduler;

    //创建定时器

    /**
     * @param jobDetail    任务类 执行的任务
     * @param jobName      定时器名字（删除用）
     * @param jobGroupName 定时器分组的名字（删除用）
     * @param cron         表达式
     */
    public void addJob(JobDetail jobDetail, String jobName, String jobGroupName,
                       String cron) {
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("定时器删除失败", e);
        }

    }


    //销毁定时器
    public void deleteJob(String jobName, String jobGroupName) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        try {
            scheduler.resumeTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
            log.debug("成功删除" + jobName + "定时器");
        } catch (Exception e) {
            log.error("定时器删除失败",e);
        }
    }
}
