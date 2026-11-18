# 1. 基础镜像：必须使用 Java 17 的精简版运行环境
FROM openjdk:17-jdk-slim

# 2. 安装验证码图片生成所需的字体/图形库
# 解决 /auth/captcha 报错：libfreetype.so.6: cannot open shared object file
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# 3. 锚定工作目录
WORKDIR /app

# 4. 复制 Jar 包到容器内
COPY target/docker_campus_platform-0.0.1-SNAPSHOT.jar app.jar

# 5. 暴露 SpringBoot 端口
EXPOSE 8080

# 6. 启动生产环境
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]