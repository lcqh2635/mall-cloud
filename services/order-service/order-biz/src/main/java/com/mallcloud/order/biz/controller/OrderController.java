package com.mallcloud.order.biz.controller;


import com.mallcloud.order.api.client.OrderClient;
import com.mallcloud.order.api.dto.OrderResponse;
import com.mallcloud.order.biz.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 * 实现 order-api 中定义的 Client 接口，确保生产者和消费者的契约强一致性
 * 虽然实现了 OrderClient 接口，但我们建议在实现类上显式标注 @RequestMapping，增加可读性并方便插件识别
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderClient {

    private final OrderService orderService;

    /**
     * 实现接口方法
     * 建议显式标注 @GetMapping，Apifox 等工具能更精准地抓取
     */
    @Override
    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable("id") Long id) {
        // 调用内部业务逻辑，并将结果映射为 DTO
        return orderService.findOrderById(id);
    }
}