package com.leyou.order.controller;

import com.leyou.order.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private PayService payService;
    /**
     * 微信支付的回调方法
     * @param map
     * @return
     */
    @PostMapping(value = "/wx/notify",produces = "application/xml")
    public Map<String,String>  wxNotify(@RequestBody Map<String, String> map){

        //处理回调的内容
        payService.handleNotify(map);

        Map<String,String> returnMap = new HashMap<>();
        returnMap.put("return_code","SUCCESS");
        returnMap.put("return_msg","OK");
        return returnMap;

    }
}
