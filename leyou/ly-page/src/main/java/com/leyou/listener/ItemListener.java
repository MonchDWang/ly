package com.leyou.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品微服务的消息  消费者
 */
@Slf4j
@Component
public class ItemListener {

    @Autowired
    private PageService pageService;
    /**
     * 上架 消息的 消费者
     * 创建静态页面
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.PAGE_ITEM_UP,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = {MQConstants.RoutingKey.ITEM_UP_KEY}
    ))
    public void itemUp(Long spuId){
        log.info("itemUp接收到消息{}",spuId);
        pageService.createHtml(spuId);
    }

    /**
     * 下架的消费者
     * 删除静态页面
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.PAGE_ITEM_DOWN,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = {MQConstants.RoutingKey.ITEM_DOWN_KEY}
    ))
    public void itemDown(Long spuId){
        log.info("itemDown接收到消息{}",spuId);
        pageService.removePage(spuId);
    }
}
