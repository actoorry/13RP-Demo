package com.boyu.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 13RP 决策操作系统后端启动类（V0.4 九大业务域 + 决策演示）。
 * P0 = 博宇企业管理平台功能复现（九大业务域）+ 国内场景决策演示。
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.boyu.demo.module")
public class BoyuDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoyuDemoApplication.class, args);
    }
}
