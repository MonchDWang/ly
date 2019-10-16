package com.leyou.item.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//@RefreshScope
@RestController
public class MsgController {


//    @Value("${ly.msg}")
//    private  String msg;
//
//    @GetMapping("/testbus")
//    public ResponseEntity<String> testBus(){
//        return ResponseEntity.ok(msg);
//    }
}
