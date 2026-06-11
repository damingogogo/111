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
        register("employees", "员工", List.of("name", "employee_no", "phone", "department_id", "position", "return_work_date", "avatar_url", "risk_level", "status", "login_password"));
        register("screenings", "筛查", List.of("title", "description", "push_cycle", "estimated_minutes", "cover_url", "status"));
        register("screening_questions", "题库", List.of("screening_id", "content", "dimension", "sort_no", "answer_type", "options_json", "score_rule", "image_url", "status"));
        register("screening_reports", "报告", List.of("employee_id", "screening_id", "score", "risk_level", "summary", "suggestion", "risk_points", "answers_json", "enterprise_notice_consent", "chart_image_url", "created_at"));
        register("intervention_plans", "干预方案", List.of("risk_level", "title", "content", "action_type", "cover_url", "status"));
        register("intervention_records", "干预记录", List.of("employee_id", "plan_id", "report_id", "action", "state_note", "created_at"));
        register("courses", "课程", List.of("title", "category", "duration_minutes", "summary", "video_url", "mind_map", "quiz_json", "cover_url", "status"));
        register("course_progress", "学习进度", List.of("employee_id", "course_id", "progress", "completed", "last_viewed_at", "image_url"));
        register("course_favorites", "课程收藏", List.of("employee_id", "course_id", "created_at"));
        register("course_quiz_records", "课程小测", List.of("employee_id", "course_id", "score", "answers_json", "created_at"));
        register("consultants", "咨询师", List.of("name", "title", "speciality", "phone", "avatar_url", "status"));
        register("appointments", "咨询预约", List.of("employee_id", "consultant_id", "appointment_time", "method", "status", "notes", "image_url"));
        register("mood_logs", "状态记录", List.of("employee_id", "mood_score", "work_stress", "family_stress", "note", "image_url", "logged_at"));
        register("community_posts", "互助社区", List.of("employee_id", "category", "title", "content", "anonymous", "reply_count", "status"));
        register("care_followups", "关怀跟进", List.of("report_id", "employee_id", "department_id", "employee_no", "risk_level", "consent_to_notify", "status", "follow_note", "assigned_role", "followed_at"));
        register("service_notifications", "服务通知", List.of("target_type", "target_id", "title", "content", "channel", "status"));
        register("service_effect_metrics", "效果量化", List.of("period_label", "department_id", "retained_count", "employee_count", "happiness_before", "happiness_after", "productivity_before", "productivity_after", "fit_before", "fit_after", "encrypted_file_url", "report_summary"));
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
