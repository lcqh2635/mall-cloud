package com.mallcloud.commons.api.services.order;

import com.urbane.commons.model.entity.order.CreateOrderRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import reactor.core.publisher.Mono;

/**
 * 订单服务远程调用接口定义（契约）
 * 功能：
 *   - 定义其他服务如何调用 order-service
 *   - 供 cart-service、payment-gateway、notification-service 等调用
 *   - 不包含任何实现，仅声明方法签名和注解
 *
 * 注意：
 *   - 使用 @FeignClient 指定目标服务名（必须与 Nacos 中的服务名一致）
 *   - 所有路径、方法、参数、返回值必须与 order-service 实现严格一致
 *   - 返回类型统一使用 ResponseResult<T>，保证一致性
 */
// @HttpExchange 是我们可以应用于 HTTP 接口及其 exchange 方法的根注解。如果我们将其应用于接口层，那么它就会应用于所有 exchange 方法。
// 这对于指定所有接口方法的共同属性（如 content type 或 URL 前缀）非常有用。模式类似有 @RequestMapping。
@HttpExchange(
        url = "/repos/{owner}/{repo}",
        accept = "application/vnd.github.v3+json"
)
public interface OrderServiceClient {

    /**
     * 创建订单
     * 调用方：cart-service
     * 路径：POST /order/create
     * 输入：购物车快照
     * 输出：订单ID和摘要
     */
    @PostExchange("/order/create")
    Mono<ResponseEntity<String>> createOrder(@RequestBody CreateOrderRequest request);

    /**
     * 查询订单摘要
     * 调用方：notification-service、user-service
     * 路径：GET /order/{orderId}
     * 输出：订单基础信息
     */
    @GetExchange("/order/{orderId}")
    Mono<ResponseEntity<String>> getOrderSummary(@PathVariable("orderId") Long orderId);

    /**
     * 批量查询用户订单列表
     * 调用方：user-service
     * 路径：GET /order/list
     * 输出：订单列表
     */
    @GetExchange("/order/list")
    Mono<ResponseEntity<String>> listOrdersByUserId(@RequestParam("userId") Long userId);

    /**
     * 更新订单状态（支付成功后触发）
     * 调用方：payment-gateway
     * 路径：PUT /order/{orderId}/status
     * 输入：状态码
     */
    @PutExchange("/order/{orderId}/status")
    Mono<ResponseEntity<String>> updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestParam("status") String status);
}
