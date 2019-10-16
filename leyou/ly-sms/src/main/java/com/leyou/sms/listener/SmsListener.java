package com.leyou.sms.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.RegexUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SmsListener {

    @Autowired
    private SmsHelper smsHelper;
    @Autowired
    private SmsProperties prop;
    /**
     * 监听 发送短信的 消息
     * @param map
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE,durable = "true"),
            exchange = @Exchange(value = MQConstants.Exchange.SMS_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = {MQConstants.RoutingKey.VERIFY_CODE_KEY}
    ))
    public void sendSms(Map<String,String> map){
        //当前的map{"phone":"128982323","code":"2342344"}
        String phone = map.remove("phone");
        if(!RegexUtils.isPhone(phone)){
            log.error("电话号码有误！！");
            return;
        }
        //remove 后{""code":"2342344"}
        //把map转json
        String param = JsonUtils.toString(map);
        smsHelper.sendMessage(phone,prop.getSignName(),prop.getVerifyCodeTemplate(),param);
    }
}
