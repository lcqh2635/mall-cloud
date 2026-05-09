package com.mallcloud.chat.biz.controller;

import com.mallcloud.chat.api.client.ChatRemoteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController implements ChatRemoteClient {
}
