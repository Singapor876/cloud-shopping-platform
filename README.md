# Cloud 微服务商城系统

这是一个基于 Spring Cloud 与 Vue 的前后端分离商城项目，包含统一认证、API 网关、系统管理、商品、订单、会员、内容和文件等服务，并提供商城前台与后台管理界面。

> 本项目是在开源项目 [RuoYi-Cloud（若依 Cloud）](https://gitee.com/y_project/RuoYi-Cloud) 基础上进行的二次开发，遵循 MIT 许可证。若依框架的版权归原作者所有，本仓库重点展示在商城业务方向上的学习与扩展成果。

## 主要模块

- `cloud-auth`：统一认证服务
- `cloud-gateway`：API 网关与访问控制
- `cloud-common`：公共组件、工具与安全支持
- `cloud-api`：服务间接口定义
- `cloud-modules`：系统、会员、商品、订单、内容和文件等业务模块
- `admin-ui`：Vue 2 + Element UI 后台管理界面
- `front-ui`：商城用户端界面

## 技术栈

- Java 8、Spring Boot 2.6、Spring Cloud 2021
- Spring Cloud Alibaba、Nacos
- MyBatis-Plus、Redis、MySQL
- Vue 2、Vuex、Vue Router、Element UI
- Maven、Node.js

## 本地运行

### 使用 Docker Compose 启动基础设施

复制环境变量模板并按需修改密码：

```powershell
Copy-Item .env.example .env
docker compose up -d
```

该命令会启动：

- MySQL 8：`localhost:3306`
- Redis 7：`localhost:6379`
- Nacos 2.0.4：`http://localhost:8848/nacos`
- MinIO API：`http://localhost:9000`
- MinIO 控制台：`http://localhost:9001`

MySQL 首次启动会自动执行：

- `deploy/mysql/init/01-schema.sql`：28 张表的纯结构
- `deploy/mysql/init/02-seed.sql`：脱敏后的最小系统数据

导入 Nacos 配置：

```powershell
.\deploy\nacos\import-config.ps1
```

然后继续启动后端服务：

1. 确认 MySQL、Redis、Nacos 与 MinIO 均已启动。
2. 在 Nacos 控制台检查 `application-dev.yml` 和各服务配置是否存在。
3. 设置安全的 JWT 密钥：

   ```powershell
   $env:JWT_SECRET="请替换为足够长的随机字符串"
   ```

4. 编译后端：

   ```bash
   mvn clean package -DskipTests
   ```

5. 分别启动认证、网关和所需业务模块。
6. 安装并启动后台管理界面：

   ```bash
   cd admin-ui
   npm install
   npm run dev
   ```

7. 安装并启动商城用户端：

   ```bash
   cd front-ui
   npm install
   npm run serve
   ```

建议的后端启动顺序为：`cloud-system`、`cloud-file`、`cloud-cms`、`cloud-ums`、`cloud-oms`、`cloud-auth`、`cloud-gateway`。

> 演示数据中的管理员账号沿用原开发数据：`admin / admin123`。仅限本地开发，首次登录后必须立即修改密码。生产环境不得使用模板中的默认密码。

## 安全说明

- 本公开版本不包含论文、个人文档、日志、数据库备份、构建产物和本地依赖。
- 仓库不提供生产环境密码、密钥或真实业务数据。
- 部署前必须自行设置数据库、Redis、Nacos、文件服务和 JWT 配置。
- 请勿在公开仓库中提交真实账号、密码或 Token。

## 在线演示

该项目由多个后端微服务、数据库、Redis 和 Nacos 共同组成，无法通过 GitHub Pages 完整运行。后续若配置独立服务器，可在此补充完整演示地址。

## 开源许可与致谢

本项目遵循 [MIT 许可证](LICENSE)。感谢 [RuoYi-Cloud](https://gitee.com/y_project/RuoYi-Cloud) 提供的开源基础框架。

