insert into admin_users(id, username, password, real_name, role, phone, avatar_url, status) values
(1,'admin','123456','系统管理员','SUPER_ADMIN','13800000001','http://119.29.152.180:9000/deng/mock/avatar-admin-1.jpg','启用'),
(2,'hr01','123456','林佳','HR','13800000002','http://119.29.152.180:9000/deng/mock/avatar-admin-2.jpg','启用'),
(3,'hr02','123456','周宁','HR','13800000003','http://119.29.152.180:9000/deng/mock/avatar-admin-3.jpg','启用'),
(4,'manager01','123456','陈敏','MANAGER','13800000004','http://119.29.152.180:9000/deng/mock/avatar-admin-4.jpg','启用'),
(5,'manager02','123456','王珊','MANAGER','13800000005','http://119.29.152.180:9000/deng/mock/avatar-admin-5.jpg','启用'),
(6,'care01','123456','许岚','CARE','13800000006','http://119.29.152.180:9000/deng/mock/avatar-admin-6.jpg','启用'),
(7,'data01','123456','秦悦','DATA','13800000007','http://119.29.152.180:9000/deng/mock/avatar-admin-7.jpg','启用'),
(8,'dept01','123456','赵青','DEPT_ADMIN','13800000008','http://119.29.152.180:9000/deng/mock/avatar-admin-8.jpg','启用'),
(9,'dept02','123456','刘曼','DEPT_ADMIN','13800000009','http://119.29.152.180:9000/deng/mock/avatar-admin-9.jpg','启用'),
(10,'audit01','123456','宋禾','AUDITOR','13800000010','http://119.29.152.180:9000/deng/mock/avatar-admin-10.jpg','启用');

insert into departments(id, name, leader, description, image_url, status) values
(1,'人力资源部','林佳','负责员工关怀、账号激活与服务通知','http://119.29.152.180:9000/deng/mock/dept-1.jpg','启用'),
(2,'产研中心','陈敏','关注项目节奏、角色平衡与复工适应','http://119.29.152.180:9000/deng/mock/dept-2.jpg','启用'),
(3,'运营中心','王珊','关注排班压力与服务协同','http://119.29.152.180:9000/deng/mock/dept-3.jpg','启用'),
(4,'销售一部','赵青','关注客户沟通压力与目标变化','http://119.29.152.180:9000/deng/mock/dept-4.jpg','启用'),
(5,'销售二部','刘曼','关注出差安排与家庭支持','http://119.29.152.180:9000/deng/mock/dept-5.jpg','启用'),
(6,'财务部','沈音','关注月结周期与专注恢复','http://119.29.152.180:9000/deng/mock/dept-6.jpg','启用'),
(7,'法务合规部','唐晓','关注高压审阅与沟通边界','http://119.29.152.180:9000/deng/mock/dept-7.jpg','启用'),
(8,'品牌市场部','叶然','关注活动节奏与公众表达','http://119.29.152.180:9000/deng/mock/dept-8.jpg','启用'),
(9,'客户成功部','何晴','关注客户情绪承接与轮班','http://119.29.152.180:9000/deng/mock/dept-9.jpg','启用'),
(10,'行政支持部','苏瑶','关注空间支持与福利落地','http://119.29.152.180:9000/deng/mock/dept-10.jpg','启用');

insert into employees(id, name, employee_no, phone, department_id, position, return_work_date, avatar_url, risk_level, status) values
(1,'安然','E2026001','13900000001',1,'HRBP','2026-03-01','http://119.29.152.180:9000/deng/mock/employee-1.jpg','低风险','在职'),
(2,'白露','E2026002','13900000002',2,'产品经理','2026-03-08','http://119.29.152.180:9000/deng/mock/employee-2.jpg','中风险','在职'),
(3,'楚瑶','E2026003','13900000003',3,'运营主管','2026-03-15','http://119.29.152.180:9000/deng/mock/employee-3.jpg','高风险','在职'),
(4,'丁宁','E2026004','13900000004',4,'销售经理','2026-03-22','http://119.29.152.180:9000/deng/mock/employee-4.jpg','低风险','在职'),
(5,'方晴','E2026005','13900000005',5,'大客户经理','2026-03-29','http://119.29.152.180:9000/deng/mock/employee-5.jpg','中风险','在职'),
(6,'顾言','E2026006','13900000006',6,'会计','2026-04-05','http://119.29.152.180:9000/deng/mock/employee-6.jpg','低风险','在职'),
(7,'韩溪','E2026007','13900000007',7,'合规专员','2026-04-12','http://119.29.152.180:9000/deng/mock/employee-7.jpg','高风险','在职'),
(8,'姜禾','E2026008','13900000008',8,'品牌策划','2026-04-19','http://119.29.152.180:9000/deng/mock/employee-8.jpg','低风险','在职'),
(9,'李沐','E2026009','13900000009',9,'客户成功','2026-04-26','http://119.29.152.180:9000/deng/mock/employee-9.jpg','中风险','在职'),
(10,'孟夏','E2026010','13900000010',10,'行政专员','2026-05-03','http://119.29.152.180:9000/deng/mock/employee-10.jpg','低风险','在职');

