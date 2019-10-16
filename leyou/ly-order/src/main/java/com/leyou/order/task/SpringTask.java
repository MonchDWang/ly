package com.leyou.order.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Slf4j
//@Component
public class SpringTask {

    int i= 0;

    /**
     * 定时任务的业务
     */
    @Scheduled(cron = "0 7 15 * * ?")
    public void test1(){
        log.info("[定时任务开始]-----------");
//        if(i==1){
//            try {
//
//                Thread.sleep(3000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        i++;
    }
}
