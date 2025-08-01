package com.example.reve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.reve.domain.User;
import com.example.reve.dto.MypageDTO;
import com.example.reve.dto.UpdateProfileDTO;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  // 아이디 중복 검사
  boolean existsByLoginId(String loginId);

  @Query(
      "SELECT new com.example.reve.dto.MypageDTO(u.email, u.createdAt) FROM User u WHERE u.loginId = :loginId")
  MypageDTO findUserInfoByLoginId(String loginId);

  @Query(
      "SELECT new com.example.reve.dto.UpdateProfileDTO(u.profileUrl, u.name,u.birthday,u.phone,u.nickname) FROM User u WHERE u.loginId = :loginId")
  UpdateProfileDTO findUserUpdate(String loginId);
}
