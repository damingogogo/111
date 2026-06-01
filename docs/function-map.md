# 功能清单映射

## 员工端小程序

- 核心定位：隐私化、个性化、轻量化心理服务与职场适应指导入口
- 账号激活：当前预留员工身份上下文，默认员工 ID 为 1，后端支持员工数据绑定
- AI 情绪筛查：`pages/assessment/assessment.vue`
  - 月度筛查列表
  - 轻量化选择题
  - 提交后生成个人筛查报告
  - 风险等级：低风险、中风险、高风险
- 个性化心理干预：后端 `intervention_plans` 表
  - 低风险：自助工具、正念、呼吸、压力疏导
  - 中风险：专业咨询入口、岗位沟通、状态追踪
  - 高风险：预警对接、专业援助、内部关怀提示
- 职场适应指导：`pages/course/course.vue`
  - 时间管理、角色平衡、职场沟通、职业信心、母婴兼顾
  - 支持学习打卡和进度记录
- 全周期状态追踪：`pages/mood/mood.vue`
  - 每日情绪
  - 工作压力
  - 家庭压力
  - 备注记录
- 我的报告：`pages/profile/profile.vue`
  - 查看个人筛查历史
  - 查看风险等级、分数、摘要、建议

## 企业管理后台

- 后台布局：`admin-web/src/App.vue`
  - 左侧菜单栏
  - 右侧内容区
  - 登录入口与退出
- 账号与员工管理：
  - `admin_users`
  - `departments`
  - `employees`
- 心理健康监测与风险预警：
  - `screenings`
  - `screening_questions`
  - `screening_reports`
  - `intervention_plans`
- 服务数据统计与可视化：`admin-web/src/views/DashboardView.vue`
  - 员工总数
  - 高风险预警
  - 咨询预约量
  - 课程平均进度
  - 风险等级饼图
  - 部门平均压力柱状图
  - 情绪与压力趋势折线图
  - 服务使用情况横向柱状图
- 统计报表导出：`GET /api/dashboard/export`
  - 导出 CSV
  - 覆盖总体指标、风险等级分布、部门压力指数、服务使用情况
- 服务效果量化分析：
  - 可通过 `screening_reports`、`course_progress`、`appointments`、`mood_logs` 汇总
  - 数据已进入统计接口，后续可扩展为导出报表
- 个性化后台设置：
  - `system_settings`
  - `policies`
  - 后端 MinIO 配置在 `backend/src/main/resources/application.yml`

## 图片与文件

- 后台本地图片上传：`admin-web/src/views/UploadView.vue`
- 后端上传接口：`POST /api/upload`
- 后端图片代理：`GET /api/files?object=emotion-ai/...`
- 文件存储：MinIO，桶名 `deng`
- 上传记录表：`upload_files`

## 演示数据

`backend/src/main/resources/data.sql` 已为每张表生成 10 条演示数据。涉及展示的业务表都包含头像、封面、图片或图表 URL 字段。
