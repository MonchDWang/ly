package com.leyou.order.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        if(!requestTemplate.path().contains("address")){
            return ;
        }
        //获取request对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //获得request对象
        HttpServletRequest request = requestAttributes.getRequest();

        String cookie = request.getHeader("cookie");
        System.out.println("##################cookie=="+cookie);
        if(StringUtils.isEmpty(cookie)){
            return ;
        }
        requestTemplate.header("cookie",cookie);

    }
}
