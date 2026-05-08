#!/bin/bash
echo "Step 1: 启动基础设施（DB、Redis）..."
podman-compose -f compose-base.yaml up -d
echo "等待基础设施启动完成..."
sleep 10

echo "Step 2: 启动治理中心（Nacos、Seata）..."
podman-compose -f compose-governance.yaml up -d
echo "等待治理中心启动完成..."
sleep 10

echo "Step 3: 启动服务..."
podman-compose -f compose-service.yaml up -d
echo "等待服务启动完成..."
sleep 10

echo "Step 4: 启动网关..."
podman-compose -f compose-gateway.yaml up -d
echo "等待网关启动完成..."
sleep 10

echo "Step 5: 启动客户端..."
podman-compose -f compose-client.yaml up -d
echo "等待客户端启动完成..."
sleep 10

echo "全部启动完成！..."