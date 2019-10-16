package com.leyou.order.utils;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置redisson
 */
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient Redissonconfig(RedisProperties prop){
        String address = "redis://%s:%d";//reids://+prop.getHost()+
        Config config = new Config();
        config.useSingleServer().setAddress(String.format(address,prop.getHost(),prop.getPort()));
        return Redisson.create(config);

    }
}
