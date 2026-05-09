package com.mallcloud.coupon.biz.controller;

import com.mallcloud.coupon.api.client.CouponRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController implements CouponRemoteClient {
}
