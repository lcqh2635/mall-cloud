# ===== 第一阶段：构建阶段 =====
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /build

# 先复制 pom.xml 利用 Docker 缓存，依赖未变化时不重新下载
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（利用缓存层）
RUN ./mvnw dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ===== 第二阶段：运行阶段 =====
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# 创建非 root 用户运行，提高安全性
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/*.jar app.jar

# 修改文件归属
RUN chown appuser:appgroup app.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8082

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseZGC"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]