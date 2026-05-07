package com.mallcloud.commons.api.services.user;

import com.urbane.commons.model.entity.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 用户服务HTTP客户端接口。
 * 使用 @HttpExchange 注解声明基础路径，该路径会应用于接口内所有方法。
 * Spring Boot 会自动在运行时为此接口生成代理实现。
 */
@HttpExchange(url = "/api/v1/users", accept = "application/json", contentType = "application/json")
public interface UserServiceClient {

    /**
     * 获取所有用户列表。
     * 使用 @GetExchange 注解，表示这是一个 HTTP GET 请求。
     * 方法返回 Flux<User>，表示这是一个响应式流，可以处理多个异步返回的用户对象。
     * 等同于请求：GET /api/v1/users
     *
     * @return 包含所有用户的响应式流
     */
    @GetExchange
    Flux<UserEntity> getAllUsers();

    /**
     * 根据ID获取单个用户。
     * 使用 @GetExchange 注解，并在URL中通过 {id} 定义路径变量。
     * 使用 @PathVariable 注解将方法参数 id 绑定到URL路径变量 {id}。
     * 方法返回 Mono<User>，表示一个异步的、可能为单个或空的用户对象。
     * 等同于请求：GET /api/v1/users/{id}
     *
     * @param id 用户唯一标识符
     * @return 包含查询到的用户的 Mono 对象
     */
    @GetExchange("/{id}")
    Mono<UserEntity> getUserById(@PathVariable("id") Long id);

    /**
     * 分页查询用户。
     * 使用 @GetExchange 注解指定具体路径 “/page”。
     * 使用 @RequestParam 注解将方法参数绑定为URL查询参数，例如 ?page=1&size=10。
     * 如果参数值为 null，对应的查询参数会被省略（这是与 @RequestParam 传统用法的区别）。
     * 等同于请求：GET /api/v1/users/page?page={page}&size={size}
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 包含分页用户数据的 Map 结构（假设服务端返回JSON对象）
     */
    @GetExchange("/page")
    Mono<Map<String, Object>> getUsersByPage(@RequestParam Integer page, @RequestParam Integer size);

    /**
     * 创建新用户。
     * 使用 @PostExchange 注解，表示这是一个 HTTP POST 请求。
     * 使用 @RequestBody 注解将 User 类型参数 user 序列化为JSON，并作为请求体发送。
     * 返回 ResponseEntity<Void>，可以访问HTTP响应的状态码和头信息。
     * 等同于请求：POST /api/v1/users
     *
     * @param user 要创建的用户对象（不含ID）
     * @return 包含HTTP响应状态的 ResponseEntity
     */
    @PostExchange
    Mono<ResponseEntity<Void>> createUser(@RequestBody UserEntity user);

    /**
     * 更新用户信息。
     * 使用 @PutExchange 注解，表示这是一个 HTTP PUT 请求，并指定包含 {id} 的路径。
     * 结合使用 @PathVariable 和 @RequestBody 注解。
     * 等同于请求：PUT /api/v1/users/{id}
     *
     * @param id   要更新的用户ID
     * @param user 更新后的用户信息
     * @return 更新完成后的用户对象
     */
    @PutExchange("/{id}")
    Mono<UserEntity> updateUser(@PathVariable("id") Long id, @RequestBody UserEntity user);

    /**
     * 删除用户。
     * 使用 @DeleteExchange 注解，表示这是一个 HTTP DELETE 请求。
     * 等同于请求：DELETE /api/v1/users/{id}
     *
     * @param id 要删除的用户ID
     * @return 不包含响应体的 Mono 信号，仅表示操作完成
     */
    @DeleteExchange("/{id}")
    Mono<Void> deleteUser(@PathVariable("id") Long id);
}