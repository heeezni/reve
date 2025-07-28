package com.example.reve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class ReveApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReveApplication.class, args);
  }
}
