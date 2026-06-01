package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public class MobileController {
    private final JdbcTemplate jdbcTemplate;

    public MobileController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        Map<String, Object> employee = jdbcTemplate.queryForMap("""
                select e.*, d.name department_name from employees e left join departments d on e.department_id = d.id where e.id = ?
                """, employeeId);
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
        String risk = score >= 80 ? "高风险" : score >= 55 ? "中风险" : "低风险";
        jdbcTemplate.update("""
                insert into screening_reports(employee_id, screening_id, score, risk_level, summary, suggestion, chart_image_url)
                values (?, ?, ?, ?, ?, ?, ?)
                """, employeeId, id, score, risk, "AI 已生成本次情绪筛查摘要", "建议查看匹配干预方案并持续记录状态", mockImage("report-new.jpg"));
        return ApiResponse.ok(first("select * from screening_reports where employee_id = ? order by id desc limit 1", employeeId));
    }

    @GetMapping("/courses")
    public ApiResponse<List<Map<String, Object>>> courses(@RequestParam(defaultValue = "1") long employeeId) {
        return ApiResponse.ok(courseRows(employeeId, 0));
    }

    @GetMapping("/courses/{id}")
    public ApiResponse<Map<String, Object>> course(@PathVariable long id, @RequestParam(defaultValue = "1") long employeeId) {
        Map<String, Object> course = jdbcTemplate.queryForMap("select * from courses where id = ?", id);
        Map<String, Object> progress = first("select * from course_progress where employee_id = ? and course_id = ? limit 1", employeeId, id);
        return ApiResponse.ok(Map.of("course", course, "progress", progress));
    }

    @PostMapping("/course-progress")
    public ApiResponse<Void> courseProgress(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update("""
                insert into course_progress(employee_id, course_id, progress, completed, last_viewed_at, image_url)
                values (?, ?, ?, ?, now(), ?)
                on duplicate key update progress = values(progress), completed = values(completed), last_viewed_at = now()
                """, body.get("employeeId"), body.get("courseId"), body.get("progress"), body.get("completed"), mockImage("progress.jpg"));
        return ApiResponse.ok(null);
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
        jdbcTemplate.update("""
                insert into appointments(employee_id, consultant_id, appointment_time, method, status, notes, image_url)
                values (?, ?, ?, ?, '待确认', ?, ?)
                """, body.get("employeeId"), body.get("consultantId"), body.get("appointmentTime"), body.get("method"), body.get("notes"), mockImage("appointment.jpg"));
        return ApiResponse.ok(null);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<Void> cancelAppointment(@PathVariable long id, @RequestBody Map<String, Object> body) {
        long employeeId = Long.parseLong(String.valueOf(body.getOrDefault("employeeId", 1)));
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
        jdbcTemplate.update("""
                update appointments
                set appointment_time = ?, method = ?, notes = ?, status = '待确认'
                where id = ? and employee_id = ?
                """, body.get("appointmentTime"), body.get("method"), body.get("notes"), id, employeeId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/moods")
    public ApiResponse<Void> mood(@RequestBody Map<String, Object> body) {
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
        Map<String, Object> report = jdbcTemplate.queryForMap("""
                select r.*, e.name employee_name, s.title screening_title
                from screening_reports r
                left join employees e on r.employee_id = e.id
                left join screenings s on r.screening_id = s.id
                where r.id = ?
                """, id);
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

    @GetMapping("/policies")
    public ApiResponse<List<Map<String, Object>>> policies() {
        return ApiResponse.ok(jdbcTemplate.queryForList("select * from policies where status = '发布' order by id desc"));
    }

    @GetMapping("/policies/{id}")
    public ApiResponse<Map<String, Object>> policy(@PathVariable long id) {
        return ApiResponse.ok(jdbcTemplate.queryForMap("select * from policies where id = ?", id));
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
                       cp.last_viewed_at
                from courses c
                left join course_progress cp on cp.course_id = c.id and cp.employee_id = ?
                where c.status = '上架'
                order by c.id desc
                """ + (limit > 0 ? " limit " + limit : "");
        return jdbcTemplate.queryForList(sql, employeeId);
    }

    private String mockImage(String name) {
        return "http://119.29.152.180:9000/deng/mock/" + name;
    }
}
