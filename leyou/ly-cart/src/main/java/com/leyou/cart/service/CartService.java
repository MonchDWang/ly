package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.JsonUtils;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 保存购物车信息
     * 保存到 redis中
     * @param cart
     */
    private String PRE_REDIS= "ly:cart:uid:";

    public void save(Cart cart) {
//        1、获取用户的id
        Long userId = UserHolder.getUser();
//        2、从redis中获取用户的购物车信息
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(PRE_REDIS + userId);
//         3、获取购物车中的商品信息
        String hashKey = String.valueOf(cart.getSkuId());
        Boolean b = boundHashOps.hasKey(hashKey);
        Integer num = cart.getNum();
        if(b){
            String cartJson = boundHashOps.get(hashKey);
            Cart cacheCart = JsonUtils.toBean(cartJson, Cart.class);
            cacheCart.setNum(cacheCart.getNum()+num);
            boundHashOps.put(hashKey,JsonUtils.toString(cacheCart));
        }else{
            boundHashOps.put(hashKey,JsonUtils.toString(cart));
        }
//        boundHashOps.put(hashKey,JsonUtils.toString(cart));
    }

    /**
     * 获取购物车
     * @return
     */
    public List<Cart> getCart() {
//        1、获取userid
        Long userId = UserHolder.getUser();
//        2、构造redis的key
        String redisKey = PRE_REDIS + userId;
        Boolean b = redisTemplate.hasKey(redisKey);
        if(b == null || !b){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(redisKey);
        List<String> cartJsonList = boundHashOps.values();
        if(CollectionUtils.isEmpty(cartJsonList)){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<Cart> cartList = cartJsonList.stream().map(json -> {
            return JsonUtils.toBean(json, Cart.class);
        }).collect(Collectors.toList());
        return cartList;
    }

    /**
     * 修改数量
     * @param skuId
     * @param num
     */
    public void updateNum(Long skuId, Integer num) {
//        1、获取用户id
        Long userId = UserHolder.getUser();
        String redisKey = PRE_REDIS + userId;
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(redisKey);
        Boolean b = boundHashOps.hasKey(skuId.toString());
        if(b == null || !b){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        String cartJson = boundHashOps.get(skuId.toString());
        Cart cart = JsonUtils.toBean(cartJson, Cart.class);
        cart.setNum(num);
        boundHashOps.put(skuId.toString(),JsonUtils.toString(cart));
    }

    /***
     * 删除购物车
     * @param skuId
     */
    public void deleteCart(Long skuId) {
        Long userId = UserHolder.getUser();
        String redisKey = PRE_REDIS + userId;
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(redisKey);
        String hashKy = skuId.toString();
        Boolean b = boundHashOps.hasKey(hashKy);
        if(b == null || !b){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        boundHashOps.delete(hashKy);
    }

    /**
     * 合并购物车
     * @param cartList
     */
    public void addCartList(List<Cart> cartList) {
        String redisKey = PRE_REDIS + UserHolder.getUser();
        Boolean b = redisTemplate.hasKey(redisKey);
        if(b==null || !b){
            for (Cart cart : cartList) {
                //客户端发送的cart
                Long skuId = cart.getSkuId();
                String hashKey = skuId.toString();
                redisTemplate.opsForHash().put(redisKey,hashKey,JsonUtils.toString(cart));
            }
        }else{
            BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(redisKey);
            for (Cart cart : cartList) {
                //客户端发送的cart
                Long skuId = cart.getSkuId();
                String hashKey = skuId.toString();
                Boolean bHash = boundHashOps.hasKey(hashKey);
                if(bHash != null && bHash){
                    Integer num = cart.getNum();
                    String cartJson = boundHashOps.get(hashKey);
                    cart = JsonUtils.toBean(cartJson, Cart.class);
                    cart.setNum(  cart.getNum() + num );
                }
                boundHashOps.put(hashKey,JsonUtils.toString(cart));
            }
        }
    }
}
