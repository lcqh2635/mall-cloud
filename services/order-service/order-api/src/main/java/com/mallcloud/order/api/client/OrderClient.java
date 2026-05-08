package com.mallcloud.order.api.client;

import com.mallcloud.order.api.dto.OrderResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务客户端接口
 * HttpExchange 是 Spring 原生支持的，相比 OpenFeign 更轻量，更易适配虚拟线程
 */
@HttpExchange("/orders")
public interface OrderClient {

    /**
     * 根据 ID 查询订单详情
     */
    @GetExchange("/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);
}