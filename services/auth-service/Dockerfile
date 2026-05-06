# 依赖的基础镜像
# 21.0.4-graal
# FROM openjdk:21.0.4-graal
# 如果使用了镜像仓库，则需要提前从DockerHub仓库下载基础镜像，因为阿里云仓库不支持从DockerHub下载镜像。
FROM openjdk:21-oracle
# 直接使用阿里云镜像仓库的镜像，原理是将从DockerHub下载的镜像，上传到阿里云镜像仓库中，然后再从阿里云镜像仓库下载到本地。
# 这样做是因为docker插件配置了阿里云镜像仓库的镜像源，可以直接从阿里云镜像仓库下载镜像。而不会从DockerHub下载镜像。
# FROM registry.cn-hangzhou.aliyuncs.com/lcqh/openjdk:21-oracle

# 制作的镜像作者
MAINTAINER 龙茶清欢

# 在镜像中创建文件夹
RUN mkdir -p /blog-cloud

# 将上面创建的文件夹设置为工作区，也是配置挂载的目标目录
WORKDIR /blog-cloud

# 定义环境变量
ENV TZ=Asia/Shanghai JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
# 设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo '$TZ' > /etc/timezone

# 将项目打好的jar包复制到制作的镜像根目录，并重命名
COPY target/blog-cloud-service-1.0.0.jar service.jar

# 暴露运行端口
EXPOSE 8003

# 使用 docker run 命令默认将执行的脚本命令
# 启动容器时，会执行 sleep 10 秒，等待 MySQL 数据库启动完成，然后启动 Spring Boot 应用。
CMD sleep 10; java $JAVA_OPTS -jar service.jar

# -Djava.security.egd=file:/dev/./urandom 可以避免在高并发下因大量生成随机数造成性能会较低

# 使用 ZGC 垃圾回收器，提高 GC 效率。ZGC使用配置参考 https://win11.ren/2022/03/17/java/zgc/
# ZGC 是一种低延迟的垃圾收集器，适用于需要极低 GC 暂停时间的应用场景
# JAVA_OPTS="-XX:+UseZGC -Xms128m -Xmx512m -Xlog:gc"

# 打开终端或命令提示符窗口，然后导航到包含 .jar 文件的目录。假设你的 .jar 文件名为 myapp.jar，你可以使用以下命令来运行它：
# 其中 -Xmx512m 是设置最大堆内存为 512MB，-XX:+UnlockExperimentalVMOptions 是启用实验性的 ZGC 选项，-XX:+UseZGC 是启用 ZGC 垃圾回收器。
# java -Xms512m -Xmx1g -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -jar myapp.jar
# 或者，你可以直接运行 .jar 文件，不需要导航到目录：
# java -jar /path/to/myapp.jar