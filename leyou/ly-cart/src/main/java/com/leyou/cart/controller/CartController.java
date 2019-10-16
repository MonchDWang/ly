package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;
    /**
     * 添加购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody Cart cart){
        cartService.save(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 获取购物车
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> findCart(){
        return ResponseEntity.ok(cartService.getCart());
    }

    /**
     * 修改商品数量
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam(name = "id")Long skuId,
                                          @RequestParam(name = "num")Integer num){
        cartService.updateNum(skuId,num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 删除购物车
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable(name = "skuId") Long skuId){
            cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 合并登录后的购物车
     * @param cartList
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<Void> addCartList(@RequestBody List<Cart> cartList){
        cartService.addCartList(cartList);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
