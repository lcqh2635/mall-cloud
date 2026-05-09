package com.mallcloud.auth.biz.controller;

import com.mallcloud.auth.api.client.AuthRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthRemoteClient {
}
