package com.leyou.item.test;

import net.bytebuddy.asm.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRedis {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void test1(){
        redisTemplate.opsForValue().set("name","jack",2, TimeUnit.MINUTES);

        redisTemplate.opsForHash().put("user:1","name","jack");
        redisTemplate.opsForHash().put("user:1","age","20");
        redisTemplate.opsForHash().put("user:2","name","rose");
        redisTemplate.opsForHash().put("user:2","age","22");

        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps("user:3");
        boundHashOps.put("name","jerry");
        boundHashOps.put("age","50");
    }
}
