package com.danzi.emotion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionGeneratorService {
    private static final List<String> DIMENSIONS = List.of(
            "情绪稳定",
            "焦虑风险",
            "工作压力",
            "睡眠恢复",
            "支持系统",
            "角色平衡",
            "职业信心",
            "自我照护",
            "沟通安全",
            "恢复力"
    );
    private static final List<String> PROMPTS = List.of(
            "过去一周，我在面对%s相关情况时能保持基本稳定",
            "当%s带来压力时，我能找到适合自己的缓解方式",
            "%s已经明显影响到我的休息、专注或工作推进",
            "我愿意在%s状态加重时主动寻求同事、家人或专业支持",
            "我能识别%s背后的主要压力来源，并尝试做出调整",
            "面对%s，我仍能把任务拆成当天可以完成的小步骤",
            "我能和身边的人清楚表达自己在%s方面的支持需求",
            "%s出现波动时，我能给自己保留必要的恢复时间",
            "我对逐步改善%s相关状态有信心",
            "我能持续记录%s的变化，并观察哪些方法对自己有效",
            "我知道企业或平台中哪些资源可以帮助我应对%s",
            "即使遇到%s，我也能保持基本的生活和工作节奏"
    );
    private static final List<Map<String, Object>> LIKERT_OPTIONS = List.of(
            Map.of("label", "完全不符合", "value", 1, "score", 20),
            Map.of("label", "比较不符合", "value", 2, "score", 40),
            Map.of("label", "一般", "value", 3, "score", 60),
            Map.of("label", "比较符合", "value", 4, "score", 80),
            Map.of("label", "完全符合", "value", 5, "score", 100)
    );

    private final ObjectMapper objectMapper;

    public QuestionGeneratorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> generate(String topic, int requestedCount) {
        String cleanTopic = sanitizeTopic(topic);
        int count = Math.max(1, Math.min(requestedCount, 12));
        List<Map<String, Object>> questions = new ArrayList<>();
        String optionsJson = optionsJson();
        for (int i = 0; i < count; i++) {
            Map<String, Object> question = new LinkedHashMap<>();
            question.put("content", String.format(PROMPTS.get(i % PROMPTS.size()), cleanTopic));
            question.put("dimension", DIMENSIONS.get(i % DIMENSIONS.size()));
            question.put("sort_no", i + 1);
            question.put("answer_type", "single");
            question.put("options_json", optionsJson);
            question.put("score_rule", "likert_5");
            question.put("image_url", "");
            question.put("status", "启用");
            questions.add(question);
        }
        return questions;
    }

    private String sanitizeTopic(String topic) {
        String value = topic == null ? "" : topic.trim();
        return value.isEmpty() ? "情绪状态" : value;
    }

    private String optionsJson() {
        try {
            return objectMapper.writeValueAsString(LIKERT_OPTIONS);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("选项配置生成失败", e);
        }
    }
}
