package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import com.danzi.emotion.service.QuestionGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/question-bank")
public class QuestionBankController {
    private static final String DEFAULT_SCREENING_IMAGE = "http://119.29.152.180:9000/deng/mock/screening-generated.jpg";
    private static final String DEFAULT_QUESTION_IMAGE = "http://119.29.152.180:9000/deng/mock/question-generated.jpg";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final QuestionGeneratorService generatorService;

    public QuestionBankController(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, QuestionGeneratorService generatorService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.generatorService = generatorService;
    }

    @GetMapping("/screenings")
    public ApiResponse<List<Map<String, Object>>> screenings() {
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select s.*,
                       (select count(*) from screening_questions q where q.screening_id = s.id) question_count
                from screenings s
                order by s.id desc
                """));
    }

    @PostMapping("/screenings")
    public ApiResponse<Map<String, Object>> createScreening(@RequestBody Map<String, Object> payload) {
        String title = required(payload, "title", "测试名称不能为空");
        String description = text(payload, "description", "后台创建的情感测试");
        String pushCycle = text(payload, "push_cycle", "手动");
        int estimatedMinutes = number(payload, "estimated_minutes", 6);
        String coverUrl = text(payload, "cover_url", DEFAULT_SCREENING_IMAGE);
        String status = text(payload, "status", "启用");

        long id = insert("""
                insert into screenings(title, description, push_cycle, estimated_minutes, cover_url, status)
                values (?, ?, ?, ?, ?, ?)
                """, title, description, pushCycle, estimatedMinutes, coverUrl, status);
        return ApiResponse.ok(jdbcTemplate.queryForMap("select * from screenings where id = ?", id));
    }

    @GetMapping("/questions")
    public ApiResponse<List<Map<String, Object>>> questions(@RequestParam(required = false) Long screeningId,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(required = false) String dimension,
                                                            @RequestParam(required = false) String status) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where 1 = 1");
        if (screeningId != null && screeningId > 0) {
            where.append(" and q.screening_id = ?");
            args.add(screeningId);
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and (q.content like ? or q.dimension like ? or s.title like ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (dimension != null && !dimension.isBlank()) {
            where.append(" and q.dimension = ?");
            args.add(dimension.trim());
        }
        if (status != null && !status.isBlank()) {
            where.append(" and q.status = ?");
            args.add(status.trim());
        }
        return ApiResponse.ok(jdbcTemplate.queryForList("""
                select q.*, s.title screening_title
                from screening_questions q
                left join screenings s on s.id = q.screening_id
                """ + where + " order by q.screening_id desc, q.sort_no asc, q.id desc limit 200", args.toArray()));
    }

    @PostMapping("/questions")
    public ApiResponse<Map<String, Object>> createQuestion(@RequestBody Map<String, Object> payload) {
        long id = insertQuestion(payload, longValue(payload.get("screening_id"), "请选择所属情感测试"));
        return ApiResponse.ok(questionById(id));
    }

    @PostMapping("/questions/batch")
    public ApiResponse<List<Map<String, Object>>> createQuestions(@RequestBody Map<String, Object> payload) {
        long screeningId = longValue(payload.get("screeningId"), "请选择所属情感测试");
        Object raw = payload.get("questions");
        if (!(raw instanceof List<?> questions) || questions.isEmpty()) {
            throw new IllegalArgumentException("请先添加要保存的题目");
        }
        List<Map<String, Object>> saved = new ArrayList<>();
        int sortNo = nextSortNo(screeningId);
        for (Object item : questions) {
            if (item instanceof Map<?, ?> map) {
                Map<String, Object> question = asPayload(map);
                question.put("sort_no", sortNo + saved.size());
                long id = insertQuestion(question, screeningId);
                saved.add(questionById(id));
            }
        }
        return ApiResponse.ok(saved);
    }

    @PostMapping("/generate")
    public ApiResponse<List<Map<String, Object>>> generate(@RequestBody Map<String, Object> payload) {
        String topic = text(payload, "topic", "情绪状态");
        int count = number(payload, "count", 5);
        return ApiResponse.ok(generatorService.generate(topic, count));
    }

    private long insertQuestion(Map<String, Object> payload, long screeningId) {
        String content = required(payload, "content", "题干不能为空");
        String dimension = text(payload, "dimension", "综合评估");
        int sortNo = number(payload, "sort_no", nextSortNo(screeningId));
        String answerType = text(payload, "answer_type", "single");
        String optionsJson = optionsJson(payload);
        String scoreRule = text(payload, "score_rule", "likert_5");
        String imageUrl = text(payload, "image_url", DEFAULT_QUESTION_IMAGE);
        String status = text(payload, "status", "启用");
        return insert("""
                insert into screening_questions(screening_id, content, dimension, sort_no, answer_type, options_json, score_rule, image_url, status)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, screeningId, content, dimension, sortNo, answerType, optionsJson, scoreRule, imageUrl, status);
    }

    private long insert(String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        return Objects.requireNonNullElse(keyHolder.getKey(), 0).longValue();
    }

    private Map<String, Object> questionById(long id) {
        return jdbcTemplate.queryForMap("""
                select q.*, s.title screening_title
                from screening_questions q
                left join screenings s on s.id = q.screening_id
                where q.id = ?
                """, id);
    }

    private int nextSortNo(long screeningId) {
        Integer value = jdbcTemplate.queryForObject(
                "select coalesce(max(sort_no), 0) + 1 from screening_questions where screening_id = ?",
                Integer.class,
                screeningId
        );
        return value == null ? 1 : value;
    }

    private String optionsJson(Map<String, Object> payload) {
        Object options = payload.get("options");
        if (options instanceof List<?>) {
            try {
                return objectMapper.writeValueAsString(options);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("选项配置格式不正确");
            }
        }
        return text(payload, "options_json", generatorService.generate("情绪状态", 1).get(0).get("options_json").toString());
    }

    private String required(Map<String, Object> payload, String key, String message) {
        String value = text(payload, key, "");
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String text(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return String.valueOf(value).trim();
    }

    private int number(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private long longValue(Object value, String message) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException(message);
        }
        long parsed = Long.parseLong(String.valueOf(value));
        if (parsed <= 0) {
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private Map<String, Object> asPayload(Map<?, ?> source) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null) {
                payload.put(String.valueOf(key), value);
            }
        });
        return payload;
    }
}
