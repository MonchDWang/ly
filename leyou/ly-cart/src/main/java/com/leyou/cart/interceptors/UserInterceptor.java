package com.leyou.cart.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    /**
     * 从request中 获取token
     * 从token中解析出用户信息
     * 把userid 放入threadlocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try{
//        获取token
            String token = CookieUtils.getCookieValue(request, "LY_TOKEN");
//        解析payload 获取内容
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, UserInfo.class);
//        从用户自描述信息中获取用户的id
            Long userId = payload.getUserInfo().getId();
//        把用户的id 放入 容器中
            UserHolder.setUser(userId);
            return true;
        }catch(Exception e){
            e.printStackTrace();
            // 解析失败，不继续向下
            log.error("【购物车服务】解析用户信息失败！", e);
            return false;
        }

    }

    /**
     * 线程使用完毕
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
