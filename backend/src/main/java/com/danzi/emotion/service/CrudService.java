package com.danzi.emotion.service;

import com.danzi.emotion.model.TableDefinition;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CrudService {
    private final JdbcTemplate jdbcTemplate;
    private final TableRegistry registry;

    public CrudService(JdbcTemplate jdbcTemplate, TableRegistry registry) {
        this.jdbcTemplate = jdbcTemplate;
        this.registry = registry;
    }

    public List<Map<String, Object>> list(String table, int page, int size, String keyword) {
        TableDefinition definition = mustFind(table);
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page - 1) * limit;
        List<Object> args = new ArrayList<>();
        String where = "";
        if (keyword != null && !keyword.isBlank()) {
            where = " where cast(id as char) like ? or concat_ws('', " + String.join(",", definition.writableColumns()) + ") like ?";
            args.add("%" + keyword + "%");
            args.add("%" + keyword + "%");
        }
        args.add(limit);
        args.add(offset);
        String sql = "select * from " + definition.name() + where + " order by " + definition.defaultOrder() + " limit ? offset ?";
        return jdbcTemplate.queryForList(sql, args.toArray());
    }

    public Map<String, Object> create(String table, Map<String, Object> payload) {
        TableDefinition definition = mustFind(table);
        Map<String, Object> values = writableValues(definition, payload, true);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("没有可保存字段");
        }
        String columns = String.join(",", values.keySet());
        String placeholders = values.keySet().stream().map(k -> "?").collect(Collectors.joining(","));
        String sql = "insert into " + definition.name() + " (" + columns + ") values (" + placeholders + ")";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int index = 1;
            for (Object value : values.values()) {
                ps.setObject(index++, value);
            }
            return ps;
        }, keyHolder);
        Number key = Objects.requireNonNullElse(keyHolder.getKey(), 0);
        return get(table, key.longValue());
    }

    public Map<String, Object> update(String table, long id, Map<String, Object> payload) {
        TableDefinition definition = mustFind(table);
        Map<String, Object> values = writableValues(definition, payload, false);
        if (values.isEmpty()) {
            return get(table, id);
        }
        String assignments = values.keySet().stream().map(k -> k + " = ?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>(values.values());
        args.add(id);
        jdbcTemplate.update("update " + definition.name() + " set " + assignments + " where id = ?", args.toArray());
        return get(table, id);
    }

    public void delete(String table, long id) {
        TableDefinition definition = mustFind(table);
        jdbcTemplate.update("delete from " + definition.name() + " where id = ?", id);
    }

    public Map<String, Object> get(String table, long id) {
        TableDefinition definition = mustFind(table);
        return jdbcTemplate.queryForMap("select * from " + definition.name() + " where id = ?", id);
    }

    private TableDefinition mustFind(String table) {
        return registry.find(table).orElseThrow(() -> new IllegalArgumentException("不支持的表: " + table));
    }

    private Map<String, Object> writableValues(TableDefinition definition, Map<String, Object> payload, boolean create) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String column : definition.writableColumns()) {
            if (payload.containsKey(column)) {
                Object value = payload.get(column);
                if (value instanceof String text && text.isBlank()) {
                    if (create) {
                        continue;
                    }
                    value = null;
                }
                values.put(column, value);
            }
        }
        return values;
    }
}
