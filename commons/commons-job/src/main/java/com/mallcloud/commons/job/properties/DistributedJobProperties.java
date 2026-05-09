package com.mallcloud.commons.job.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 分布式定时任务配置属性类
 * 用于接收和绑定application.yml中以"xxl.job"为前缀的配置项
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = DistributedJobProperties.PREFIX)
public class DistributedJobProperties {
    /**
     * 配置属性前缀常量
     * 对应配置文件中的 xxl-job 前缀
     */
    public static final String PREFIX = "xxl-job";


}
