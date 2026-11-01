package com.empasy.sync.kkumz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kkumz(꿈즈) AI 브랜드·멀티사이트 빌더 & 창업 팀빌딩 커뮤니티 서버 메인 애플리케이션
 */
@Slf4j
@SpringBootApplication
public class KkumzApplication {

    public static void main(String[] args) {
        SpringApplication.run(KkumzApplication.class, args);
        log.info("Kkumz AI Brand & Multi-Site Builder Server started successfully.");
    }
}
