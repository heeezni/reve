package com.example.reve.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.User;
import com.example.reve.dto.CreateUserDTO;
import com.example.reve.dto.LoginUserDTO;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void signup(CreateUserDTO create) {
    log.info("회원 가입 서비스 호출");
    // 아이디 중복 확인
    if (userRepository.findByLoginId(create.getLoginId()).isPresent()) {
      throw new DuplicateKeyException("이미 있는 아이디 입니다");
    }
    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(create.getPassword());
    log.info("암호화 성공 : {}", encodedPassword);
    // DB 저장
    String phone = create.getFirstNum() + create.getMiddleNum() + create.getLastNum();
    User user =
        User.builder()
            .loginId(create.getLoginId())
            .password(encodedPassword)
            .name(create.getName())
            .phone(phone)
            .email(create.getEmail())
            .build();
    log.info("회원 가입 유저 {}", user);
    userRepository.save(user);
  }

  public User login(LoginUserDTO loginUser) {
    log.info("로그인 서비스 호출");
    User user =
        userRepository
            .findByLoginId(loginUser.getLoginId())
            .orElseThrow( // 조회해서 없으면 에러메시지 보내기
                () -> new UsernameNotFoundException("아이디 없음"));
    if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("비밀번호가 일치하지 않음");
    }
    log.info("로그인 성공 : {}", user);

    return user;
  }
}
