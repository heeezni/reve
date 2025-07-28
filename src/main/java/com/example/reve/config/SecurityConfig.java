package com.example.reve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/***
 * Spring Security를 위한 Bean 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  /***
   * Security 제외 할 페이지 선정하는 Bean 설정
   * @param http 제외 처리할 페이지를 설정 할 객체
   * @return  http.build()
   * @throws Exception
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable);
    ;
    return http.build();
  }

  /***
   * 비밀보호 암호화를 위한 Bean 설정
   * @return new BCryptPasswordEncoder()
   */
  @Bean
  public PasswordEncoder PasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    // 빈 유저 설정: 아무 사용자도 등록하지 않음
    return new InMemoryUserDetailsManager();
  }
}
