# 校园综合服务平台 · Java / Docker

基于 Spring Boot 3、Java 17、MyBatis、MySQL、Redis 和 Thymeleaf 的校园服务项目，包含二手商品、校园任务、订单、公告及后台管理等模块，并提供 Dockerfile。

## 现有功能

- 用户注册、登录、个人信息与角色访问拦截。
- 商品发布与浏览、订单管理、超时订单处理。
- 校园任务、公告和管理后台页面。
- Redis 缓存及 Spring Session Redis 共享会话。
- 图片上传的文件头校验及外部目录映射。
- 开发/生产环境配置与 Dockerfile；容器内补充验证码所需的字体和图形库。

本仓库展示已有源码与容器化准备工作，不代表已经完成云端、Compose 或 Kubernetes 的部署验证。

## 目录

| 路径 | 内容 |
| --- | --- |
| `src/main/java/com/campus_platform/controller` | 页面、业务与管理接口 |
| `src/main/java/com/campus_platform/service` | 业务逻辑 |
| `src/main/java/com/campus_platform/mapper` | MyBatis 数据访问 |
| `src/main/resources/templates` | Thymeleaf 页面 |
| `src/main/resources/static` | 样式、脚本和界面素材 |
| `Dockerfile` | Java 17 容器构建定义 |

## 本地运行

准备 Java 17、Maven、MySQL 和 Redis。当前仓库尚未包含数据库初始化 SQL，首次在新环境运行需要自行准备与 Mapper 匹配的表结构；仅克隆仓库不足以完成全流程运行。

1. 将 `src/main/resources/application-dev.example.yml` 复制为同目录的 `application-dev.yml`。
2. 填写本地数据库账号、密码、Redis 地址与密码，以及图片存储目录。
3. 默认配置激活 `dev`。通过 IDE 启动 `CampusPlatformApplication`，或在根目录运行：

```powershell
.\mvnw.cmd spring-boot:run
```

默认地址为 `http://localhost:8080`。原开发目录已保留真实本地配置，无需重新填写，也不要求改用环境变量。

## Docker 构建准备

Dockerfile 从 `target` 复制预先打好的 JAR，需要先打包：

```powershell
.\mvnw.cmd -DskipTests package
docker build -t campus-platform:local .
```

容器启动使用 `prod` 配置，仍需配置可访问的 MySQL、Redis 和 `/app/data/upload/` 持久化存储。`application-prod.yml` 提供参数占位符；完整启动命令、数据库初始化、参数覆盖方式、健康探针与持久化方案尚未验证或补齐。此处不提供已经上云的承诺。

注意：Git 忽略本地配置不等于打包时自动排除；当前 Maven 打包仍可能将本地开发配置放入 JAR，发布镜像前需另行处理配置隔离。请勿直接将包含本地密码的镜像公开发布。

## 演示范围与后续工作

- `ChaosController` 提供 `/chaos/kill-liveness` 故障模拟接口，现有拦截配置将其放行；该接口仅适合受控演示，对公网部署前需关闭或加访问限制。
- 目录内没有 Compose、Kubernetes 清单或数据库初始化 SQL；后续可补充可复现部署流程。
- 代码保留了小组协作相关注释。用于个人作品集时，应根据实际分工说明个人贡献。
- 上传时不重新部署、不修改业务逻辑，也不更换本地数据库密码。

## Git 文件范围

上传前使用现有 Java 17 和 Maven 缓存离线编译，46 个 Java 源文件编译通过。未启动数据库、Redis、容器或执行端到端联调。

真实 `application-dev.yml`、上传图片、IDE 设置和 `target` 构建产物留在本地，不进入仓库。公开提供无密码的开发配置示例。

后续直接在原项目修改，并在根目录提交：

```powershell
git status
git add .
git commit -m "说明这次修改"
git push
```

当前未为原创代码指定开源许可证，第三方依赖遵循各自许可证。
