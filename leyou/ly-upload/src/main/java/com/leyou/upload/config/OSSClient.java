package com.leyou.upload.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OSSproperties.class)
public class OSSClient {

    @Bean
    public OSS ossClient(OSSproperties prop){
        return new OSSClientBuilder()
                .build(prop.getEndpoint(),prop.getAccessKeyId(),prop.getAccessKeySecret());
    }
}
