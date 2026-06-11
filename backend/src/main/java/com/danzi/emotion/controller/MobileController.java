package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MobileController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/activate")
    public ApiResponse<Map<String, Object>> activate(@RequestBody Map<String, Object> body) {
        String phone = String.valueOf(body.getOrDefault("phone", "")).trim();
        String code = String.valueOf(body.getOrDefault("code", "")).trim();
        if (phone.isBlank()) {
            return ApiResponse.fail("请输入手机号");
        }
        if (!"123456".equals(code)) {
            return ApiResponse.fail("验证码错误，演示验证码为 123456");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                select e.*, d.name department_name
                from employees e
                left join departments d on e.department_id = d.id
                where e.phone = ?
                limit 1
                """, phone);
        if (rows.isEmpty()) {
            return ApiResponse.fail("未找到该手机号对应的员工账号，请联系 HR 录入员工信息");
        }
        return ApiResponse.ok(rows.get(0));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 0)));
        String oldPassword = String.valueOf(body.getOrDefault("oldPassword", "")).trim();
        String newPassword = String.valueOf(body.getOrDefault("newPassword", "")).trim();
        if (employeeId <= 0) {
            return ApiResponse.fail("请先登录");
        }
        if (oldPassword.isBlank()) {
            return ApiResponse.fail("请输入原密码");
        }
        if (newPassword.length() < 6) {
            return ApiResponse.fail("新密码至少 6 位");
        }
        Map<String, Object> employee = first("select login_password from employees where id = ?", employeeId);
        if (employee.isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        if (!oldPassword.equals(String.valueOf(employee.getOrDefault("login_password", "")))) {
            return ApiResponse.fail("原密码不正确");
        }
        jdbcTemplate.update("update employees set login_password = ? where id = ?", newPassword, employeeId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(@RequestParam(defaultValue = "1") long employeeId) {
        Map<String, Object> employee = first("""
                select e.*, d.name department_name from employees e left join departments d on e.department_id = d.id where e.id = ?
                """, employeeId);
        if (employee.isEmpty()) {
            return ApiResponse.fail("员工账号不存在，请重新登录");
        }
        return ApiResponse.ok(Map.of(
                "employee", employee,
                "latestReport", first("select * from screening_reports where employee_id = ? order by created_at desc limit 1", employeeId),
                "latestMood", first("select * from mood_logs where employee_id = ? order by logged_at desc limit 1", employeeId),
                "nextAppointment", first("""
                        select a.*, c.name consultant_name
                        from appointments a
                        left join consultants c on a.consultant_id = c.id
                        where a.employee_id = ? and a.status <> '已取消'
                        order by a.appointment_time asc
                        limit 1
                        """, employeeId),
                "courses", courseRows(employeeId, 4),
                "notifications", notifications(employee),
                "communityPosts", jdbcTemplate.queryForList("""
                        select p.*, if(p.anonymous = 1, '匿名同事', e.name) author_name
                        from community_posts p
                        left join employees e on e.id = p.employee_id
                        where p.status = '发布'
                        order by p.created_at desc
                        limit 3
                        """),
                "policies", jdbcTemplate.queryForList("select * from policies where status = '发布' order by id desc limit 3")
        ));
    }

    @GetMapping("/screenings")
    public ApiResponse<List<Map<String, Object>>> screenings() {
        return ApiResponse.ok(jdbcTemplate.queryForList("select * from screenings where status = '启用' order by id desc"));
    }

    @GetMapping("/screenings/{id}/questions")
    public ApiResponse<List<Map<String, Object>>> questions(@PathVariable long id) {
        return ApiResponse.ok(jdbcTemplate.queryForList("select * from screening_questions where screening_id = ? and status = '启用' order by sort_no", id));
    }

    @PostMapping("/screenings/{id}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable long id, @RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 1)));
        int score = Integer.parseInt(String.valueOf(body.getOrDefault("score", 70)));
        if (first("select id from employees where id = ?", employeeId).isEmpty()) {
            return ApiResponse.fail("员工账号不存在，请重新登录");
        }
        if (first("select id from screenings where id = ? and status = '启用'", id).isEmpty()) {
            return ApiResponse.fail("筛查不存在或已停用");
        }
        String risk = score >= 80 ? "高风险" : score >= 55 ? "中风险" : "低风险";
        boolean consent = Boolean.parseBoolean(String.valueOf(body.getOrDefault("enterpriseNoticeConsent", false)));
        String answersJson = toJson(body.getOrDefault("answers", Map.of()));
        String riskPoints = riskPoints(score);
        String suggestion = suggestionFor(risk);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into screening_reports(employee_id, screening_id, score, risk_level, summary, suggestion, risk_points, answers_json, enterprise_notice_consent, chart_image_url)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, employeeId);
            ps.setLong(2, id);
            ps.setInt(3, score);
            ps.setString(4, risk);
            ps.setString(5, "AI 已生成本次情绪筛查摘要，并完成匿名化风险评估");
            ps.setString(6, suggestion);
            ps.setString(7, riskPoints);
            ps.setString(8, answersJson);
            ps.setBoolean(9, consent);
            ps.setString(10, mockImage("report-new.jpg"));
            return ps;
        }, keyHolder);
        long reportId = keyHolder.getKey() == null ? 0 : keyHolder.getKey().longValue();
        jdbcTemplate.update("update employees set risk_level = ? where id = ?", risk, employeeId);
        if ("高风险".equals(risk)) {
            Map<String, Object> employee = first("select department_id, employee_no from employees where id = ?", employeeId);
            jdbcTemplate.update("""
                    insert into care_followups(report_id, employee_id, department_id, employee_no, risk_level, consent_to_notify, status, follow_note, assigned_role)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    reportId,
                    employeeId,
                    employee.get("department_id"),
                    employee.get("employee_no"),
                    risk,
                    consent,
                    consent ? "待跟进" : "观察中",
                    consent ? "员工同意接收企业关怀提示，仅展示部门与工号" : "员工暂未同意告知企业，保留脱敏预警记录",
                    "关怀专员");
            jdbcTemplate.update("""
                    insert into service_notifications(target_type, target_id, title, content, channel, status)
                    values (?, ?, ?, ?, ?, ?)
                    """,
                    "department",
                    employee.get("department_id"),
                    "高风险脱敏关怀提示",
                    "系统检测到高风险状态，请授权关怀人员按制度跟进。",
                    "企业内部通知",
                    "未读");
        }
        return ApiResponse.ok(first("select * from screening_reports where employee_id = ? order by id desc limit 1", employeeId));
    }

    @GetMapping("/courses")
    public ApiResponse<List<Map<String, Object>>> courses(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(courseRows(employeeId, 0));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<Map<String, Object>> course(@PathVariable long id, @RequestParam(defaultValue = "1") long employeeId) {
        Map<String, Object> course = first("select * from courses where id = ?", id);
        if (course.isEmpty()) {
            return ApiResponse.fail("课程不存在或已下架");
        }
        Map<String, Object> progress = first("select * from course_progress where employee_id = ? and course_id = ? limit 1", employeeId, id);
        Map<String, Object> favorite = first("select * from course_favorites where employee_id = ? and course_id = ? limit 1", employeeId, id);
        Map<String, Object> quiz = first("select * from course_quiz_records where employee_id = ? and course_id = ? order by id desc limit 1", employeeId, id);
        return ApiResponse.ok(Map.of("course", course, "progress", progress, "favorite", favorite, "latestQuiz", quiz));
    }

    @PostMapping("/course-progress")
    public ApiResponse<Void> courseProgress(@RequestBody Map<String, Object> body) {
        if (!hasEmployeeAndCourse(body)) {
            return ApiResponse.fail("员工或课程不存在");
        }
        jdbcTemplate.update("""
                insert into course_progress(employee_id, course_id, progress, completed, last_viewed_at, image_url)
                values (?, ?, ?, ?, now(), ?)
                on duplicate key update progress = values(progress), completed = values(completed), last_viewed_at = now(), image_url = values(image_url)
                """, body.get("employeeId"), body.get("courseId"), body.get("progress"), body.get("completed"), mockImage("progress.jpg"));
        return ApiResponse.ok(null);
    }

    @PostMapping("/course-favorites")
    public ApiResponse<Void> favoriteCourse(@RequestBody Map<String, Object> body) {
        if (!hasEmployeeAndCourse(body)) {
            return ApiResponse.fail("员工或课程不存在");
        }
        jdbcTemplate.update("""
                insert ignore into course_favorites(employee_id, course_id)
                values (?, ?)
                """, body.get("employeeId"), body.get("courseId"));
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/course-favorites")
    public ApiResponse<Void> deleteFavoriteCourse(@RequestParam long employeeId, @RequestParam long courseId) {
        jdbcTemplate.update("delete from course_favorites where employee_id = ? and course_id = ?", employeeId, courseId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/course-quiz")
    public ApiResponse<Map<String, Object>> courseQuiz(@RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 1)));
        long courseId = Long.parseLong(String.valueOf(body.getOrDefault("courseId", 0)));
        int score = Integer.parseInt(String.valueOf(body.getOrDefault("score", 0)));
        if (first("select id from employees where id = ?", employeeId).isEmpty() || first("select id from courses where id = ? and status = '上架'", courseId).isEmpty()) {
            return ApiResponse.fail("员工或课程不存在");
        }
        jdbcTemplate.update("""
                insert into course_quiz_records(employee_id, course_id, score, answers_json)
                values (?, ?, ?, ?)
                """, employeeId, courseId, score, toJson(body.getOrDefault("answers", List.of())));
        return ApiResponse.ok(first("select * from course_quiz_records where employee_id = ? and course_id = ? order by id desc limit 1", employeeId, courseId));
    }

    @GetMapping("/consultants")
    public ApiResponse<List<Map<String, Object>>> consultants() {
        return ApiResponse.ok(jdbcTemplate.queryForList("select * from consultants where status = '可预约' order by id desc"));
    }

    @GetMapping("/appointments")
    public ApiResponse<List<Map<String, Object>>> appointments(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select a.*, c.name consultant_name, c.title consultant_title, c.avatar_url consultant_avatar_url
                from appointments a
                left join consultants c on a.consultant_id = c.id
                where a.employee_id = ?
                order by a.appointment_time desc
                """, employeeId));
    }

    @PostMapping("/appointments")
    public ApiResponse<Void> appointment(@RequestBody Map<String, Object> body) {
        if (first("select id from employees where id = ?", body.get("employeeId")).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        if (first("select id from consultants where id = ? and status = '可预约'", body.get("consultantId")).isEmpty()) {
            return ApiResponse.fail("咨询师不可预约");
        }
        jdbcTemplate.update("""
                insert into appointments(employee_id, consultant_id, appointment_time, method, status, notes, image_url)
                values (?, ?, ?, ?, '待确认', ?, ?)
                """, body.get("employeeId"), body.get("consultantId"), body.get("appointmentTime"), body.get("method"), body.get("notes"), mockImage("appointment.jpg"));
        return ApiResponse.ok(null);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<Void> cancelAppointment(@PathVariable long id, @RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 1)));
        if (first("select id from employees where id = ?", employeeId).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        jdbcTemplate.update("""
                update appointments
                set status = '已取消'
                where id = ? and employee_id = ? and status <> '已取消'
                """, id, employeeId);
        return ApiResponse.ok(null);
    }

    @PutMapping("/appointments/{id}/reschedule")
    public ApiResponse<Void> rescheduleAppointment(@PathVariable long id, @RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 1)));
        if (first("select id from employees where id = ?", employeeId).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        jdbcTemplate.update("""
                update appointments
                set appointment_time = ?, method = ?, notes = ?, status = '待确认'
                where id = ? and employee_id = ?
                """, body.get("appointmentTime"), body.get("method"), body.get("notes"), id, employeeId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/moods")
    public ApiResponse<Void> mood(@RequestBody Map<String, Object> body) {
        if (first("select id from employees where id = ?", body.get("employeeId")).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        jdbcTemplate.update("""
                insert into mood_logs(employee_id, mood_score, work_stress, family_stress, note, image_url, logged_at)
                values (?, ?, ?, ?, ?, ?, now())
                """, body.get("employeeId"), body.get("moodScore"), body.get("workStress"), body.get("familyStress"), body.get("note"), mockImage("mood.jpg"));
        return ApiResponse.ok(null);
    }

    @GetMapping("/moods")
    public ApiResponse<List<Map<String, Object>>> moods(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select * from mood_logs
                where employee_id = ?
                order by logged_at desc
                limit 14
                """, employeeId));
    }

    @DeleteMapping("/moods/{id}")
    public ApiResponse<Void> deleteMood(@PathVariable long id, @RequestParam(defaultValue = "1") long employeeId) {
        jdbcTemplate.update("delete from mood_logs where id = ? and employee_id = ?", id, employeeId);
        return ApiResponse.ok(null);
    }

    @GetMapping("/intervention-records")
    public ApiResponse<List<Map<String, Object>>> interventionRecords(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select r.*, p.title plan_title, p.action_type
                from intervention_records r
                left join intervention_plans p on p.id = r.plan_id
                where r.employee_id = ?
                order by r.created_at desc
                limit 30
                """, employeeId));
    }

    @PostMapping("/intervention-records")
    public ApiResponse<Void> interventionRecord(@RequestBody Map<String, Object> body) {
        if (first("select id from employees where id = ?", body.get("employeeId")).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        if (first("select id from intervention_plans where id = ? and status = '启用'", body.get("planId")).isEmpty()) {
            return ApiResponse.fail("干预方案不存在或已停用");
        }
        jdbcTemplate.update("""
                insert into intervention_records(employee_id, plan_id, report_id, action, state_note)
                values (?, ?, ?, ?, ?)
                """,
                body.get("employeeId"),
                body.get("planId"),
                zeroToNull(body.get("reportId")),
                body.getOrDefault("action", "打卡"),
                body.getOrDefault("stateNote", ""));
        return ApiResponse.ok(null);
    }

    @GetMapping("/reports")
    public ApiResponse<List<Map<String, Object>>> reports(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select r.*, s.title screening_title
                from screening_reports r
                left join screenings s on r.screening_id = s.id
                where r.employee_id = ?
                order by r.created_at desc
                """, employeeId));
    }

    @GetMapping("/reports/{id}")
    public ApiResponse<Map<String, Object>> report(@PathVariable long id) {
        Map<String, Object> report = first("""
                select r.*, e.name employee_name, s.title screening_title
                from screening_reports r
                left join employees e on r.employee_id = e.id
                left join screenings s on r.screening_id = s.id
                where r.id = ?
                """, id);
        if (report.isEmpty()) {
            return ApiResponse.fail("报告不存在");
        }
        String riskLevel = String.valueOf(report.getOrDefault("risk_level", "低风险"));
        List<Map<String, Object>> plans = jdbcTemplate.queryForList("""
                select * from intervention_plans
                where risk_level = ? and status = '启用'
                order by id desc
                """, riskLevel);
        List<Map<String, Object>> history = jdbcTemplate.queryForList("""
                select id, score, risk_level, created_at
                from screening_reports
                where employee_id = ?
                order by created_at desc
                limit 6
                """, report.get("employee_id"));
        return ApiResponse.ok(Map.of("report", report, "plans", plans, "history", history));
    }

    @GetMapping("/community-posts")
    public ApiResponse<List<Map<String, Object>>> communityPosts(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank() || "全部".equals(category)) {
            return ApiResponse.ok(jdbcTemplate.queryForList("""
                    select p.*, if(p.anonymous = 1, '匿名同事', e.name) author_name
                    from community_posts p
                    left join employees e on e.id = p.employee_id
                    where p.status = '发布'
                    order by p.created_at desc
                    """));
        }
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select p.*, if(p.anonymous = 1, '匿名同事', e.name) author_name
                from community_posts p
                left join employees e on e.id = p.employee_id
                where p.status = '发布' and p.category = ?
                order by p.created_at desc
                """, category));
    }

    @PostMapping("/community-posts")
    public ApiResponse<Void> communityPost(@RequestBody Map<String, Object> body) {
        if (first("select id from employees where id = ?", body.get("employeeId")).isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        String title = String.valueOf(body.getOrDefault("title", "")).trim();
        String content = String.valueOf(body.getOrDefault("content", "")).trim();
        if (title.isBlank() || content.isBlank()) {
            return ApiResponse.fail("请填写标题和内容");
        }
        jdbcTemplate.update("""
                insert into community_posts(employee_id, category, title, content, anonymous, status)
                values (?, ?, ?, ?, ?, '发布')
                """,
                body.get("employeeId"),
                body.getOrDefault("category", "经验分享"),
                title,
                content,
                Boolean.parseBoolean(String.valueOf(body.getOrDefault("anonymous", true))));
        return ApiResponse.ok(null);
    }

    @GetMapping("/notifications")
    public ApiResponse<List<Map<String, Object>>> notifications(@RequestParam(defaultValue = "1") long employeeId) {
        Map<String, Object> employee = first("select department_id from employees where id = ?", employeeId);
        if (employee.isEmpty()) {
            return ApiResponse.fail("员工账号不存在，请重新登录");
        }
        return ApiResponse.ok(notifications(employeeId, employee.get("department_id")));
    }

    @PutMapping("/notifications/{id}/read")
    public ApiResponse<Void> readNotification(@PathVariable long id, @RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 0)));
        Map<String, Object> employee = first("select department_id from employees where id = ?", employeeId);
        if (employee.isEmpty()) {
            return ApiResponse.fail("员工账号不存在");
        }
        jdbcTemplate.update("""
                update service_notifications
                set status = '已读'
                where id = ?
                  and (target_type = 'all'
                    or (target_type = 'employee' and target_id = ?)
                    or (target_type = 'department' and target_id = ?))
                """, id, employeeId, employee.get("department_id"));
        return ApiResponse.ok(null);
    }

    @GetMapping("/policies")
    public ApiResponse<List<Map<String, Object>>> policies() {
        return ApiResponse.ok(jdbcTemplate.queryForList("select * from policies where status = '发布' order by id desc"));
    }

    @GetMapping("/policies/{id}")
    public ApiResponse<Map<String, Object>> policy(@PathVariable long id) {
        Map<String, Object> policy = first("select * from policies where id = ?", id);
        if (policy.isEmpty()) {
            return ApiResponse.fail("政策内容不存在");
        }
        return ApiResponse.ok(policy);
    }

    private Map<String, Object> first(String sql, Object... args) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private List<Map<String, Object>> courseRows(long employeeId, int limit) {
        String sql = """
                select c.*,
                       coalesce(cp.progress, 0) progress,
                       coalesce(cp.completed, 0) completed,
                       cp.last_viewed_at,
                       if(cf.id is null, 0, 1) favorite,
                       coalesce((select max(score) from course_quiz_records qr where qr.employee_id = ? and qr.course_id = c.id), 0) quiz_score
                from courses c
                left join course_progress cp on cp.course_id = c.id and cp.employee_id = ?
                left join course_favorites cf on cf.course_id = c.id and cf.employee_id = ?
                where c.status = '上架'
                order by c.id desc
                """ + (limit > 0 ? " limit " + limit : "");
        return jdbcTemplate.queryForList(sql, employeeId, employeeId, employeeId);
    }

    private boolean hasEmployeeAndCourse(Map<String, Object> body) {
        Object employeeId = body.get("employeeId");
        Object courseId = body.get("courseId");
        return !first("select id from employees where id = ?", employeeId).isEmpty()
                && !first("select id from courses where id = ? and status = '上架'", courseId).isEmpty();
    }

    private String mockImage(String name) {
        return "http://119.29.152.180:9000/deng/mock/" + name;
    }

    private List<Map<String, Object>> notifications(Map<String, Object> employee) {
        Object employeeId = employee.get("id");
        Object departmentId = employee.get("department_id");
        return notifications(employeeId, departmentId);
    }

    private List<Map<String, Object>> notifications(Object employeeId, Object departmentId) {
        return jdbcTemplate.queryForList("""
                select * from service_notifications
                where target_type = 'all'
                   or (target_type = 'employee' and target_id = ?)
                   or (target_type = 'department' and target_id = ?)
                order by created_at desc
                limit 8
                """, employeeId, departmentId);
    }

    private String riskPoints(int score) {
        if (score >= 80) {
            return "焦虑风险、角色冲突、持续压力偏高";
        }
        if (score >= 55) {
            return "工作压力、支持感不足、节奏适应波动";
        }
        return "状态整体平稳，建议关注睡眠恢复与日常压力";
    }

    private String suggestionFor(String risk) {
        return switch (risk) {
            case "高风险" -> "建议优先预约专业咨询，并按自主意愿决定是否接收企业关怀跟进";
            case "中风险" -> "建议选择 1 对 1 咨询入口或岗位沟通清单，连续记录七日状态";
            default -> "建议使用正念练习、呼吸放松等自助工具，并保持状态记录";
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object zeroToNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return "0".equals(text) || text.isBlank() ? null : value;
    }
}
