# Copilot 指南（Smart-Pharmacy-System）

## 架构速览
- 这是单体 Spring Boot 3.2 应用（Java 17），前后端同仓：后端在 [src/main/java](src/main/java)，前端为静态 HTML/JS/CSS 在 [src/main/resources/static](src/main/resources/static)。
- 多租户通过“多数据源 + 路由数据源”实现：
  - 运行期 DataSource 来自 `spring.tenants[]`（见 [src/main/resources/application.yaml](src/main/resources/application.yaml)）。
  - `TenantFilter` 读取 `X-Shop-Id` 或 `shopId` 写入 `TenantContext`（只拦截 `/api/*`），`StoreRoutingDataSource` 根据 ThreadLocal 切库（见 [src/main/java/com/pharmacy/multitenant](src/main/java/com/pharmacy/multitenant)）。
  - 租户数据源可在运行时追加（`/api/admin/tenants`，见 [src/main/java/com/pharmacy/controller/TenantAdminController.java](src/main/java/com/pharmacy/controller/TenantAdminController.java)）。

## 关键初始化/修复逻辑
- 应用启动会自动补齐多租户核心表并种子账号：
  - `MultiTenantSchemaInitializer` 检测并补齐表（如 `stock_in`、`member` 等）。
  - `MultiTenantSeeder` 补种默认角色/用户（bht/wx/rzt）。
- 管理端修复入口：`/api/admin/repair-stock-in-tables`（见 [src/main/java/com/pharmacy/controller/AdminTenantController.java](src/main/java/com/pharmacy/controller/AdminTenantController.java)）。
- 生产库结构修复脚本集中在 [db/fix-scripts](db/fix-scripts)（含 `wx_fix.sql`、`bht_fix.sql`、`rzt_db_fix.sql`），操作流程写在 [db/fix-scripts/README.md](db/fix-scripts/README.md)。

## 前端/接口约定
- 前端无构建流程，直接加载本地 Tailwind CSS 与静态资源；API 基础地址为相对 `/api`，可被 `window.API_BASE` 覆盖（见 [src/main/resources/static/js/api.js](src/main/resources/static/js/api.js)）。
- 选择租户时务必传 `X-Shop-Id` 头；Boss 页面会通过 `/api/boss/tenants` 拉取租户列表（见 [src/main/resources/static/boss.html](src/main/resources/static/boss.html)）。

## 常用流程（来自仓库脚本）
- 本地运行（Windows）：`./mvnw.cmd clean spring-boot:run`（见 [README.md](README.md)）。
- Docker 一键启动：`docker-compose up -d --build`（见 [docker-compose.yml](docker-compose.yml)）。
- E2E 接口冒烟：运行 [e2e_run.ps1](e2e_run.ps1)，结果在 `e2e_results/`。
- 镜像打包：运行 [docker-build.ps1](docker-build.ps1) 生成 `pharmacy-system.tar`。

## 调试/健康检查入口
- 健康检查：`/api/health`、`/api/health/schema`、`/api/health/diff`（见 [src/main/java/com/pharmacy/controller/HealthController.java](src/main/java/com/pharmacy/controller/HealthController.java)）。

## 代码修改时的定位建议
- 控制器在 [src/main/java/com/pharmacy/controller](src/main/java/com/pharmacy/controller)，JPA 与 MyBatis 混用（MyBatis 配置见 [src/main/resources/application.yaml](src/main/resources/application.yaml)）。
- 多租户与路由逻辑集中在 [src/main/java/com/pharmacy/multitenant](src/main/java/com/pharmacy/multitenant)。
