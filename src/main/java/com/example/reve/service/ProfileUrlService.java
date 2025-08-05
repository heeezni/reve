package com.example.reve.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

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

  // 허용 이미지 확장자
  private static final Set<String> ALLOWED_IMAGE_EXTENSIONS =
      Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "heic");

  // 확장자 검사
  private boolean isImageFile(String filename) {
    if (filename == null || !filename.contains(".")) return false;
    String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    return ALLOWED_IMAGE_EXTENSIONS.contains(ext);
  }

  /** 프로필 이미지 저장 */
  public String saveProfileImage(MultipartFile file, String userId, String loginId)
      throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }

    String originalName = file.getOriginalFilename();
    if (!isImageFile(originalName)) {
      throw new IOException("지원하지 않는 이미지 확장자입니다.");
    }

    // 저장 경로 설정 및 디렉토리 생성
    Path saveDir = profileBuilder.getProfilePath("profile", userId);
    if (!Files.exists(saveDir)) {
      Files.createDirectories(saveDir);
    }

    // 안전한 파일명 생성
    String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    String safeFilename = UUID.randomUUID() + "_" + loginId + "." + ext;

    // 파일 저장
    Path savePath = saveDir.resolve(safeFilename);
    file.transferTo(savePath.toFile());

    // DB에 저장할 URL 생성
    String fileUrl = profileBuilder.buildUrl("profile", userId, safeFilename);

    return fileUrl;
  }

  /** 프로필 이미지 삭제 */
  public void deleteProfileImage(String fileUrl) throws IOException {
    if (fileUrl == null || !fileUrl.startsWith("/uploads/")) return;

    String relativePath = fileUrl.substring("/uploads/".length());

    // OS 호환 경로로 변환
    Path filePath =
        Path.of(profileBuilder.getRealUrl(), relativePath.replace("/", java.io.File.separator));

    Files.deleteIfExists(filePath);
  }
}
