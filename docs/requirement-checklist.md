# 图片需求功能核对

## 员工端

| 图片需求 | 完成情况 | 对应位置 |
| --- | --- | --- |
| 企业统一开通、员工实名激活、手机号登录、密码设置 | 已完成 | `mobile-uniapp/src/pages/login/login.vue`、`mobile-uniapp/src/pages/profile/profile.vue`、`backend/src/main/java/com/danzi/emotion/controller/MobileController.java` |
| AI 情绪筛查、20-30 题、暂停续答、提交后生成个人报告 | 已完成 | `mobile-uniapp/src/pages/assessment/assessment.vue`、`screening_questions`、`screening_reports` |
| 筛查匿名化、个人报告仅本人可见 | 已完成 | 移动端报告入口；管理端统计只展示聚合、部门、工号等脱敏字段 |
| 低/中/高风险分级干预 | 已完成 | `intervention_plans`、`mobile-uniapp/src/pages/profile/report-detail.vue` |
| 轻度自助工具播放、收藏、打卡、状态记录 | 已完成 | `intervention_records`、报告详情执行记录、状态追踪页 |
| 中度 1 对 1 咨询入口、认证咨询师、视频/语音预约 | 已完成 | `mobile-uniapp/src/pages/consult/consult.vue`、`appointments` |
| 高风险预警、专业援助提示、线下机构引导、自主选择是否告知企业 | 已完成 | 筛查页高风险关怀开关、`care_followups`、`service_notifications` |
| 职场适应课程、短视频、图文解析、思维导图、小测、收藏、打卡 | 已完成 | `mobile-uniapp/src/pages/course/detail.vue`、`courses`、`course_progress`、`course_favorites`、`course_quiz_records` |
| 社群交流、专业辅导员定期答疑、主题分享 | 已完成基础闭环 | `mobile-uniapp/src/pages/community/community.vue`、`community_posts`；辅导员答疑可在后台社区内容中维护 |
| 我的状态、每日情绪、职场/家庭困扰、趋势图、成长轨迹 | 已完成 | `mobile-uniapp/src/pages/mood/mood.vue`、`mood_logs` |

## 企业管理端

| 图片需求 | 完成情况 | 对应位置 |
| --- | --- | --- |
| 分级权限子账号、HR/管理层账号 | 已完成 | `admin_users`、后台账号管理 |
| 批量/新增/停用员工、部门岗位分类、返岗日期管理 | 已完成 | `employees`、`departments`、后台 CRUD |
| 一键发起专属服务通知 | 已完成 | `admin-web/src/views/DashboardView.vue`、`service_notifications` |
| 心理健康监测、参与率/合格率/风险分布趋势 | 已完成 | `DashboardController`、`DashboardView.vue` |
| 高风险自动红色预警、仅显示部门和工号 | 已完成 | `care_followups`、管理端高风险关怀跟进表 |
| 及时提醒关怀人员跟进、避免过度干预 | 已完成 | 关怀跟进状态、同意告知企业字段、完成跟进操作 |
| 服务数据统计、柱状图/折线图/饼图、周/月/季度/年度筛选、导出 | 已完成 | 管理端统计页周期筛选与 `GET /api/dashboard/export` |
| 服务效果量化：留存率、绩效/幸福感/生产力/适配度对比 | 已完成 | `service_effect_metrics`、管理端效果量化表 |
| 个性化后台设置：筛查推送时间、风险阈值、企业政策/母婴福利/职场支持 | 已完成 | `system_settings`、`policies`、后台设置与政策内容 |

