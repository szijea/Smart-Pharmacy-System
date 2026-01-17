# 因网络限制无法拉取远程镜像，这里使用本地已存在的镜像作为基础
# 请注意，确保 springboot-main-app:latest 包含 Java 17 运行环境
FROM springboot-main-app:latest
# 设置容器内工作目录
WORKDIR /app
# 复制最新编译的 Jar 包，重命名为 app.jar
COPY target/pharmacy-system-1.2.3.jar app.jar
# 暴露端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
