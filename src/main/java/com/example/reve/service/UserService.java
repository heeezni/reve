package com.example.reve.service;

import java.util.Collections;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.User;
import com.example.reve.dto.CreateUserDTO;
import com.example.reve.dto.LoginUserDTO;
import com.example.reve.dto.NewPasswordDTO;
import com.example.reve.dto.UpdateProfileDTO;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
/** User 테이블에 대한 서비스 */
public class UserService implements UserDetailsService { // UserDetailsService 구현

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * 회원 가입 서비스
   *
   * @param create (아이디,비밀번호, 이메일, 휴대폰 번호)
   */
  public void signup(CreateUserDTO create) {
    log.info("회원 가입 서비스 호출");
    // 아이디 중복 확인
    if (userRepository.findByLoginId(create.getLoginId()).isPresent()) {
      throw new DuplicateKeyException("이미 있는 아이디 입니다");
    }
    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(create.getPassword());
    log.info("암호화 성공 : {}", encodedPassword);
    // 휴대폰 번호 조합
    String phone = create.getFirstNum() + create.getMiddleNum() + create.getLastNum();
    // DB 저장
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

  /**
   * 로그인 서비스
   *
   * @param loginUser (로그인 아이디, 비밀번호)
   * @return user
   */
  public User login(LoginUserDTO loginUser) {
    log.info("로그인 서비스 호출");
    // 로그인 아이디 비교
    User user =
        userRepository
            .findByLoginId(loginUser.getLoginId())
            .orElseThrow( // 조회해서 없으면 에러메시지 보내기
                () -> new UsernameNotFoundException("아이디 없음"));
    // 암호화된 비밀번호와 비교
    if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("비밀번호가 일치하지 않음");
    }
    log.info("로그인 성공 : {}", user);

    return user;
  }

  /**
   * Spring Security의 UserDetailsService 인터페이스 구현 사용자 이름(loginId)으로 사용자 정보를 로드
   *
   * @param username 사용자의 로그인 ID
   * @return UserDetails 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByLoginId(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with loginId: " + username));

    // Spring Security의 User 객체로 변환하여 반환
    return new org.springframework.security.core.userdetails.User(
        user.getLoginId(),
        user.getPassword(),
        Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())) // 권한 설정
        );
  }

  /**
   * 회원 정보 수정 서비스
   *
   * @param updateProfileDTO (프로필 사진, 이름, 닉네임, 이메일, 생일,휴대폰 번호)
   * @return
   */
  public User update(UpdateProfileDTO updateProfileDTO) {
    // 로그인 아이디가 같은 회원
    User user = userRepository.findByLoginId(updateProfileDTO.getLoginId()).orElseThrow();
    // 이름
    user.setName(updateProfileDTO.getName());
    // 닉네임
    user.setNickname(updateProfileDTO.getNickname());
    // 프로필 사진 경로
    user.setProfileUrl(updateProfileDTO.getProfileUrl());
    // 생일
    user.setBirthday(updateProfileDTO.getBirthday());
    // 휴대폰 번호
    user.setPhone(updateProfileDTO.getPhone());

    log.info("회원 정보 변경 : {}", user);
    // 수정하기
    userRepository.save(user);
    return user;
  }

  /**
   * 비밀번호 변경 서비스 -기존 비밀번호 확인 후 새 비밀번호 암호화 후 저장
   *
   * @param newPasswordDTO (로그인 아이디, 기존 비밀번호, 변경 비밀번호)
   */
  public boolean updatePassword(NewPasswordDTO newPasswordDTO) {
    // 로그인 아이디가 같은 회원 정보 가져오기
    User user = userRepository.findByLoginId(newPasswordDTO.getLoginId()).orElseThrow();
    // 기존 비밀번호가 일치한지 확인하기
    if (passwordEncoder.matches(newPasswordDTO.getPassword(), user.getPassword())) {
      // 새 비밀번호와 기존 비밀번호가 일치한지 비교
      if (!newPasswordDTO.getPassword().equals(newPasswordDTO.getNewPassword())) {
        // 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPasswordDTO.getNewPassword());
        user.setPassword(encodedNewPassword);
        // 저장
        userRepository.save(user);
        return true;
      } else {
        throw new BadCredentialsException("비밀번호의 변경사함이 없음");
      }
    } else {
      throw new BadCredentialsException("비밀번호가 일치하지 않음");
    }
  }
}
