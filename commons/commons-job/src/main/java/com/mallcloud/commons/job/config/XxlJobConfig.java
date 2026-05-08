package com.mallcloud.commons.job.config;

import com.mallcloud.commons.job.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class XxlJobConfig {

    /**
     * 自定义配置执行器，执行器需要注册到调度中心
     */
    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobProperties properties) {
        log.info(">>>>>>>>>>> xxl-job executor config init.");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdmin().getAddress());
        executor.setAddress(properties.getExecutor().getAddress());
        executor.setAppname(properties.getExecutor().getAppname());
        executor.setIp(properties.getExecutor().getIp());
        executor.setPort(properties.getExecutor().getPort());
        executor.setAccessToken(properties.getExecutor().getAccessToken());
        executor.setLogPath(properties.getExecutor().getLogPath());
        executor.setLogRetentionDays(properties.getExecutor().getLogRetentionDays());
        return executor;
    }
}
