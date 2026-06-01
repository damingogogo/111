package com.danzi.emotion.controller;

import com.danzi.emotion.model.ApiResponse;
import com.danzi.emotion.model.TableDefinition;
import com.danzi.emotion.service.CrudService;
import com.danzi.emotion.service.TableRegistry;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class CrudController {
    private final CrudService crudService;
    private final TableRegistry tableRegistry;

    public CrudController(CrudService crudService, TableRegistry tableRegistry) {
        this.crudService = crudService;
        this.tableRegistry = tableRegistry;
    }

    @GetMapping("/tables")
    public ApiResponse<List<TableDefinition>> tables() {
        return ApiResponse.ok(tableRegistry.all());
    }

    @GetMapping("/{table}")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable String table,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(crudService.list(table, page, size, keyword));
    }

    @PostMapping("/{table}")
    public ApiResponse<Map<String, Object>> create(@PathVariable String table, @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(crudService.create(table, payload));
    }

    @PutMapping("/{table}/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String table, @PathVariable long id, @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(crudService.update(table, id, payload));
    }

    @DeleteMapping("/{table}/{id}")
    public ApiResponse<Void> delete(@PathVariable String table, @PathVariable long id) {
        crudService.delete(table, id);
        return ApiResponse.ok(null);
    }
}
