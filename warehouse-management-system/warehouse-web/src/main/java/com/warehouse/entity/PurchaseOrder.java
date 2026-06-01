package com.warehouse.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseOrder {
    private Integer id;
    private String orderNo;
    private String orderLine;
    private String materialCode;
    private String name;
    private String model;
    private String unit;
    private String supplier;
    private BigDecimal orderQuantity;
    private String orderDate;
    private String deliveryDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