insert into screenings(id, title, description, push_cycle, estimated_minutes, cover_url, status) values
(1,'月度情绪筛查','20-30 道轻量化选择题，评估情绪状态与潜在风险','每月',8,'http://119.29.152.180:9000/deng/mock/screening-1.jpg','启用'),
(2,'复岗适应测评','关注岗位节奏、角色冲突和支持感','返岗后第1周',6,'http://119.29.152.180:9000/deng/mock/screening-2.jpg','启用'),
(3,'焦虑风险快筛','识别焦虑、睡眠与身体化反应','每两周',5,'http://119.29.152.180:9000/deng/mock/screening-3.jpg','启用'),
(4,'角色平衡问卷','评估家庭、工作与自我照护平衡','每月',7,'http://119.29.152.180:9000/deng/mock/screening-4.jpg','启用'),
(5,'团队支持感调查','评估同事、直属上级与制度支持','每季度',6,'http://119.29.152.180:9000/deng/mock/screening-5.jpg','启用'),
(6,'睡眠与恢复力测评','关注睡眠质量、疲劳感和恢复方式','每月',5,'http://119.29.152.180:9000/deng/mock/screening-6.jpg','启用'),
(7,'工作压力指数','评估负荷、控制感与冲突场景','每月',6,'http://119.29.152.180:9000/deng/mock/screening-7.jpg','启用'),
(8,'职业信心量表','评估能力感、目标感和沟通信心','每季度',6,'http://119.29.152.180:9000/deng/mock/screening-8.jpg','启用'),
(9,'关怀跟进复测','用于干预后状态跟踪','干预后7天',5,'http://119.29.152.180:9000/deng/mock/screening-9.jpg','启用'),
(10,'年度心理画像','企业年度匿名汇总分析入口','每年',10,'http://119.29.152.180:9000/deng/mock/screening-10.jpg','启用');

insert into screening_questions(id, screening_id, content, dimension, sort_no, image_url, status) values
(1,1,'过去一周，我能较稳定地完成日常工作安排','情绪稳定',1,'http://119.29.152.180:9000/deng/mock/question-1.jpg','启用'),
(2,1,'我能在需要时向同事或家人表达支持需求','支持系统',2,'http://119.29.152.180:9000/deng/mock/question-2.jpg','启用'),
(3,2,'返岗后的任务节奏在我可承受范围内','职场适应',1,'http://119.29.152.180:9000/deng/mock/question-3.jpg','启用'),
(4,2,'我清楚当前岗位的优先级和阶段目标','职业信心',2,'http://119.29.152.180:9000/deng/mock/question-4.jpg','启用'),
(5,3,'我最近容易因小事紧张或担心','焦虑风险',1,'http://119.29.152.180:9000/deng/mock/question-5.jpg','启用'),
(6,4,'我能为自己保留短暂休息和恢复时间','角色平衡',1,'http://119.29.152.180:9000/deng/mock/question-6.jpg','启用'),
(7,5,'我的直属上级能理解返岗阶段的过渡需求','团队支持',1,'http://119.29.152.180:9000/deng/mock/question-7.jpg','启用'),
(8,6,'我最近的睡眠能支持第二天的工作状态','恢复力',1,'http://119.29.152.180:9000/deng/mock/question-8.jpg','启用'),
(9,7,'我能掌控工作任务的推进节奏','工作压力',1,'http://119.29.152.180:9000/deng/mock/question-9.jpg','启用'),
(10,8,'我对重新承担核心任务有信心','职业信心',1,'http://119.29.152.180:9000/deng/mock/question-10.jpg','启用');

