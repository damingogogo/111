alter table screening_reports
  add column answers_json text,
  add column risk_points varchar(512),
  add column enterprise_notice_consent tinyint(1) not null default 0;

alter table courses
  add column video_url varchar(512),
  add column mind_map text,
  add column quiz_json text;

create table if not exists intervention_records (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  plan_id bigint not null,
  report_id bigint,
  action varchar(32) not null,
  state_note varchar(512),
  created_at datetime not null default current_timestamp,
  constraint fk_intervention_record_employee foreign key (employee_id) references employees(id),
  constraint fk_intervention_record_plan foreign key (plan_id) references intervention_plans(id),
  constraint fk_intervention_record_report foreign key (report_id) references screening_reports(id)
);

create table if not exists course_favorites (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  course_id bigint not null,
  created_at datetime not null default current_timestamp,
  unique key uk_course_favorite(employee_id, course_id),
  constraint fk_course_favorite_employee foreign key (employee_id) references employees(id),
  constraint fk_course_favorite_course foreign key (course_id) references courses(id)
);

create table if not exists course_quiz_records (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  course_id bigint not null,
  score int not null,
  answers_json text,
  created_at datetime not null default current_timestamp,
  constraint fk_course_quiz_employee foreign key (employee_id) references employees(id),
  constraint fk_course_quiz_course foreign key (course_id) references courses(id)
);

create table if not exists community_posts (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  category varchar(64),
  title varchar(128) not null,
  content text,
  anonymous tinyint(1) not null default 1,
  reply_count int not null default 0,
  status varchar(16) not null default '发布',
  created_at datetime not null default current_timestamp,
  constraint fk_community_post_employee foreign key (employee_id) references employees(id)
);

create table if not exists care_followups (
  id bigint primary key auto_increment,
  report_id bigint not null,
  employee_id bigint not null,
  department_id bigint,
  employee_no varchar(64),
  risk_level varchar(16) not null,
  consent_to_notify tinyint(1) not null default 0,
  status varchar(32) not null default '待跟进',
  follow_note varchar(512),
  assigned_role varchar(64) not null default '关怀专员',
  created_at datetime not null default current_timestamp,
  followed_at datetime,
  constraint fk_care_followup_report foreign key (report_id) references screening_reports(id),
  constraint fk_care_followup_employee foreign key (employee_id) references employees(id),
  constraint fk_care_followup_department foreign key (department_id) references departments(id)
);

create table if not exists service_notifications (
  id bigint primary key auto_increment,
  target_type varchar(32) not null,
  target_id bigint,
  title varchar(128) not null,
  content varchar(512),
  channel varchar(64) not null default '企业内部通知',
  status varchar(16) not null default '未读',
  created_at datetime not null default current_timestamp
);

create table if not exists service_effect_metrics (
  id bigint primary key auto_increment,
  period_label varchar(64) not null,
  department_id bigint,
  retained_count int not null default 0,
  employee_count int not null default 0,
  happiness_before decimal(5,2) not null default 0,
  happiness_after decimal(5,2) not null default 0,
  productivity_before decimal(5,2) not null default 0,
  productivity_after decimal(5,2) not null default 0,
  fit_before decimal(5,2) not null default 0,
  fit_after decimal(5,2) not null default 0,
  encrypted_file_url varchar(512),
  report_summary varchar(512),
  created_at datetime not null default current_timestamp,
  constraint fk_effect_metric_department foreign key (department_id) references departments(id)
);

update courses
set
  video_url = concat('http://119.29.152.180:9000/deng/mock/video-course-', id, '.mp4'),
  mind_map = case id
    when 1 then '返岗节奏>任务拆解>优先级>每日复盘'
    when 2 then '角色平衡>边界沟通>家庭协作>自我照护'
    when 3 then '职业信心>小目标>正向反馈>能力恢复'
    when 4 then '压力识别>身体信号>呼吸练习>求助资源'
    else '课程目标>核心方法>场景练习>行动打卡'
  end,
  quiz_json = '[{"question":"本课程建议把行动拆成多长时间内可完成的小步骤？","options":[{"label":"15 分钟内","score":100},{"label":"半天以上","score":0},{"label":"完全不拆分","score":0}]},{"question":"返岗适应遇到压力时，更推荐的做法是？","options":[{"label":"记录状态并寻求合适支持","score":100},{"label":"持续压抑不沟通","score":0},{"label":"立即否定自己的能力","score":0}]}]'
