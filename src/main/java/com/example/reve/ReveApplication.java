package com.example.reve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // 엔티티의 생성/수정 시간 자동 기록 기능이 동작하도록 JPA Auditing 기능을 활성화
public class ReveApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReveApplication.class, args);
  }
}
