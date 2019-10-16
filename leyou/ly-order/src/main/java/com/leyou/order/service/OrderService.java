package com.leyou.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.SkuDTO;
import com.leyou.order.DTO.CartDTO;
import com.leyou.order.DTO.OrderDTO;
import com.leyou.order.VO.OrderDetailVO;
import com.leyou.order.VO.OrderLogisticsVO;
import com.leyou.order.VO.OrderVO;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.entity.TbOrderDetail;
import com.leyou.order.entity.TbOrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.utils.PayHelper;
import com.leyou.user.DTO.AddressDTO;
import com.leyou.user.client.UserClient;
import org.apache.catalina.startup.UserConfig;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private TbOrderService tbOrderService;
    @Autowired
    private TbOrderDetailService orderDetailService;
    @Autowired
    private TbOrderLogisticsService logisticsService;
    /**
     * 保存订单
     * @param orderDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(OrderDTO orderDTO) {
//        获取用户
//        1、保存订单
//        1.1、生成订单号  全局唯一不重复 使用雪花算法
        long orderId = idWorker.nextId();
//        1.2、算总金额  num * price =totalfee 远程调用item
        List<CartDTO> carts = orderDTO.getCarts();
        List<Long> skuIdList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //key -skuid   value - num
        Map<Long, Integer> skuNumMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        List<SkuDTO> skuDTOList = itemClient.findSkuListByIds(skuIdList);
        long totalFee = 0;
//        for(CartDTO cartDTO:carts){
//            //购物车中的数量
//            int num = cartDTO.getNum().intValue();
//            Long skuId = cartDTO.getSkuId();
//            for(SkuDTO skuDTO: skuDTOList){
////                当前skuid  == 购物车中id
//                if(skuDTO.getId().longValue() == skuId.longValue()){
//                    long price = skuDTO.getPrice().longValue();
//                    //当前商品的总金额
//                    long skuTotalFee = price * num;
//                    totalFee += skuTotalFee;
//                }
//            }
//        }
        List<TbOrderDetail> orderDetailList = new ArrayList<>();
        for(SkuDTO skuDTO:skuDTOList){
            Long skuId = skuDTO.getId();
            long skuPrice = skuDTO.getPrice().longValue();
            int num = skuNumMap.get(skuId).intValue();
            totalFee += skuPrice * num;
            TbOrderDetail orderDetail = new TbOrderDetail();
            orderDetail.setSkuId(skuId);
            orderDetail.setImage(skuDTO.getImages());
            orderDetail.setOwnSpec(skuDTO.getOwnSpec());
            orderDetail.setTitle(skuDTO.getTitle());
            orderDetail.setNum(num);
            orderDetail.setPrice(skuPrice);
            orderDetail.setOrderId(orderId);
            orderDetailList.add(orderDetail);
        }
//        1.3、实付金额  totalfee - 优惠 + 运费
        long actualFee = totalFee;
        Long userId = UserHolder.getUser();
//        1.4、设置状态  未付款
        TbOrder tbOrder = new TbOrder();
        tbOrder.setStatus(OrderStatusEnum.INIT.value());
        tbOrder.setOrderId(orderId);
        tbOrder.setUserId(userId);
        tbOrder.setTotalFee(totalFee);
        tbOrder.setActualFee(actualFee);
        tbOrder.setPostFee(0L);
        tbOrder.setSourceType(2);
        tbOrder.setPaymentType(1);
        boolean bOrder = tbOrderService.save(tbOrder);
        if(!bOrder){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        2、保存detail  远程调用item 查询sku的信息
        boolean bOrderDetail = orderDetailService.saveBatch(orderDetailList);
        if(!bOrderDetail){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        3、保存物流   远程调用user  ，查询收货人信息
        AddressDTO addressDTO = userClient.findAddress(1L);
        if(addressDTO == null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        TbOrderLogistics tbOrderLogistics = BeanHelper.copyProperties(addressDTO, TbOrderLogistics.class);
        tbOrderLogistics.setOrderId(orderId);
        boolean bLogistic = logisticsService.save(tbOrderLogistics);
        if(!bLogistic){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//         4、减库存  远程调用item ，根据skuid，num 减掉库存
//         feign可以把远端的异常携带回来
//        传递map参数  key - skuId  value - num
        itemClient.minusSkuStock(skuNumMap);
        return orderId;
    }

    /**
     * 根据id查询 订单信息
     * @param orderId
     * @return
     */
    public OrderVO findOrder(Long orderId) {

        TbOrder tbOrder = tbOrderService.getById(orderId);
        if(tbOrder==null){
            throw  new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        Long orderUserId = tbOrder.getUserId();
        Long userId = UserHolder.getUser();
        if(userId.longValue() != orderUserId.longValue()){
            throw  new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //处理订单详情表
        QueryWrapper<TbOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbOrderDetail::getOrderId,orderId);
        List<TbOrderDetail> tbOrderDetailList = orderDetailService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbOrderDetailList)){
            throw  new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
//        处理订单物流表
        TbOrderLogistics tbOrderLogistics = logisticsService.getById(orderId);
        if(tbOrderLogistics==null){
            throw  new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        OrderVO orderVO = BeanHelper.copyProperties(tbOrder, OrderVO.class);
        orderVO.setDetailList(BeanHelper.copyWithCollection(tbOrderDetailList, OrderDetailVO.class));
        orderVO.setLogistics(BeanHelper.copyProperties(tbOrderLogistics, OrderLogisticsVO.class));
        return orderVO;
    }

    @Autowired
    private PayHelper payHelper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取统一下单url
     * @param orderId
     * @return
     */
    private String payUrlKey = "ly:pay:orderid:";

    public String getCodeUrl(Long orderId) {

        String cacheUrl = redisTemplate.opsForValue().get(payUrlKey+orderId);
        if(!StringUtils.isEmpty(cacheUrl)){
            return cacheUrl;
        }
        TbOrder tbOrder = tbOrderService.getById(orderId);
        if(tbOrder.getStatus().intValue() != OrderStatusEnum.INIT.value()){
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }
        //支付描述
        String desc = "乐优商城支付";
        //实付金额 测试，使用1分钱
        Long actualFee = 1L;//tbOrder.getActualFee();
        //统一下单url，用来生成二维码
        String codeUrl = payHelper.createOrder(orderId, actualFee, desc);
        //有2小时的 有效期,把codeurl放入 redis
        redisTemplate.opsForValue().set(payUrlKey+orderId,codeUrl,2, TimeUnit.HOURS);
        return codeUrl;
    }

    /**
     * 获取订单状态
     * @param orderId
     * @return
     */
    public Integer findState(Long orderId) {
        TbOrder tbOrder = tbOrderService.getById(orderId);
        if(tbOrder == null){
            throw  new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        return tbOrder.getStatus();
    }

    /**
     * 关闭过期订单 恢复库存
     * @param dateTime
     */
    @Transactional
    public void closeOverOrder(Date dateTime) {

//   根据时间查询订单
        //查询有多少条 过期的订单
        List<Long> orderIds = tbOrderService.getOverTimeOrderIds(dateTime);
        if(CollectionUtils.isEmpty(orderIds)){
            return ;
        }
//   修改订单的状态
        UpdateWrapper<TbOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbOrder::getStatus,OrderStatusEnum.INIT.value());
        updateWrapper.lambda().le(TbOrder::getCreateTime,dateTime);
        updateWrapper.lambda().set(TbOrder::getStatus,OrderStatusEnum.CLOSED.value());
        boolean update = tbOrderService.update(updateWrapper);
        if(!update){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//   找到商品的id和num
        QueryWrapper<TbOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(TbOrderDetail::getOrderId,orderIds);
        List<TbOrderDetail> tbOrderDetailList = orderDetailService.list(queryWrapper);
        //计算skuid 对应  数量
        Map<Long, Integer> skuMap = tbOrderDetailList.stream().collect(Collectors.groupingBy(TbOrderDetail::getSkuId, Collectors.summingInt(TbOrderDetail::getNum)));
//   恢复库存
        itemClient.plusStock(skuMap);


    }
}
