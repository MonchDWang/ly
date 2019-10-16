package com.leyou.gateway.filter;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterprop;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER -1 ;
    }

    @Override
    public boolean shouldFilter() {
        //获取request对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String path =request.getRequestURI();
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path){
        boolean b = false;
        List<String> allowPaths = filterprop.getAllowPaths();
        for (String allowPath : allowPaths) {
            if(path.startsWith(allowPath)){
                b = true;
                break;
            }
        }
        return b;
    }

    @Override
    public Object run() throws ZuulException {
        //获取request对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        try{
//        获取token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
//        用jwt去解析token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
//        获取用户的自描述信息
            UserInfo userInfo = payload.getUserInfo();
            //获取用户角色
            String role = userInfo.getRole();
//         获取当前资源路径
            String path = request.getRequestURI();
            String method = request.getMethod();
            // TODO 判断权限，此处暂时空置，等待权限服务完成后补充
            log.info("【网关】用户{},角色{}。访问服务{} : {}，", userInfo.getUsername(), role, method, path);
        }catch (Exception e){
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
            log.error("非法访问，未登录，地址：{}", request.getRemoteHost(), e );
        }
        return null;
    }
}
