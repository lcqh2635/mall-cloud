package com.mallcloud.commons.banner.listener;

import org.springframework.boot.context.event.*;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static java.lang.System.*;

@Order(1)
@Component
public class MallCloudBannerListener implements ApplicationListener<SpringApplicationEvent> {

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        // 根据事件类型执行不同的逻辑
        switch (event) {
            case ApplicationStartingEvent _ ->
                    out.println("1、MallCloudBannerListener - 应用启动中");
            case ApplicationEnvironmentPreparedEvent _ ->
                    out.println("2、MallCloudBannerListener - 环境准备完成");
            case ApplicationPreparedEvent _ ->
                    out.println("3、MallCloudBannerListener - 应用准备完成");
            case ApplicationStartedEvent _ ->
                    out.println("4、MallCloudBannerListener - 应用已启动");
            case ApplicationReadyEvent _ ->
                    out.println("5、MallCloudBannerListener - 应用准备就绪");
            case ApplicationFailedEvent _ ->
                    out.println("6、MallCloudBannerListener - 应用启动失败");
            default -> out.println("7、MallCloudBannerListener - Hello World!");
        }
    }

}
