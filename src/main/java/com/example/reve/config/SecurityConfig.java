package com.example.reve.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Spring Security를 위한 Bean 설정 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * Security 제외 할 페이지 선정하는 Bean 설정
   *
   * @param http 제외 처리할 페이지를 설정 할 객체
   * @return http.build()
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/webjars/**",
                        "/member/check/**",
                        "/favicon.ico")
                    .permitAll()
                    .requestMatchers(
                        "/", "/member/signup", "/shop/**", "/board/notice/**", "/info/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/board/qna/list")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(
            form ->
                form.loginPage("/member/login") // 로그인 페이지 URL
                    .loginProcessingUrl("/member/login") // 로그인 처리 URL
                    .usernameParameter("loginId") // 사용자 이름 파라미터 (기본값 username)
                    .passwordParameter("password") // 비밀번호 파라미터 (기본값 password)
                    .defaultSuccessUrl("/", true) // 로그인 성공 시 기본 리다이렉트 URL
                    .failureUrl("/member/login?error=true") // 로그인 실패 처리 URL
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/member/logout") // 로그아웃 처리 URL
                    .logoutSuccessUrl("/") // 로그아웃 성공 시 리다이렉트될 URL
                    .invalidateHttpSession(true) // 세션 무효화
                    .deleteCookies("JSESSIONID") // 쿠키 삭제
            )
        .httpBasic(AbstractHttpConfigurer::disable);
    return http.build();
  }

  /**
   * 비밀보호 암호화를 위한 Bean 설정
   *
   * @return new BCryptPasswordEncoder()
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * AuthenticationManager Bean 설정
   *
   * @param http HttpSecurity 객체
   * @param userDetailsService UserService (UserDetailsService 구현체)
   * @param passwordEncoder PasswordEncoder
   * @return AuthenticationManager
   */
  @Bean
  public AuthenticationManager authenticationManager(
      HttpSecurity http, UserDetailsService userDetailsService, PasswordEncoder passwordEncoder)
      throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder);
    return authenticationManagerBuilder.build();
  }
}
