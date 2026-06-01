package com.warehouse.entity;

import lombok.Data;

import java.util.Map;

@Data
public class StatsSummary {
    private Integer materials;
    private Map<String, Integer> byStatus;
}