insert into screening_reports(id, employee_id, screening_id, score, risk_level, summary, suggestion, chart_image_url, created_at) values
(1,1,1,42,'低风险','整体情绪稳定，支持感较好','继续保持状态记录和课程学习','http://119.29.152.180:9000/deng/mock/report-1.jpg','2026-05-01 09:00:00'),
(2,2,2,64,'中风险','岗位节奏适应中，存在轻度压力','建议使用呼吸放松工具并预约一次辅导','http://119.29.152.180:9000/deng/mock/report-2.jpg','2026-05-02 09:00:00'),
(3,3,3,86,'高风险','焦虑和睡眠困扰较明显','建议专业咨询并同步内部关怀提醒','http://119.29.152.180:9000/deng/mock/report-3.jpg','2026-05-03 09:00:00'),
(4,4,4,38,'低风险','角色平衡较好','推荐职业信心课程','http://119.29.152.180:9000/deng/mock/report-4.jpg','2026-05-04 09:00:00'),
(5,5,5,68,'中风险','团队支持感一般','建议 HR 跟进岗位沟通','http://119.29.152.180:9000/deng/mock/report-5.jpg','2026-05-05 09:00:00'),
(6,6,6,44,'低风险','睡眠恢复基本正常','继续记录变化趋势','http://119.29.152.180:9000/deng/mock/report-6.jpg','2026-05-06 09:00:00'),
(7,7,7,91,'高风险','工作压力和角色冲突偏高','建议红色预警对接专业支持','http://119.29.152.180:9000/deng/mock/report-7.jpg','2026-05-07 09:00:00'),
(8,8,8,46,'低风险','职业信心恢复良好','推荐进阶沟通课程','http://119.29.152.180:9000/deng/mock/report-8.jpg','2026-05-08 09:00:00'),
(9,9,9,62,'中风险','干预后状态有所改善','建议持续一周状态追踪','http://119.29.152.180:9000/deng/mock/report-9.jpg','2026-05-09 09:00:00'),
(10,10,10,35,'低风险','年度画像显示状态平稳','保持日常自助工具使用','http://119.29.152.180:9000/deng/mock/report-10.jpg','2026-05-10 09:00:00');

insert into intervention_plans(id, risk_level, title, content, action_type, cover_url, status) values
(1,'低风险','五分钟正念练习','每日一次呼吸觉察，记录练习前后状态','自助工具','http://119.29.152.180:9000/deng/mock/intervention-1.jpg','启用'),
(2,'低风险','情绪调节冥想','提供短音频和收藏打卡','自助工具','http://119.29.152.180:9000/deng/mock/intervention-2.jpg','启用'),
(3,'低风险','压力疏导小技巧','结合碎片时间完成放松动作','自助工具','http://119.29.152.180:9000/deng/mock/intervention-3.jpg','启用'),
(4,'中风险','一对一咨询入口','员工自主选择认证咨询师和时间','专业咨询','http://119.29.152.180:9000/deng/mock/intervention-4.jpg','启用'),
(5,'中风险','岗位沟通清单','辅助员工整理返岗阶段沟通重点','职场指导','http://119.29.152.180:9000/deng/mock/intervention-5.jpg','启用'),
(6,'中风险','七日状态跟踪','连续记录情绪、压力和恢复情况','状态追踪','http://119.29.152.180:9000/deng/mock/intervention-6.jpg','启用'),
(7,'高风险','专业心理援助提醒','引导员工对接专业心理援助资源','预警对接','http://119.29.152.180:9000/deng/mock/intervention-7.jpg','启用'),
(8,'高风险','线下医疗机构建议','提供线下支持信息，由员工自主选择','预警对接','http://119.29.152.180:9000/deng/mock/intervention-8.jpg','启用'),
(9,'高风险','内部关怀跟进','仅向授权关怀人员展示部门和工号','关怀提醒','http://119.29.152.180:9000/deng/mock/intervention-9.jpg','启用'),
(10,'中风险','家庭支持计划','帮助员工梳理家庭协作与休息安排','角色平衡','http://119.29.152.180:9000/deng/mock/intervention-10.jpg','启用');

