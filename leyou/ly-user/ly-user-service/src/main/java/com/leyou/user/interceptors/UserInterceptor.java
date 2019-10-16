package com.leyou.user.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.CookieUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    /**
     * 从cookie中获取token
     * 从token解析用户信息，直接用base64解析
     * 放入userHolder
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try{
            String token = CookieUtils.getCookieValue(request, "LY_TOKEN");
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            Long userId = payload.getUserInfo().getId();
            UserHolder.setUser(userId);
            return true;
        }catch(Exception e){
            //throw  new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
            return false;

        }
    }

    /**
     * 把信息从threadlocal中删除
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
