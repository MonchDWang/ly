package com.leyou.sms.test;

import com.leyou.common.constants.MQConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSendSms {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendSms(){
        Map<String,String> map = new HashMap();
        map.put("phone","13136777752");
        map.put("code","123456");
        amqpTemplate.convertAndSend(MQConstants.Exchange.SMS_EXCHANGE_NAME,
                MQConstants.RoutingKey.VERIFY_CODE_KEY,
                map
                );
    }
}
