package com.boyu.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 13RP 决策演示后端启动类。
 * P0 = 博宇四方管理端功能复现 + 国内场景决策演示。
 */
@SpringBootApplication
@EnableScheduling
public class BoyuDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoyuDemoApplication.class, args);
    }
}
