package com.warehouse.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Arrival {
    private Integer id;
    private String materialCode;
    private String name;
    private String model;
    private String unit;
    private String source;
    private String supplier;
    private String waybillNo;
    private BigDecimal arrivalQuantity;
    private String packaging;
    private Integer packageCount;
    private BigDecimal weight;
    private String warehouseKeeper;
    private LocalDateTime acceptanceTime;
    private LocalDateTime shelvingTime;
    private String receiptNumber;
    private LocalDateTime arrivalTime;
    private String status;
    private String remark;
    private LocalDateTime updatedAt;
}
