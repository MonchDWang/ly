package com.leyou.order.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.enums.OrderStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Service
public class PayService {

    @Autowired
    private WXPay wxPay;
    @Autowired
    private TbOrderService orderService;
    /**
     * 处理回调的内容
     * @param reqMap
     */
    public void handleNotify(Map<String, String> reqMap) {
        if(reqMap.get("result_code") == null || !reqMap.get("result_code").equals(WXPayConstants.SUCCESS)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        try {
            boolean bWeiChat = wxPay.isPayResultNotifySignatureValid(reqMap);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        //获取系统该订单号
        String orderIdStr = reqMap.get("out_trade_no");
        Long orderId = Long.valueOf(orderIdStr);
        if(StringUtils.isEmpty(orderIdStr)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        String totalFeeStr = reqMap.get("total_fee");
        if(StringUtils.isEmpty(totalFeeStr)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        //获取订单信息
        TbOrder tbOrder = orderService.getById(orderId);
//        如果状态已经不是 初始状态了，就直接返回
        if(tbOrder.getStatus().intValue() != OrderStatusEnum.INIT.value()){
            return ;
        }
        Long actualFee = tbOrder.getActualFee();
        Long totalFee = Long.valueOf(totalFeeStr);
//        判断支付的金额和实付金额是否一致
//        if(actualFee.longValue() != totalFee.longValue()){
//            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
//        }
//        修改状态，保证幂等
//        update tb_order set status=2 where order_id=? and status=1
        UpdateWrapper<TbOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbOrder::getOrderId,orderId);
        updateWrapper.lambda().eq(TbOrder::getStatus,OrderStatusEnum.INIT.value());
        updateWrapper.lambda().set(TbOrder::getStatus,OrderStatusEnum.PAY_UP.value());
        boolean bupdate = orderService.update(updateWrapper);
        if(!bupdate){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        log.info("#####[微信支付回调]  操作成功");
    }
}
