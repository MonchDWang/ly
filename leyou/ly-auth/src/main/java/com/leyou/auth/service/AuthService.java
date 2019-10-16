package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.DTO.UserDTO;
import com.leyou.user.client.UserClient;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;
    /**
     * 登录
     * @param username
     * @param password
     */
    public void login(String username, String password, HttpServletResponse response) {
        try {
//        1、验证用户名和密码
            UserDTO userDTO = userClient.query(username, password);
//        2、组装payload中的自描述信息
            UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), "admin");
//        3、生成jwt
            String jwtToken = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
//        4、jwt 发送给 客户端
            CookieUtils.newCookieBuilder()
                    .name(prop.getUser().getCookieName()) //cookie中token名字
                    .value(jwtToken) //cookie中token值
                    .domain(prop.getUser().getCookieDomain())
                    .httpOnly(true)//不允许js操作，只能由http来携带
                    .response(response)
                    .build();
        }catch(Exception e){
            throw  new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
    }

    /**
     * 验证用户
     * @param request
     * @return
     */
    public UserInfo verify(HttpServletRequest request,HttpServletResponse response) {
        try{
//        1、从请求中获取token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
//        2、用jwtutils 解密token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);

//            判断当前的token是否在黑名单中
            String jwtId = payload.getId();
            Boolean hasKey = redisTemplate.hasKey(jwtId);
            if(hasKey != null && hasKey){
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }

            UserInfo userInfo = payload.getUserInfo();
//            获取过期时间
            Date expiration = payload.getExpiration();
            //获取最早的刷新时间
            DateTime refreshTime = new DateTime(expiration).minusMinutes(prop.getUser().getMinRefreshInterval());
            //比较当前时间和 刷新时间 谁靠前，如果刷新时间靠前，就刷新token
            if(refreshTime.isBefore(System.currentTimeMillis())){
//                重新生成jwt
                token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
//                 把jwt放入cookie中
                CookieUtils.newCookieBuilder()
                        .response(response)
                        .name(prop.getUser().getCookieName())//LY_TOKEN
                        .value(token)
                        .domain(prop.getUser().getCookieDomain())//leyou.com
                        .httpOnly(true) //防止XSS
                        .build();
            }
            return userInfo;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 用户登出
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try{
//        用户token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
//        jwt解密token
            Payload<UserInfo> payLoad = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
//        jwt的唯一id 作为 redis的黑名单的key
            String jwtId = payLoad.getId();
//        jwt的过期时间
            Date expiration = payLoad.getExpiration();
            // 计算黑名单的过期时间
            long time = expiration.getTime() - System.currentTimeMillis();
            //把这个jwt的id加入黑名单,过期时间为 可用的剩余的时间，单位是毫秒
//            剩余时间超过5秒以上才写
            if (time > 5000) {
                redisTemplate.opsForValue().set(jwtId, "1", time, TimeUnit.MILLISECONDS);
            }
//        删除token
            CookieUtils.deleteCookie(prop.getUser().getCookieName(),
                    prop.getUser().getCookieDomain(),
                    response);
        }catch(Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }
}
