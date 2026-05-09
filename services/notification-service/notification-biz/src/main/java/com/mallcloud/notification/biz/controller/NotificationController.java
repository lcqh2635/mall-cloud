package com.mallcloud.notification.biz.controller;

import com.mallcloud.notification.api.client.NotificationRemoteClient;
import com.mallcloud.notification.api.constant.NotificationApiPath;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(NotificationApiPath.USER)
@RequiredArgsConstructor
public class NotificationController implements NotificationRemoteClient {
}
