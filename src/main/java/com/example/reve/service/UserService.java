package com.example.reve.service;

import java.io.IOException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.CustomUserDetails;
import com.example.reve.domain.User;
import com.example.reve.dto.*;
import com.example.reve.repository.CouponRepository;
import com.example.reve.repository.PointRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
/* User 테이블에 대한 서비스 */
public class UserService implements UserDetailsService { // UserDetailsService 구현

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CouponService couponService;
  private final ProfileUrlService profileUrlService;
  private final CouponRepository couponRepository;
  private final PointRepository pointRepository;

  /**
   * 회원 가입 서비스
   *
   * @param create (아이디,비밀번호, 이메일, 휴대폰 번호)
   */
  public Long signup(CreateUserDTO create) {
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
    return user.getUserId();
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

  // 아이디 중복 검사
  public boolean checklogin(String loginId) {
    log.info("아이디 중복 검사 {}", userRepository.existsByLoginId(loginId));
    return userRepository.existsByLoginId(loginId);
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

    // CustomUserDetails 객체로 변환하여 반환
    return new CustomUserDetails(user);
  }

  /**
   * 회원 정보 수정 서비스
   *
   * @param updateProfileDTO (프로필 사진, 이름, 닉네임, 이메일, 생일,휴대폰 번호)
   * @return user
   */
  public UpdateProfileDTO update(
      UpdateProfileDTO updateProfileDTO, String loginId, MultipartFile profileImg) {
    // 로그인 아이디가 같은 회원
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    log.info("profile image : {}", profileImg);

    if (profileImg != null && !profileImg.isEmpty()) {
      try {
        if (user.getProfileUrl() != null) {
          profileUrlService.deleteProfileImage(user.getProfileUrl());
        }
        // 파일 저장
        String userIdStr = Long.toString(user.getUserId());
        log.info("userIdStr : {}", userIdStr);
        String saveUrl = profileUrlService.saveProfileImage(profileImg, "profile", userIdStr);
        log.info("saveUrl : {}", saveUrl);
        // 프로필 사진 경로
        user.setProfileUrl(saveUrl);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    // 이름
    user.setName(updateProfileDTO.getName());
    // 생일
    user.setBirthday(updateProfileDTO.getBirthday());
    // 휴대폰 번호
    user.setPhone(updateProfileDTO.getPhone());
    // 닉네임
    user.setNickname(updateProfileDTO.getNickname());

    log.info("회원 정보 변경 : {}", user);
    // 수정하기
    userRepository.save(user);
    // 생일 등록 시 생일쿠폰 발급
    if (updateProfileDTO.getBirthday() != null) {
      couponService.birthdayCoupon(user);
    }
    return userRepository.findUserUpdate(loginId);
  }

  public UpdateProfileDTO profileById(String loginId) {
    return userRepository.findUserUpdate(loginId);
  }

  public boolean updatePassword(NewPasswordDTO newPasswordDTO, String loginId) {
    User user = userRepository.findByLoginId(loginId).orElseThrow();
    // 비밀번호 일치 비교
    if (!passwordEncoder.matches(newPasswordDTO.getPassword(), user.getPassword())) {
      log.error("비밀번호 불일치");
      return false;
    }
    // 새 비밀번호가 기존과 같은지 확인
    if (passwordEncoder.matches(newPasswordDTO.getNewPassword(), user.getPassword())) {
      log.error("현재 비밀번호와 새 비밀번호가 일치");
      return false;
    }
    // 새 비밀번호 암호화 및 저장
    String encodedNewPassword = passwordEncoder.encode(newPasswordDTO.getNewPassword());
    user.setPassword(encodedNewPassword);
    userRepository.save(user);
    log.info("비밀번호 성공");
    return true;
  }

  public MypageDTO selectMypage(String loginId) {
    return userRepository.findUserInfoByLoginId(loginId);
  }

  // 전체 회원 수 조회
  public long getTotalUserCount() {
    return userRepository.count();
  }

  // 모든 사용자 조회
  public java.util.List<User> getAllUsers() {
    return userRepository.findAll();
  }

  // 사용자 삭제
  @Transactional
  public void deleteUser(Long userId) {
    // 해당 사용자의 모든 쿠폰 삭제
    couponRepository.deleteAll(couponRepository.findByUser_UserId(userId));
    userRepository.deleteById(userId);
  }

  // 사용자 권한 변경
  @Transactional
  public void updateUserRole(Long userId, com.example.reve.domain.Role newRole) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    user.setRole(newRole);
    userRepository.save(user);
  }

  public int getPointByUserId(Long userId) {
    return pointRepository.getTotalPointByUserId(userId);
  }
}
