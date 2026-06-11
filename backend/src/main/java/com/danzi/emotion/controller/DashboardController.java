package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final JdbcTemplate jdbcTemplate;

    public DashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> dashboard(@RequestParam(defaultValue = "all") String period) {
        String reportRange = periodWhere(period, "created_at");
        String reportJoinRange = periodWhere(period, "r.created_at");
        String appointmentRange = periodWhere(period, "appointment_time");
        String progressRange = periodWhere(period, "last_viewed_at");
        String moodRange = periodWhere(period, "logged_at");
        String interventionRange = periodWhere(period, "created_at");
        String followupRange = periodWhere(period, "created_at");
        String followupAliasRange = periodWhere(period, "f.created_at");
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("employees", count("employees"));
        stats.put("highRisk", countWhere("screening_reports", "risk_level = '高风险'" + reportRange));
        stats.put("appointments", countWhere("appointments", "1 = 1" + appointmentRange));
        stats.put("courseCompletion", averageProgress());
        stats.put("carePending", countWhere("care_followups", "status in ('待跟进', '观察中')" + followupRange));
        stats.put("retentionRate", retentionRate());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", stats);
        data.put("period", period);
        data.put("riskLevels", jdbcTemplate.queryForList("select risk_level name, count(*) value from screening_reports where 1 = 1 " + reportRange + " group by risk_level"));
        data.put("departmentRisk", jdbcTemplate.queryForList("""
                select d.name, coalesce(round(avg(r.score), 1), 0) score
                from departments d
                left join employees e on e.department_id = d.id
                left join screening_reports r on r.employee_id = e.id %s
                group by d.id, d.name
                order by d.id
                """.formatted(reportJoinRange)));
        data.put("moodTrend", jdbcTemplate.queryForList(String.format("""
                select date_format(logged_at, '%%m-%%d') day, round(avg(mood_score), 1) mood, round(avg(work_stress), 1) stress
                from mood_logs where 1 = 1 %s group by date_format(logged_at, '%%m-%%d') order by min(logged_at)
                """, moodRange)));
        data.put("serviceUsage", jdbcTemplate.queryForList("""
                select '筛查完成' name, count(*) value from screening_reports where 1 = 1 %s
                union all select '课程学习', count(*) from course_progress where progress >= 60 %s
                union all select '咨询预约', count(*) from appointments where 1 = 1 %s
                union all select '状态记录', count(*) from mood_logs where 1 = 1 %s
                union all select '干预执行', count(*) from intervention_records where 1 = 1 %s
                """.formatted(
                reportRange,
                progressRange,
                appointmentRange,
                moodRange,
                interventionRange
        )));
        data.put("careFollowups", jdbcTemplate.queryForList("""
                select f.*, d.name department_name
                from care_followups f
                left join departments d on d.id = f.department_id
                where 1 = 1 %s
                order by f.created_at desc
                limit 12
                """.formatted(followupAliasRange)));
        data.put("effectMetrics", jdbcTemplate.queryForList("""
                select m.*, d.name department_name,
                       round(if(employee_count = 0, 0, retained_count / employee_count * 100), 1) retention_rate,
                       round(happiness_after - happiness_before, 1) happiness_delta,
                       round(productivity_after - productivity_before, 1) productivity_delta,
                       round(fit_after - fit_before, 1) fit_delta
                from service_effect_metrics m
                left join departments d on d.id = m.department_id
                order by m.created_at desc
                limit 8
                """));
        data.put("notifications", jdbcTemplate.queryForList("""
                select n.*, d.name department_name, e.employee_no employee_no
                from service_notifications n
                left join departments d on n.target_type = 'department' and n.target_id = d.id
                left join employees e on n.target_type = 'employee' and n.target_id = e.id
                order by n.created_at desc
                limit 10
                """));
        return ApiResponse.ok(data);
    }

    @GetMapping("/export")
    public void export(@RequestParam(defaultValue = "all") String period, HttpServletResponse response) throws Exception {
        String reportRange = periodWhere(period, "created_at");
        String reportJoinRange = periodWhere(period, "r.created_at");
        String appointmentRange = periodWhere(period, "appointment_time");
        String progressRange = periodWhere(period, "last_viewed_at");
        String moodRange = periodWhere(period, "logged_at");
        String interventionRange = periodWhere(period, "created_at");
        String followupRange = periodWhere(period, "created_at");
        String fileName = URLEncoder.encode("emotion-dashboard-report.csv", StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("模块,指标,数值,说明");
            writer.printf("筛选,统计周期,%s,all/quarter/month/week%n", csv(period));
            writer.printf("总体,员工总数,%d,已录入员工账号%n", count("employees"));
            writer.printf("总体,高风险预警,%d,筛查报告风险等级为高风险%n", countWhere("screening_reports", "risk_level = '高风险'" + reportRange));
            writer.printf("总体,待跟进预警,%d,关怀跟进仍需处理%n", countWhere("care_followups", "status in ('待跟进', '观察中')" + followupRange));
            writer.printf("总体,咨询预约量,%d,累计咨询预约记录%n", countWhere("appointments", "1 = 1" + appointmentRange));
            writer.printf("总体,课程平均进度,%.1f%%,课程学习进度平均值%n", averageProgress());
            writer.printf("总体,员工留存率,%.1f%%,效果量化表的加权留存率%n", retentionRate());

            writeRows(writer, "风险等级分布", jdbcTemplate.queryForList("""
                    select risk_level metric, count(*) value
                    from screening_reports
                    where 1 = 1 %s
                    group by risk_level
                    order by field(risk_level, '低风险', '中风险', '高风险')
                    """.formatted(reportRange)));
            writeRows(writer, "部门压力指数", jdbcTemplate.queryForList("""
                    select d.name metric, coalesce(round(avg(r.score), 1), 0) value
                    from departments d
                    left join employees e on e.department_id = d.id
                    left join screening_reports r on r.employee_id = e.id %s
                    group by d.id, d.name
                    order by d.id
                    """.formatted(reportJoinRange)));
            writeRows(writer, "服务使用情况", jdbcTemplate.queryForList("""
                    select '筛查完成' metric, count(*) value from screening_reports where 1 = 1 %s
                    union all select '课程学习', count(*) from course_progress where progress >= 60 %s
                    union all select '咨询预约', count(*) from appointments where 1 = 1 %s
                    union all select '状态记录', count(*) from mood_logs where 1 = 1 %s
                    union all select '干预执行', count(*) from intervention_records where 1 = 1 %s
                    """.formatted(
                    reportRange,
                    progressRange,
                    appointmentRange,
                    moodRange,
                    interventionRange
            )));
            writeRows(writer, "效果量化", jdbcTemplate.queryForList("""
                    select coalesce(d.name, period_label) metric,
                           concat('留存率 ', round(if(employee_count = 0, 0, retained_count / employee_count * 100), 1), '%%; 幸福感+', round(happiness_after - happiness_before, 1)) value
                    from service_effect_metrics m
                    left join departments d on d.id = m.department_id
                    order by m.created_at desc
                    """));
        }
    }

    @PutMapping("/care-followups/{id}/complete")
    public ApiResponse<Void> completeFollowup(@PathVariable long id, @RequestBody Map<String, Object> body) {
        jdbcTemplate.update("""
                update care_followups
                set status = '已跟进', follow_note = ?, followed_at = now()
                where id = ?
                """, body.getOrDefault("followNote", "已完成关怀跟进"), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/notifications")
    public ApiResponse<Void> notification(@RequestBody Map<String, Object> body) {
        jdbcTemplate.update("""
                insert into service_notifications(target_type, target_id, title, content, channel, status)
                values (?, ?, ?, ?, ?, '未读')
                """,
                body.getOrDefault("targetType", "all"),
                body.get("targetId"),
                body.getOrDefault("title", "服务通知"),
                body.getOrDefault("content", ""),
                body.getOrDefault("channel", "企业内部通知"));
        return ApiResponse.ok(null);
    }

    private Integer count(String table) {
        return jdbcTemplate.queryForObject("select count(*) from " + table, Integer.class);
    }

    private Integer countWhere(String table, String where) {
        return jdbcTemplate.queryForObject("select count(*) from " + table + " where " + where, Integer.class);
    }

    private Double averageProgress() {
        Double value = jdbcTemplate.queryForObject("select round(coalesce(avg(progress), 0), 1) from course_progress", Double.class);
        return value == null ? 0.0 : value;
    }

    private Double retentionRate() {
        Double value = jdbcTemplate.queryForObject("""
                select round(if(coalesce(sum(employee_count), 0) = 0, 0, sum(retained_count) / sum(employee_count) * 100), 1)
                from service_effect_metrics
                """, Double.class);
        return value == null ? 0.0 : value;
    }

    private String periodWhere(String period, String column) {
        return switch (period) {
            case "week" -> " and " + column + " >= date_sub(now(), interval 7 day)";
            case "month" -> " and " + column + " >= date_sub(now(), interval 1 month)";
            case "quarter" -> " and " + column + " >= date_sub(now(), interval 3 month)";
            case "year" -> " and " + column + " >= date_sub(now(), interval 1 year)";
            default -> "";
        };
    }

    private void writeRows(PrintWriter writer, String section, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            writer.printf("%s,%s,%s,%s%n",
                    csv(section),
                    csv(String.valueOf(row.getOrDefault("metric", ""))),
                    csv(String.valueOf(row.getOrDefault("value", ""))),
                    "");
        }
    }

    private String csv(String value) {
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
