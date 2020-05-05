package com.zjx.controller;

import com.zjx.service.MindMapService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时任务
 */
@Component
public class ScheduledTask {

    @Resource
    private MindMapService mindMapService;

    /**
     * 定时任务 0 50 23 ? * WED 表示每个星期日23点50触发
     * 每周日23点50分触发
     */
    @Scheduled(cron = "0 50 1 ? * MON")
    public void everyDayUpdate(){
        mindMapService.mapSystemRating();//系统评分
    }
}
