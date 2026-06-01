package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiResponse<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("employees", count("employees"));
        stats.put("highRisk", countWhere("screening_reports", "risk_level = '高风险'"));
        stats.put("appointments", count("appointments"));
        stats.put("courseCompletion", averageProgress());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("stats", stats);
        data.put("riskLevels", jdbcTemplate.queryForList("select risk_level name, count(*) value from screening_reports group by risk_level"));
        data.put("departmentRisk", jdbcTemplate.queryForList("""
                select d.name, coalesce(round(avg(r.score), 1), 0) score
                from departments d
                left join employees e on e.department_id = d.id
                left join screening_reports r on r.employee_id = e.id
                group by d.id, d.name
                order by d.id
                """));
        data.put("moodTrend", jdbcTemplate.queryForList("""
                select date_format(logged_at, '%m-%d') day, round(avg(mood_score), 1) mood, round(avg(work_stress), 1) stress
                from mood_logs group by date_format(logged_at, '%m-%d') order by min(logged_at)
                """));
        data.put("serviceUsage", jdbcTemplate.queryForList("""
                select '筛查完成' name, count(*) value from screening_reports
                union all select '课程学习', count(*) from course_progress where progress >= 60
                union all select '咨询预约', count(*) from appointments
                union all select '状态记录', count(*) from mood_logs
                """));
        return ApiResponse.ok(data);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        String fileName = URLEncoder.encode("emotion-dashboard-report.csv", StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\ufeff');
            writer.println("模块,指标,数值,说明");
            writer.printf("总体,员工总数,%d,已录入员工账号%n", count("employees"));
            writer.printf("总体,高风险预警,%d,筛查报告风险等级为高风险%n", countWhere("screening_reports", "risk_level = '高风险'"));
            writer.printf("总体,咨询预约量,%d,累计咨询预约记录%n", count("appointments"));
            writer.printf("总体,课程平均进度,%.1f%%,课程学习进度平均值%n", averageProgress());

            writeRows(writer, "风险等级分布", jdbcTemplate.queryForList("""
                    select risk_level metric, count(*) value
                    from screening_reports
                    group by risk_level
                    order by field(risk_level, '低风险', '中风险', '高风险')
                    """));
            writeRows(writer, "部门压力指数", jdbcTemplate.queryForList("""
                    select d.name metric, coalesce(round(avg(r.score), 1), 0) value
                    from departments d
                    left join employees e on e.department_id = d.id
                    left join screening_reports r on r.employee_id = e.id
                    group by d.id, d.name
                    order by d.id
                    """));
            writeRows(writer, "服务使用情况", jdbcTemplate.queryForList("""
                    select '筛查完成' metric, count(*) value from screening_reports
                    union all select '课程学习', count(*) from course_progress where progress >= 60
                    union all select '咨询预约', count(*) from appointments
                    union all select '状态记录', count(*) from mood_logs
                    """));
        }
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
