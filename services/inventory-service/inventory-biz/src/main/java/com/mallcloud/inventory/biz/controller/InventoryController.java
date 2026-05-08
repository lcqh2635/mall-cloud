package com.mallcloud.inventory.biz.controller;

import com.mallcloud.inventory.api.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController implements InventoryClient {
}
