package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfigImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PayHelper {
    @Autowired
    private WXPay wxPay;

    @Autowired
    private WXPayConfigImpl wxPayConfig;

    /**
     * 统一下单
     * @param orderId 本系统的订单id
     * @param totalFee  本次交易的总金额
     * @param desc   交易的描述信息
     * @return  url   生成二维码使用
     */
    public String createOrder(Long orderId,Long totalFee,String desc){
        Map<String, String> reqData = new HashMap<>();
        reqData.put("body",desc);
        reqData.put("out_trade_no",orderId.toString());
        reqData.put("total_fee",totalFee.toString());
        reqData.put("spbill_create_ip","127.0.0.1");
        //TODO url
        reqData.put("notify_url","http://fqggcv.natappfree.cc/api/pay/wx/notify");
        reqData.put("trade_type","NATIVE");

        Map<String, String> resultMap = null;

        try{
            resultMap = wxPay.unifiedOrder(reqData);
        }catch(Exception e){
            e.printStackTrace();
        }
        checkResultCode(resultMap);

        String codeUrl = resultMap.get("code_url");
        if (StringUtils.isBlank(codeUrl)) {
            throw new RuntimeException("微信下单失败，支付链接为空");
        }
        return codeUrl;

    }


    public void checkResultCode(Map<String, String> result) {
        // 检查业务状态
        String resultCode = result.get("result_code");
        if ("FAIL".equals(resultCode)) {
            log.error("【微信支付】微信支付业务失败，错误码：{}，原因：{}", result.get("err_code"), result.get("err_code_des"));
            throw new RuntimeException("【微信支付】微信支付业务失败");
        }
    }
}
