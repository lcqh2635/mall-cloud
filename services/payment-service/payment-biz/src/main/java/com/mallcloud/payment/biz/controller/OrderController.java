package com.mallcloud.payment.biz.controller;

import com.mallcloud.order.api.client.OrderRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderRemoteClient {
}
