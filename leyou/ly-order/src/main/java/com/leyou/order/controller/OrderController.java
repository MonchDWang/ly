package com.leyou.order.controller;

import com.leyou.order.DTO.OrderDTO;
import com.leyou.order.VO.OrderVO;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return 订单id
     */
    @PostMapping
    public ResponseEntity<Long> saveOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(orderDTO));
    }

    /**
     * 根据id 查询订单信息
     * @param orderId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderVO> findOrderById(@PathVariable(name = "id")Long orderId){
        return ResponseEntity.ok(orderService.findOrder(orderId));
    }

    /**
     * 获取 统一下单返回的支付url
     * 前端接收或 生成二维码
     * @param orderId
     * @return
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> getUrl(@PathVariable(name = "id")Long orderId){
        return ResponseEntity.ok(orderService.getCodeUrl(orderId));
    }

    /**
     * 根据orderid 查询订单状态
     * @param orderId
     * @return
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> findState(@PathVariable(name = "id")Long orderId){
        return ResponseEntity.ok(orderService.findState(orderId));
    }
}
