package com.mallcloud.product.biz.controller;

import com.mallcloud.product.api.client.ProductRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单控制器
 * 实现 order-api 中定义的 Client 接口，确保生产者和消费者的契约强一致性
 * 虽然实现了 OrderClient 接口，但我们建议在实现类上显式标注 @RequestMapping，增加可读性并方便插件识别
 */
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ProductController implements ProductRemoteClient {

}