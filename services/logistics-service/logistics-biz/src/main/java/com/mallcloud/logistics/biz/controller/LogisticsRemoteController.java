package com.mallcloud.logistics.biz.controller;

import com.mallcloud.logistics.api.client.LogisticsRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsRemoteController implements LogisticsRemoteClient {
}