insert into courses(id, title, category, duration_minutes, summary, cover_url, status) values
(1,'复岗第一周节奏管理','时间管理',8,'帮助员工拆解返岗初期任务节奏','http://119.29.152.180:9000/deng/mock/course-1.jpg','上架'),
(2,'角色平衡沟通课','角色平衡',7,'家庭与工作边界沟通模板','http://119.29.152.180:9000/deng/mock/course-2.jpg','上架'),
(3,'职业信心重建','职业信心',6,'用小目标恢复工作确定感','http://119.29.152.180:9000/deng/mock/course-3.jpg','上架'),
(4,'压力识别与调节','情绪调节',8,'识别压力信号并使用短练习','http://119.29.152.180:9000/deng/mock/course-4.jpg','上架'),
(5,'高效求助与反馈','职场沟通',5,'向上沟通和同事协作话术','http://119.29.152.180:9000/deng/mock/course-5.jpg','上架'),
(6,'睡眠恢复微课','恢复力',6,'建立可持续睡前流程','http://119.29.152.180:9000/deng/mock/course-6.jpg','上架'),
(7,'母婴兼顾技巧','生活支持',8,'拆解育儿与工作协同场景','http://119.29.152.180:9000/deng/mock/course-7.jpg','上架'),
(8,'焦虑时刻的呼吸法','自助工具',3,'三分钟呼吸放松练习','http://119.29.152.180:9000/deng/mock/course-8.jpg','上架'),
(9,'团队支持共创','团队协作',7,'建立部门支持语言和行动清单','http://119.29.152.180:9000/deng/mock/course-9.jpg','上架'),
(10,'年度心理成长回顾','成长轨迹',10,'回看全年状态变化并制定目标','http://119.29.152.180:9000/deng/mock/course-10.jpg','上架');

insert into course_progress(id, employee_id, course_id, progress, completed, last_viewed_at, image_url) values
(1,1,1,100,1,'2026-05-01 18:00:00','http://119.29.152.180:9000/deng/mock/progress-1.jpg'),
(2,2,2,60,0,'2026-05-02 18:00:00','http://119.29.152.180:9000/deng/mock/progress-2.jpg'),
(3,3,3,45,0,'2026-05-03 18:00:00','http://119.29.152.180:9000/deng/mock/progress-3.jpg'),
(4,4,4,100,1,'2026-05-04 18:00:00','http://119.29.152.180:9000/deng/mock/progress-4.jpg'),
(5,5,5,70,0,'2026-05-05 18:00:00','http://119.29.152.180:9000/deng/mock/progress-5.jpg'),
(6,6,6,90,0,'2026-05-06 18:00:00','http://119.29.152.180:9000/deng/mock/progress-6.jpg'),
(7,7,7,35,0,'2026-05-07 18:00:00','http://119.29.152.180:9000/deng/mock/progress-7.jpg'),
(8,8,8,100,1,'2026-05-08 18:00:00','http://119.29.152.180:9000/deng/mock/progress-8.jpg'),
(9,9,9,65,0,'2026-05-09 18:00:00','http://119.29.152.180:9000/deng/mock/progress-9.jpg'),
(10,10,10,80,0,'2026-05-10 18:00:00','http://119.29.152.180:9000/deng/mock/progress-10.jpg');

insert into consultants(id, name, title, speciality, phone, avatar_url, status) values
(1,'许医生','国家二级心理咨询师','产后情绪、焦虑干预','13700000001','http://119.29.152.180:9000/deng/mock/consultant-1.jpg','可预约'),
(2,'罗老师','EAP 咨询顾问','职场适应、角色平衡','13700000002','http://119.29.152.180:9000/deng/mock/consultant-2.jpg','可预约'),
(3,'沈医生','心理治疗师','睡眠困扰、压力管理','13700000003','http://119.29.152.180:9000/deng/mock/consultant-3.jpg','可预约'),
(4,'唐老师','家庭关系咨询师','家庭支持、亲密沟通','13700000004','http://119.29.152.180:9000/deng/mock/consultant-4.jpg','可预约'),
(5,'叶老师','职业发展教练','职业信心、目标重建','13700000005','http://119.29.152.180:9000/deng/mock/consultant-5.jpg','可预约'),
(6,'何医生','精神科合作医生','高风险转介建议','13700000006','http://119.29.152.180:9000/deng/mock/consultant-6.jpg','可预约'),
(7,'秦老师','组织心理顾问','团队支持、管理沟通','13700000007','http://119.29.152.180:9000/deng/mock/consultant-7.jpg','可预约'),
(8,'宋老师','心理咨询师','情绪调节、正念练习','13700000008','http://119.29.152.180:9000/deng/mock/consultant-8.jpg','可预约'),
(9,'林医生','临床心理师','焦虑抑郁筛查','13700000009','http://119.29.152.180:9000/deng/mock/consultant-9.jpg','可预约'),
(10,'周老师','企业关怀顾问','福利政策与关怀方案','13700000010','http://119.29.152.180:9000/deng/mock/consultant-10.jpg','可预约');

