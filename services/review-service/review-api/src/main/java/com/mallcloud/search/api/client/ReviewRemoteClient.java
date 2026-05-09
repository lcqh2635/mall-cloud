package com.mallcloud.search.api.client;

import com.mallcloud.search.api.constant.ReviewApiPath;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * 用户服务远程调用接口
 * <p>
 * 基于 Spring 6+ Http Interface 实现
 * <p>
 * 注意：
 * 1. 这里只定义接口契约
 * 2. 不写业务逻辑
 * 3. 不依赖 Service
 * 4. 服务提供方直接实现该接口
 */
@HttpExchange(ReviewApiPath.REVIEW)
public interface ReviewRemoteClient {

}