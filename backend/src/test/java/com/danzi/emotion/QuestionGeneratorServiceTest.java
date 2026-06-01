package com.danzi.emotion;

import com.danzi.emotion.service.QuestionGeneratorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionGeneratorServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generatesReviewableLikertChoiceQuestionsForTopic() throws Exception {
        QuestionGeneratorService service = new QuestionGeneratorService(objectMapper);

        List<Map<String, Object>> questions = service.generate("产后返岗焦虑", 4);

        assertThat(questions).hasSize(4);
        assertThat(questions)
                .allSatisfy(question -> {
                    assertThat(question.get("content").toString()).contains("产后返岗焦虑");
                    assertThat(question).containsEntry("answer_type", "single");
                    assertThat(question).containsEntry("score_rule", "likert_5");
                    assertThat(question).containsEntry("status", "启用");

                    List<Map<String, Object>> options = objectMapper.readValue(
                            question.get("options_json").toString(),
                            new TypeReference<>() {
                            }
                    );
                    assertThat(options).extracting("label")
                            .containsExactly("完全不符合", "比较不符合", "一般", "比较符合", "完全符合");
                    assertThat(options).extracting("score")
                            .containsExactly(20, 40, 60, 80, 100);
                });
    }

    @Test
    void capsCountAndVariesDimensions() {
        QuestionGeneratorService service = new QuestionGeneratorService(objectMapper);

        List<Map<String, Object>> questions = service.generate("睡眠恢复", 20);

        assertThat(questions).hasSize(12);
        Set<Object> dimensions = questions.stream()
                .map(question -> question.get("dimension"))
                .collect(Collectors.toSet());
        assertThat(dimensions).hasSizeGreaterThanOrEqualTo(4);
        assertThat(questions).extracting("sort_no").containsExactlyElementsOf(
                java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList()
        );
    }
}
