package com.danzi.emotion;

import com.danzi.emotion.controller.MobileController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MobileControllerTest {
    @Test
    void homeDoesNotExposeAppointmentOrCommunityDataForPersonalAuditBuild() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        MobileController controller = new MobileController(jdbcTemplate, new ObjectMapper());

        when(jdbcTemplate.queryForList(anyString(), any(Object[].class))).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            if (sql.contains("from employees")) {
                return List.of(Map.of("id", 1L, "department_id", 2L, "name", "测试员工"));
            }
            if (sql.contains("from courses")) {
                return List.of();
            }
            if (sql.contains("from service_notifications")) {
                return List.of();
            }
            return List.of();
        });
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        controller.home(1L);

        org.mockito.Mockito.verify(jdbcTemplate, org.mockito.Mockito.never()).queryForList(
                org.mockito.ArgumentMatchers.contains("from appointments"),
                eq(1L)
        );
        org.mockito.Mockito.verify(jdbcTemplate, org.mockito.Mockito.never()).queryForList(
                org.mockito.ArgumentMatchers.contains("from community_posts")
        );
    }
}
