package com.mallcloud.cart.biz.controller;

import com.mallcloud.cart.api.client.CartRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController implements CartRemoteClient {
}