insert into appointments(id, employee_id, consultant_id, appointment_time, method, status, notes, image_url) values
(1,1,1,'2026-05-15 10:00:00','视频','已确认','月度复盘','http://119.29.152.180:9000/deng/mock/appointment-1.jpg'),
(2,2,2,'2026-05-16 11:00:00','语音','待确认','返岗沟通','http://119.29.152.180:9000/deng/mock/appointment-2.jpg'),
(3,3,3,'2026-05-17 14:00:00','视频','已确认','焦虑支持','http://119.29.152.180:9000/deng/mock/appointment-3.jpg'),
(4,4,4,'2026-05-18 15:00:00','语音','已完成','家庭协作','http://119.29.152.180:9000/deng/mock/appointment-4.jpg'),
(5,5,5,'2026-05-19 16:00:00','视频','待确认','职业信心','http://119.29.152.180:9000/deng/mock/appointment-5.jpg'),
(6,6,6,'2026-05-20 10:30:00','语音','已完成','睡眠恢复','http://119.29.152.180:9000/deng/mock/appointment-6.jpg'),
(7,7,7,'2026-05-21 11:30:00','视频','已确认','高风险跟进','http://119.29.152.180:9000/deng/mock/appointment-7.jpg'),
(8,8,8,'2026-05-22 13:30:00','语音','待确认','正念练习','http://119.29.152.180:9000/deng/mock/appointment-8.jpg'),
(9,9,9,'2026-05-23 14:30:00','视频','已完成','复测咨询','http://119.29.152.180:9000/deng/mock/appointment-9.jpg'),
(10,10,10,'2026-05-24 15:30:00','语音','已确认','福利政策','http://119.29.152.180:9000/deng/mock/appointment-10.jpg');

insert into mood_logs(id, employee_id, mood_score, work_stress, family_stress, note, image_url, logged_at) values
(1,1,82,36,30,'今天工作节奏稳定','http://119.29.152.180:9000/deng/mock/mood-1.jpg','2026-05-01 20:00:00'),
(2,2,68,55,42,'下午有些疲惫','http://119.29.152.180:9000/deng/mock/mood-2.jpg','2026-05-02 20:00:00'),
(3,3,45,82,70,'睡眠不足，容易紧张','http://119.29.152.180:9000/deng/mock/mood-3.jpg','2026-05-03 20:00:00'),
(4,4,78,40,35,'完成了重点沟通','http://119.29.152.180:9000/deng/mock/mood-4.jpg','2026-05-04 20:00:00'),
(5,5,62,64,48,'客户会议后压力上升','http://119.29.152.180:9000/deng/mock/mood-5.jpg','2026-05-05 20:00:00'),
(6,6,75,50,32,'月结任务可控','http://119.29.152.180:9000/deng/mock/mood-6.jpg','2026-05-06 20:00:00'),
(7,7,39,88,66,'担心工作遗漏','http://119.29.152.180:9000/deng/mock/mood-7.jpg','2026-05-07 20:00:00'),
(8,8,80,38,36,'创意讨论顺利','http://119.29.152.180:9000/deng/mock/mood-8.jpg','2026-05-08 20:00:00'),
(9,9,66,58,50,'需要更多休息','http://119.29.152.180:9000/deng/mock/mood-9.jpg','2026-05-09 20:00:00'),
(10,10,84,34,28,'今天状态很好','http://119.29.152.180:9000/deng/mock/mood-10.jpg','2026-05-10 20:00:00');

