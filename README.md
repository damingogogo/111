# 产后返岗情绪支持平台

技术栈：

- 后端：Spring Boot 3、JDK 17、MySQL 8.0、MinIO
- 后台：Vue 3、Vite、Element Plus、ECharts、lucide 图标
- 移动端：uniapp 微信小程序

## 目录

- `backend`：后端接口、数据库建表、演示数据、MinIO 上传
- `admin-web`：企业 HR/管理员后台，左侧菜单、右侧内容区、数据统计图表、业务 CRUD
- `mobile-uniapp`：员工端微信小程序，筛查、课程、咨询、状态追踪、个人报告

## 端口

- 后端：`18763`
- 后台前端：`31879`

## 数据库

先创建数据库：

```sql
source mysql-init.sql;
```

或手动执行：

```sql
create database if not exists emotion_ai default character set utf8mb4 collate utf8mb4_unicode_ci;
```

后端 JDBC URL 已配置 `createDatabaseIfNotExist=true`，使用 `root/123456` 启动时通常会自动创建 `emotion_ai` 数据库；如果你的 MySQL 用户没有建库权限，再手动执行上面的 SQL。

后端使用 Flyway 版本迁移：

- `backend/src/main/resources/db/migration/V1__create_core_tables.sql`：创建核心表
- `backend/src/main/resources/db/migration/V2__insert_demo_data.sql`：插入演示数据

每张表都有 10 条演示数据，图片字段使用 MinIO URL 形式。Flyway 只会执行未执行过的版本迁移，不会每次启动都删表重建。

正式部署时建议使用 `prod` profile：

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 后端启动

确认本机已安装 JDK 17、Maven、MySQL 8.0，MySQL root 密码为 `123456`。

```bash
cd backend
mvn spring-boot:run
```

Windows 也可以直接运行：

```text
scripts\start-backend.bat
```

登录账号：

- 账号：`admin`
- 密码：`123456`

MinIO 端点和桶名已在 `backend/src/main/resources/application.yml` 中配置，访问密钥通过环境变量读取，避免提交到代码仓库：

- endpoint：`http://119.29.152.180:9000`
- bucket：`deng`

启动前按你的实际 MinIO 信息设置：

```powershell
$env:MINIO_ACCESS_KEY="你的AccessKey"
$env:MINIO_SECRET_KEY="你的SecretKey"
```

如需改数据库连接，也可以设置：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/emotion_ai?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="123456"
```

## 后台启动

```bash
cd admin-web
npm install
npm run dev
```

Windows 也可以直接运行：

```text
scripts\start-admin.bat
```

访问：

```text
http://localhost:31879
```

## 微信小程序启动

```bash
cd mobile-uniapp
npm install
npm run build:mp-weixin
```

然后用微信开发者工具打开：

```text
D:\danzi\aaa\0514\2kAI情感\mobile-uniapp\dist\build\mp-weixin
```

当前不再使用 HBuilderX。`mobile-uniapp` 是 uni-app CLI 项目，微信开发者工具只打开构建后的 `dist\build\mp-weixin`。

如需直接生成微信开发者工具可导入的构建目录：

```text
scripts\build-miniapp.bat
```

生产构建输出目录：`mobile-uniapp/dist/build/mp-weixin`

如果后端部署到服务器，把 `mobile-uniapp/src/utils/request.js` 的 `BASE_URL` 改为服务器地址，例如：

```js
const BASE_URL = 'http://119.29.152.180:18763/api'
```

也可以在 `mobile-uniapp` 下创建 `.env`：

```text
VITE_API_BASE_URL=http://119.29.152.180:18763/api
```

微信开发者工具本地联调时需要勾选“不校验合法域名、web-view、TLS 版本以及 HTTPS 证书”。

## 已实现功能

- 员工端：账号上下文、AI 情绪筛查、个人报告、课程学习、咨询预约、每日状态记录
- 企业后台：账号管理、部门员工、筛查题库、报告、干预方案、课程、咨询师、预约、状态追踪、政策内容、后台设置
- 数据统计：员工总数、高风险预警、咨询预约、课程进度、风险分布、部门压力、情绪趋势、服务使用量
- 报表导出：后台统计页支持导出 CSV，接口为 `GET /api/dashboard/export`
- 文件上传：后台本地选择图片，后端上传到 MinIO 并写入 `upload_files`
- 图片预览：上传文件真实存储在 MinIO；后台优先使用后端 `/api/files?object=...` 代理预览，兼容 MinIO 桶未开放公网读的情况

需求图片对应的功能映射见：

```text
docs\function-map.md
```