where video_url is null or video_url = '';

insert ignore into intervention_records(id, employee_id, plan_id, report_id, action, state_note, created_at) values
(1,1,1,1,'打卡','完成 5 分钟呼吸练习，状态更平稳','2026-05-02 08:30:00'),
(2,2,4,2,'预约','已查看咨询入口并准备预约','2026-05-03 12:20:00'),
(3,3,7,3,'查看','已查看专业心理援助提示','2026-05-04 10:10:00'),
(4,4,3,4,'收藏','收藏压力疏导小技巧','2026-05-05 19:00:00'),
(5,5,5,5,'打卡','整理了岗位沟通清单','2026-05-06 17:40:00');

insert ignore into course_favorites(id, employee_id, course_id, created_at) values
(1,1,1,'2026-05-01 18:10:00'),
(2,2,2,'2026-05-02 18:10:00'),
(3,3,4,'2026-05-03 18:10:00'),
(4,4,8,'2026-05-04 18:10:00'),
(5,5,5,'2026-05-05 18:10:00');

insert ignore into course_quiz_records(id, employee_id, course_id, score, answers_json, created_at) values
(1,1,1,100,'[0,0]','2026-05-01 18:20:00'),
(2,2,2,50,'[0,1]','2026-05-02 18:20:00'),
(3,4,4,100,'[0,0]','2026-05-04 18:20:00');

insert ignore into community_posts(id, employee_id, category, title, content, anonymous, reply_count, status, created_at) values
(1,1,'时间管理','第一周怎么安排比较不累？','我把每天最重要的工作压到上午，下午留给沟通和缓冲。',1,3,'发布','2026-05-02 09:30:00'),
(2,2,'角色平衡','和家人协商接送安排的小方法','提前一晚把第二天的接送、加班可能性说清楚，会轻松很多。',1,5,'发布','2026-05-03 20:00:00'),
(3,3,'情绪调节','焦虑时先做三分钟呼吸','先做呼吸再处理消息，确实能减少一点慌乱。',1,2,'发布','2026-05-04 21:00:00');

insert ignore into care_followups(id, report_id, employee_id, department_id, employee_no, risk_level, consent_to_notify, status, follow_note, assigned_role, created_at) values
(1,3,3,3,'E2026003','高风险',1,'待跟进','系统已生成高风险提示，仅展示部门与工号','关怀专员','2026-05-03 09:05:00'),
(2,7,7,7,'E2026007','高风险',0,'观察中','员工暂未同意企业通知，仅保留脱敏预警','关怀专员','2026-05-07 09:05:00');

insert ignore into service_notifications(id, target_type, target_id, title, content, channel, status, created_at) values
(1,'employee',1,'月度情绪筛查提醒','本月 AI 情绪筛查已开放，预计 5-8 分钟完成。','小程序通知','未读','2026-05-01 09:30:00'),
(2,'department',3,'高风险脱敏关怀提示','运营中心出现高风险预警，请授权关怀人员按制度跟进。','企业内部通知','未读','2026-05-03 09:10:00'),
(3,'all',null,'返岗支持课程更新','新增职场沟通和角色平衡课程，欢迎按需学习。','企业内部通知','未读','2026-05-05 09:00:00');

insert ignore into service_effect_metrics(id, period_label, department_id, retained_count, employee_count, happiness_before, happiness_after, productivity_before, productivity_after, fit_before, fit_after, encrypted_file_url, report_summary, created_at) values
(1,'2026-Q2',1,18,20,68.50,78.20,72.00,81.50,70.00,82.00,'http://119.29.152.180:9000/deng/mock/effect-hr.enc','HR 返岗员工幸福感、生产力和岗位适配度均有提升','2026-05-20 10:00:00'),
(2,'2026-Q2',2,42,48,62.00,73.50,67.50,76.00,65.00,74.50,'http://119.29.152.180:9000/deng/mock/effect-rd.enc','产研中心课程学习完成率提升后，岗位适配改善明显','2026-05-20 10:00:00'),
(3,'2026-Q2',3,28,34,58.00,69.00,61.50,70.50,60.00,68.00,'http://119.29.152.180:9000/deng/mock/effect-ops.enc','运营中心仍需加强高压排班下的持续关怀','2026-05-20 10:00:00');
