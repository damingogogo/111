drop table if exists upload_files;
drop table if exists system_settings;
drop table if exists policies;
drop table if exists mood_logs;
drop table if exists appointments;
drop table if exists consultants;
drop table if exists course_progress;
drop table if exists courses;
drop table if exists intervention_plans;
drop table if exists screening_reports;
drop table if exists screening_questions;
drop table if exists screenings;
drop table if exists employees;
drop table if exists departments;
drop table if exists admin_users;

create table admin_users (
  id bigint primary key auto_increment,
  username varchar(64) not null unique,
  password varchar(128) not null,
  real_name varchar(64) not null,
  role varchar(32) not null,
  phone varchar(32),
  avatar_url varchar(512),
  status varchar(16) not null default '启用',
  created_at datetime not null default current_timestamp
);

create table departments (
  id bigint primary key auto_increment,
  name varchar(128) not null,
  leader varchar(64),
  description varchar(512),
  image_url varchar(512),
  status varchar(16) not null default '启用',
  created_at datetime not null default current_timestamp
);

create table employees (
  id bigint primary key auto_increment,
  name varchar(64) not null,
  employee_no varchar(64) not null unique,
  phone varchar(32),
  department_id bigint,
  position varchar(128),
  return_work_date date,
  avatar_url varchar(512),
  risk_level varchar(16) not null default '低风险',
  status varchar(16) not null default '在职',
  created_at datetime not null default current_timestamp,
  constraint fk_employee_department foreign key (department_id) references departments(id)
);

create table screenings (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  description varchar(512),
  push_cycle varchar(64),
  estimated_minutes int,
  cover_url varchar(512),
  status varchar(16) not null default '启用',
  created_at datetime not null default current_timestamp
);

create table screening_questions (
  id bigint primary key auto_increment,
  screening_id bigint not null,
  content varchar(512) not null,
  dimension varchar(64),
  sort_no int not null,
  image_url varchar(512),
  status varchar(16) not null default '启用',
  constraint fk_question_screening foreign key (screening_id) references screenings(id)
);

create table screening_reports (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  screening_id bigint not null,
  score int not null,
  risk_level varchar(16) not null,
  summary varchar(512),
  suggestion varchar(512),
  chart_image_url varchar(512),
  created_at datetime not null default current_timestamp,
  constraint fk_report_employee foreign key (employee_id) references employees(id),
  constraint fk_report_screening foreign key (screening_id) references screenings(id)
);

create table intervention_plans (
  id bigint primary key auto_increment,
  risk_level varchar(16) not null,
  title varchar(128) not null,
  content text,
  action_type varchar(64),
  cover_url varchar(512),
  status varchar(16) not null default '启用',
  created_at datetime not null default current_timestamp
);

create table courses (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  category varchar(64),
  duration_minutes int,
  summary varchar(512),
  cover_url varchar(512),
  status varchar(16) not null default '上架',
  created_at datetime not null default current_timestamp
);

create table course_progress (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  course_id bigint not null,
  progress int not null default 0,
  completed tinyint(1) not null default 0,
  last_viewed_at datetime,
  image_url varchar(512),
  unique key uk_employee_course(employee_id, course_id),
  constraint fk_progress_employee foreign key (employee_id) references employees(id),
  constraint fk_progress_course foreign key (course_id) references courses(id)
);

create table consultants (
  id bigint primary key auto_increment,
  name varchar(64) not null,
  title varchar(128),
  speciality varchar(256),
  phone varchar(32),
  avatar_url varchar(512),
  status varchar(16) not null default '可预约',
  created_at datetime not null default current_timestamp
);

create table appointments (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  consultant_id bigint not null,
  appointment_time datetime not null,
  method varchar(32) not null,
  status varchar(32) not null default '待确认',
  notes varchar(512),
  image_url varchar(512),
  created_at datetime not null default current_timestamp,
  constraint fk_appointment_employee foreign key (employee_id) references employees(id),
  constraint fk_appointment_consultant foreign key (consultant_id) references consultants(id)
);

create table mood_logs (
  id bigint primary key auto_increment,
  employee_id bigint not null,
  mood_score int not null,
  work_stress int not null,
  family_stress int not null,
  note varchar(512),
  image_url varchar(512),
  logged_at datetime not null,
  constraint fk_mood_employee foreign key (employee_id) references employees(id)
);

create table policies (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  category varchar(64),
  content text,
  cover_url varchar(512),
  status varchar(16) not null default '发布',
  created_at datetime not null default current_timestamp
);

create table system_settings (
  id bigint primary key auto_increment,
  setting_key varchar(128) not null unique,
  setting_value varchar(512) not null,
  description varchar(512),
  image_url varchar(512),
  created_at datetime not null default current_timestamp
);

create table upload_files (
  id bigint primary key auto_increment,
  file_name varchar(255) not null,
  object_name varchar(512) not null,
  url varchar(512) not null,
  content_type varchar(128),
  size_bytes bigint,
  biz_type varchar(64),
  created_at datetime not null default current_timestamp
);
