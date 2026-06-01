package com.danzi.emotion;

import com.danzi.emotion.service.TableRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableRegistryTest {
    @Test
    void exposesWhitelistedTablesForAdminCrud() {
        TableRegistry registry = new TableRegistry();

        assertThat(registry.find("employees")).isPresent();
        assertThat(registry.find("employees").orElseThrow().writableColumns()).contains("name", "avatar_url", "risk_level");
        assertThat(registry.find("screening_questions")).isPresent();
        assertThat(registry.find("screening_questions").orElseThrow().writableColumns())
                .contains("screening_id", "content", "dimension", "sort_no", "answer_type", "options_json", "score_rule", "image_url", "status");
        assertThat(registry.find("not_allowed")).isEmpty();
        assertThat(registry.all()).hasSize(15);
    }
}
