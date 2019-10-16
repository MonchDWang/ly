package com.leyou.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

@Data
@Configuration
@ConfigurationProperties(prefix = "ly.encoder.crypt")
public class PasswordConfig {

    private String secret;//: ${random.uuid} # 随机的密钥，使用uuid
    private Integer strength;//: 10 # 加密强度4~31，决定了密码和盐加密时的运算次数，超过10以后加密耗时会显著增加

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        return new BCryptPasswordEncoder(strength,secureRandom);
    }

}
