# 统一使用官方 Java 17 运行时镜像
FROM amazoncorretto:17
# 设置容器内工作目录
WORKDIR /app
# 复制最新编译的 Jar 包，重命名为 app.jar
COPY target/pharmacy-system-1.2.3.jar app.jar
# 暴露端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
