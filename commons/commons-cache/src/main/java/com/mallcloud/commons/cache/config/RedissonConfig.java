package com.mallcloud.commons.cache.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;

public class RedissonConfig {

    /**
     * Redisson 配置
     * Redisson 配置参考官网： <a href="https://github.com/redisson/redisson/blob/master/redisson-spring-boot-starter/README.md">...</a>
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.56.10:6379")
                .setPassword("479368");
        return Redisson.create(config);
    }

}
