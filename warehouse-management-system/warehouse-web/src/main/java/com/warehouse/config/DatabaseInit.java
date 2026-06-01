package com.warehouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@Configuration
public class DatabaseInit {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS warehouse_mgmt " +
                    "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            jdbcTemplate.execute("USE warehouse_mgmt");
        } catch (Exception ignored) {
            // The production datasource connects directly to warehouse_mgmt with a limited DB account.
        }

        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS materials (\n" +
            "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    material_code VARCHAR(64) NOT NULL UNIQUE COMMENT '物资编码',\n" +
            "    name VARCHAR(255) NOT NULL COMMENT '名称',\n" +
            "    model VARCHAR(255) DEFAULT '' COMMENT '型号',\n" +
            "    unit VARCHAR(32) DEFAULT '' COMMENT '单位',\n" +
            "    remark VARCHAR(500) DEFAULT '' COMMENT '备注',\n" +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
            "    INDEX idx_name (name)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物资基础信息'"
        );

        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS arrivals (\n" +
            "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    material_code VARCHAR(64) NOT NULL COMMENT '物资编码',\n" +
            "    name VARCHAR(255) DEFAULT '' COMMENT '名称(快照)',\n" +
            "    model VARCHAR(255) DEFAULT '' COMMENT '型号',\n" +
            "    unit VARCHAR(32) DEFAULT '' COMMENT '单位',\n" +
            "    `source` VARCHAR(255) DEFAULT '' COMMENT '来源',\n" +
            "    supplier VARCHAR(255) DEFAULT '' COMMENT '供应商',\n" +
            "    waybill_no VARCHAR(128) DEFAULT '' COMMENT '运单号',\n" +
            "    arrival_quantity DECIMAL(18,3) DEFAULT 0 COMMENT '到货数量',\n" +
            "    packaging VARCHAR(255) DEFAULT '' COMMENT '包装',\n" +
            "    package_count INT DEFAULT 0 COMMENT '件数',\n" +
            "    weight DECIMAL(18,3) DEFAULT 0 COMMENT '重量',\n" +
            "    warehouse_keeper VARCHAR(64) DEFAULT '' COMMENT '管库员',\n" +
            "    acceptance_time DATETIME NULL COMMENT '验收完成时间',\n" +
            "    shelving_time DATETIME NULL COMMENT '上架时间',\n" +
            "    receipt_number VARCHAR(64) DEFAULT '' COMMENT '入库单号',\n" +
            "    arrival_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '到货登记时间',\n" +
            "    status VARCHAR(16) DEFAULT '待认领' COMMENT '状态',\n" +
            "    remark VARCHAR(500) DEFAULT '' COMMENT '备注',\n" +
            "    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
            "    INDEX idx_code (material_code),\n" +
            "    INDEX idx_status (status),\n" +
            "    INDEX idx_supplier (supplier),\n" +
            "    INDEX idx_keeper (warehouse_keeper),\n" +
            "    INDEX idx_arrival_time (arrival_time)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='到货登记与入库管理'"
        );

        addColumnIfMissing("arrivals", "source", "`source` VARCHAR(255) DEFAULT '' COMMENT '来源' AFTER unit");
        addColumnIfMissing("arrivals", "waybill_no", "waybill_no VARCHAR(128) DEFAULT '' COMMENT '运单号' AFTER supplier");
        addColumnIfMissing("arrivals", "packaging", "packaging VARCHAR(255) DEFAULT '' COMMENT '包装' AFTER arrival_quantity");

        jdbcTemplate.execute(
            "CREATE TABLE IF NOT EXISTS users (\n" +
            "    id INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',\n" +
            "    password VARCHAR(255) NOT NULL COMMENT '密码',\n" +
            "    real_name VARCHAR(64) DEFAULT '' COMMENT '姓名',\n" +
            "    role VARCHAR(32) DEFAULT 'USER' COMMENT '角色',\n" +
            "    status TINYINT DEFAULT 1 COMMENT '状态:1启用 0禁用',\n" +
            "    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,\n" +
            "    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户'"
        );

        // 插入/更新默认用户，确保密码正确
        String[][] users = {
            {"admin", "admin123", "系统管理员", "ADMIN"},
            {"keeper", "keeper123", "库管员A", "KEEPER"},
            {"keeper2", "keeper123", "库管员B", "KEEPER"},
            {"keeper3", "keeper123", "库管员C", "KEEPER"},
            {"checker", "checker123", "验收员", "CHECKER"},
        };
        for (String[] u : users) {
            String encoded = passwordEncoder.encode(u[1]);
            jdbcTemplate.update(
                "INSERT INTO users(username, password, real_name, role, status) VALUES (?, ?, ?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE password=?, real_name=?, role=?, status=1",
                u[0], encoded, u[2], u[3], encoded, u[2], u[3]
            );
        }
    }

    private void addColumnIfMissing(String table, String column, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, table, column);
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + ddl);
        }
    }
}
