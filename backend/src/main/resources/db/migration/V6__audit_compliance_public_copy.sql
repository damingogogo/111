update screening_reports
set summary = replace(summary, 'AI 已生成', '已形成'),
    suggestion = replace(replace(replace(suggestion, '预约专业咨询', '查看心理支持资源'), '1 对 1 咨询入口', '心理支持资源'), '一次辅导', '支持资源')
where summary like '%AI%'
   or suggestion like '%预约%'
   or suggestion like '%咨询%'
   or suggestion like '%辅导%';

update screening_questions
set content = replace(content, '心理咨询', '心理支持')
where content like '%心理咨询%';

update intervention_plans
set title = '心理支持资源',
    content = '查看平台整理的心理支持方式、自助练习和线下求助路径。',
    action_type = '心理支持'
where title in ('一对一咨询入口', '专业心理援助提醒')
   or action_type in ('专业咨询', '预警对接');

update intervention_records
set action = '查看'
where action = '预约';

update service_notifications
set title = replace(title, 'AI ', ''),
    content = replace(content, 'AI ', '')
where title like '%AI%'
   or content like '%AI%';

update policies
set title = '心理支持资源说明',
    content = '企业提供心理支持资源说明，可按内部公开流程了解。'
where title = '心理咨询补贴';

update policies
set content = replace(content, '预约、', '')
where content like '%预约%';
