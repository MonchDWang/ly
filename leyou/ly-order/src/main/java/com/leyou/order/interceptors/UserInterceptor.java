package com.leyou.order.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.CookieUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try{
//            1、从request中获取jwt
            String token = CookieUtils.getCookieValue(request, "LY_TOKEN");
//            2、直接解密payload，获取用户信息
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            Long userId = payload.getUserInfo().getId();
            UserHolder.setUser(userId);
            return true;
        }catch(Exception e){
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
