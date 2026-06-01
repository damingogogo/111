package com.danzi.emotion.service;

import com.danzi.emotion.model.TableDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TableRegistry {
    private final Map<String, TableDefinition> tables = new LinkedHashMap<>();

    public TableRegistry() {
        register("admin_users", "管理员", List.of("username", "password", "real_name", "role", "phone", "avatar_url", "status"));
        register("departments", "部门", List.of("name", "leader", "description", "image_url", "status"));
        register("employees", "员工", List.of("name", "employee_no", "phone", "department_id", "position", "return_work_date", "avatar_url", "risk_level", "status"));
        register("screenings", "筛查", List.of("title", "description", "push_cycle", "estimated_minutes", "cover_url", "status"));
        register("screening_questions", "题库", List.of("screening_id", "content", "dimension", "sort_no", "answer_type", "options_json", "score_rule", "image_url", "status"));
        register("screening_reports", "报告", List.of("employee_id", "screening_id", "score", "risk_level", "summary", "suggestion", "chart_image_url", "created_at"));
        register("intervention_plans", "干预方案", List.of("risk_level", "title", "content", "action_type", "cover_url", "status"));
        register("courses", "课程", List.of("title", "category", "duration_minutes", "summary", "cover_url", "status"));
        register("course_progress", "学习进度", List.of("employee_id", "course_id", "progress", "completed", "last_viewed_at", "image_url"));
        register("consultants", "咨询师", List.of("name", "title", "speciality", "phone", "avatar_url", "status"));
        register("appointments", "咨询预约", List.of("employee_id", "consultant_id", "appointment_time", "method", "status", "notes", "image_url"));
        register("mood_logs", "状态记录", List.of("employee_id", "mood_score", "work_stress", "family_stress", "note", "image_url", "logged_at"));
        register("policies", "政策内容", List.of("title", "category", "content", "cover_url", "status"));
        register("system_settings", "系统设置", List.of("setting_key", "setting_value", "description", "image_url"));
        register("upload_files", "上传文件", List.of("file_name", "object_name", "url", "content_type", "size_bytes", "biz_type"));
    }

    private void register(String name, String label, List<String> writableColumns) {
        tables.put(name, new TableDefinition(name, label, writableColumns, "id desc"));
    }

    public Optional<TableDefinition> find(String table) {
        return Optional.ofNullable(tables.get(table));
    }

    public List<TableDefinition> all() {
        return List.copyOf(tables.values());
    }
}
