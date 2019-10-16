package com.leyou.search.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private GoodsRepository repository;
    /**
     * 上架
     * 创建索引
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SEARCH_ITEM_UP,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key= {MQConstants.RoutingKey.ITEM_UP_KEY}
    ))
    public void itemUp(Long spuId){
        log.info("search item up 收到消息=={}",spuId);
        SpuDTO spuDTO = itemClient.findSpuBySpuId(spuId);
        Goods goods = searchService.createGoods(spuDTO);
        repository.save(goods);
    }

    /**
     * 下架删除索引
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SEARCH_ITEM_DOWN,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key= {MQConstants.RoutingKey.ITEM_DOWN_KEY}
    ))
    public void itemDown(Long spuId){
        log.info("search item Down 收到消息=={}",spuId);
        repository.deleteById(spuId);
    }
}
