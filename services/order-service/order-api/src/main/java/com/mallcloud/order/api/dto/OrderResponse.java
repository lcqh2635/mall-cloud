package com.mallcloud.order.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单响应对象
 * 使用 JDK Record 保证不可变性，且语法简洁，自动生成构造、Getter、toString 等
 */
public record OrderResponse (
    Long orderId,
    String orderSn,
    BigDecimal amount,
    String status
) implements Serializable {}