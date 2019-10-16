package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
//    目录地址
    private String pubKeyPath;//: F:/ssh/id_rsa.pub # 公钥地址
    private String priKeyPath;//: F:/ssh/id_rsa # 私钥地址

    //生成公钥和私钥的java对象
    private PublicKey publicKey;
    private PrivateKey privateKey;

//    @PostConstruct
//    public void getKey(){
//         publicKey = RsaUtils.getPublicKey(pubKeyPath);
//         privateKey = RsaUtils.getPrivateKey(priKeyPath);
//    }
    private UserProp user = new UserProp();
    @Data
    public class UserProp{
        private Integer expire;//: 30 # 过期时间,单位分钟
        private String cookieName;//: LY_TOKEN # cookie名称
        private String cookieDomain;//: leyou.com # cookie的域
        private Integer minRefreshInterval; //刷新时间
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}
