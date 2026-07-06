package com.danzi.emotion;

import com.danzi.emotion.controller.MobileController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileControllerTest {
    @Test
    void homeOnlyUsesFutureAppointmentsForNextAppointment() {
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

        verify(jdbcTemplate).queryForList(
                org.mockito.ArgumentMatchers.argThat(sql ->
                        sql.contains("from appointments")
                                && sql.contains("a.status <> '已取消'")
                                && sql.contains("a.appointment_time >= now()")
                                && sql.contains("order by a.appointment_time asc")),
                eq(1L)
        );
        assertThat(true).isTrue();
    }
}
