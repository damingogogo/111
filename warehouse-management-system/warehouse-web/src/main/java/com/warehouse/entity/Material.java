package com.warehouse.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Material {
    private Integer id;
    private String materialCode;
    private String name;
    private String model;
    private String unit;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
