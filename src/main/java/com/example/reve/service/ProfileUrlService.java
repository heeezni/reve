package com.example.reve.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.config.ProfileBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileUrlService {
  private final ProfileBuilder profileBuilder;

  public String saveProfileImage(MultipartFile file, String userId, String loginId)
      throws IOException {
    if (file.isEmpty()) return null;

    // 프로필 디렉토리 저장 경로
    Path savePath = profileBuilder.getProfilePath("profile", userId);
    Files.createDirectories(savePath);

    // 고유한 파일명 생성
    String originalName = file.getOriginalFilename();
    String filename = System.currentTimeMillis() + "_" + loginId + "_" + originalName;

    // 파일 저장
    file.transferTo(savePath.resolve(filename).toFile());

    // DB에 저장할 URL 생성
    return profileBuilder.buildUrl("profile", userId, filename);
  }

  /** 프로필 이미지 삭제 */
  public void deleteProfileImage(String fileUrl) throws IOException {
    if (fileUrl == null || !fileUrl.startsWith("/uploads/")) return;

    // URL에서 상대 경로 추출
    String relativePath = fileUrl.substring("/uploads/".length());

    // 실제 파일 경로로 변환
    Path filePath = Path.of(profileBuilder.getRealUrl(), relativePath);

    Files.deleteIfExists(filePath);
  }
}
