package com.leyou.order.task;

import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CloseOrderTask {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private OrderService orderService;

    /**
     * 定时任务的频率，30分钟
     */
    private static final long TASK_INTERVAL = 180000;
    /**
     * 定时任务的锁自动释放时间。 \r\n
     * 一般只要大于各服务器的时钟飘移时长+任务执行时长即可。\r\n
     * 此处默认120秒。\r\n
     */
    private static final long TASK_LEASE_TIME = 120;

    private static final String LOCK_KEY = "close:order:task:lock";

    /**
     * 订单超时的期限，1小时
     */
    private static final int OVERDUE_SECONDS = 3600;
    /**
     * 定时任务 关闭订单
     */
    @Scheduled(fixedDelay = TASK_INTERVAL)
    public void closeOrder(){
//        创建rediisson的对象
        RLock lock = redissonClient.getLock(LOCK_KEY);
        //        1、获取分布式锁
        try{
            boolean tryLock = lock.tryLock(0, TASK_LEASE_TIME, TimeUnit.SECONDS);
            if(!tryLock){
                return ;
            }
            try{
//                计算查询订单的最后时间
                Date dateTime = DateTime.now().minusSeconds(OVERDUE_SECONDS).toDate();
//                关闭过期的订单，恢复库存
                orderService.closeOverOrder(dateTime);
            }finally {
                //释放锁
                lock.unlock();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