insert into policies(id, title, category, content, cover_url, status) values
(1,'弹性返岗安排','企业政策','支持返岗前两周弹性工时与任务缓冲','http://119.29.152.180:9000/deng/mock/policy-1.jpg','发布'),
(2,'母婴室使用说明','母婴福利','提供母婴室预约、储物与隐私保护规则','http://119.29.152.180:9000/deng/mock/policy-2.jpg','发布'),
(3,'心理咨询补贴','心理福利','企业提供认证咨询补贴额度','http://119.29.152.180:9000/deng/mock/policy-3.jpg','发布'),
(4,'岗位沟通机制','职场支持','返岗员工可申请阶段性目标沟通','http://119.29.152.180:9000/deng/mock/policy-4.jpg','发布'),
(5,'远程办公申请','企业政策','支持符合条件员工短期远程办公','http://119.29.152.180:9000/deng/mock/policy-5.jpg','发布'),
(6,'哺乳期保护','合规制度','落实相关劳动保护与休息安排','http://119.29.152.180:9000/deng/mock/policy-6.jpg','发布'),
(7,'员工关怀热线','关怀服务','提供内部关怀人员联系通道','http://119.29.152.180:9000/deng/mock/policy-7.jpg','发布'),
(8,'年度健康计划','健康管理','提供年度心理健康和身体健康活动','http://119.29.152.180:9000/deng/mock/policy-8.jpg','发布'),
(9,'团队支持倡议','文化建设','鼓励团队建立友好协作氛围','http://119.29.152.180:9000/deng/mock/policy-9.jpg','发布'),
(10,'隐私保护说明','数据安全','筛查结果仅员工本人可见，管理端展示脱敏统计','http://119.29.152.180:9000/deng/mock/policy-10.jpg','发布');

insert into system_settings(id, setting_key, setting_value, description, image_url) values
(1,'screening_push_time','09:30','AI 筛查默认推送时间','http://119.29.152.180:9000/deng/mock/setting-1.jpg'),
(2,'risk_warning_threshold','80','高风险预警阈值','http://119.29.152.180:9000/deng/mock/setting-2.jpg'),
(3,'medium_risk_threshold','55','中风险阈值','http://119.29.152.180:9000/deng/mock/setting-3.jpg'),
(4,'privacy_mode','anonymous','管理端默认脱敏展示','http://119.29.152.180:9000/deng/mock/setting-4.jpg'),
(5,'consultation_duration','45','默认咨询时长分钟','http://119.29.152.180:9000/deng/mock/setting-5.jpg'),
(6,'course_remind_days','3','课程未完成提醒间隔','http://119.29.152.180:9000/deng/mock/setting-6.jpg'),
(7,'report_export_enabled','true','允许后台导出统计报表','http://119.29.152.180:9000/deng/mock/setting-7.jpg'),
(8,'care_follow_days','7','高风险关怀跟进天数','http://119.29.152.180:9000/deng/mock/setting-8.jpg'),
(9,'minio_bucket','deng','文件上传桶名','http://119.29.152.180:9000/deng/mock/setting-9.jpg'),
(10,'company_name','产后返岗情绪支持平台','系统显示名称','http://119.29.152.180:9000/deng/mock/setting-10.jpg');

insert into upload_files(id, file_name, object_name, url, content_type, size_bytes, biz_type) values
(1,'avatar-1.jpg','mock/avatar-1.jpg','http://119.29.152.180:9000/deng/mock/avatar-1.jpg','image/jpeg',118000,'avatar'),
(2,'course-1.jpg','mock/course-1.jpg','http://119.29.152.180:9000/deng/mock/course-1.jpg','image/jpeg',126000,'course'),
(3,'report-1.jpg','mock/report-1.jpg','http://119.29.152.180:9000/deng/mock/report-1.jpg','image/jpeg',98000,'report'),
(4,'policy-1.jpg','mock/policy-1.jpg','http://119.29.152.180:9000/deng/mock/policy-1.jpg','image/jpeg',106000,'policy'),
(5,'consultant-1.jpg','mock/consultant-1.jpg','http://119.29.152.180:9000/deng/mock/consultant-1.jpg','image/jpeg',112000,'consultant'),
(6,'dept-1.jpg','mock/dept-1.jpg','http://119.29.152.180:9000/deng/mock/dept-1.jpg','image/jpeg',121000,'department'),
(7,'mood-1.jpg','mock/mood-1.jpg','http://119.29.152.180:9000/deng/mock/mood-1.jpg','image/jpeg',88000,'mood'),
(8,'screening-1.jpg','mock/screening-1.jpg','http://119.29.152.180:9000/deng/mock/screening-1.jpg','image/jpeg',130000,'screening'),
(9,'intervention-1.jpg','mock/intervention-1.jpg','http://119.29.152.180:9000/deng/mock/intervention-1.jpg','image/jpeg',119000,'intervention'),
(10,'setting-1.jpg','mock/setting-1.jpg','http://119.29.152.180:9000/deng/mock/setting-1.jpg','image/jpeg',76000,'setting');
