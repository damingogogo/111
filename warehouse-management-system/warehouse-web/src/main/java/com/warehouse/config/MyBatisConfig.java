package com.warehouse.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.warehouse.mapper")
public class MyBatisConfig {
}
