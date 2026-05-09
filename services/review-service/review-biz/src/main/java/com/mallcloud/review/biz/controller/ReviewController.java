package com.mallcloud.review.biz.controller;

import com.mallcloud.search.api.client.ReviewRemoteClient;
import com.mallcloud.search.api.constant.ReviewApiPath;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * <p>
 * 直接实现 UserApi 接口
 */
@RestController
@RequestMapping(ReviewApiPath.REVIEW)
@RequiredArgsConstructor
public class ReviewController implements ReviewRemoteClient {


}