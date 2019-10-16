package com.leyou.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class GlobalCORSConfig {

    @Bean
    public CorsFilter corsFilter(CorsProperties prop){
        //        1.添加cors的配置信息
        CorsConfiguration config = new CorsConfiguration();
//          允许访问的域
        config.setAllowedOrigins(prop.getAllowedOrigins());
//          是否允许发送cookie
        config.setAllowCredentials(prop.getAllowedCredentials());
//          允许的请求方式
        config.setAllowedMethods(prop.getAllowedMethods());
//          允许的头信息
        config.setAllowedHeaders(prop.getAllowedHeaders());
//          访问有效期
        config.setMaxAge(prop.getMaxAge());

//       2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(prop.getFilterPath(),config);
//       3.返回新的CORSFilter
        return new CorsFilter(source);
    }
}
