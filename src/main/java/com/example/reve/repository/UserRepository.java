package com.example.reve.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.reve.domain.User;
import com.example.reve.dto.MypageDTO;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByLoginId(String loginId);

  @Query(
      "SELECT new com.example.reve.dto.MypageDTO(u.email, u.createdAt) FROM User u WHERE u.loginId = :loginId")
  MypageDTO findUserInfoByLoginId(String loginId);
}
