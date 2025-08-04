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
      log.warn("업로드된 파일이 비어 있습니다.");
      return null;
    }

    String originalName = file.getOriginalFilename();
    if (!isImageFile(originalName)) {
      log.warn("허용되지 않은 확장자 파일 업로드 시도: {}", originalName);
      throw new IOException("지원하지 않는 이미지 확장자입니다.");
    }

    // 저장 경로 설정 및 디렉토리 생성
    Path saveDir = profileBuilder.getProfilePath("profile", userId);
    if (!Files.exists(saveDir)) {
      Files.createDirectories(saveDir);
      log.info("프로필 이미지 디렉토리 생성됨: {}", saveDir.toString());
    }

    // 안전한 파일명 생성
    String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    String safeFilename = UUID.randomUUID() + "_" + loginId + "." + ext;

    // 파일 저장
    Path savePath = saveDir.resolve(safeFilename);
    file.transferTo(savePath.toFile());

    // DB에 저장할 URL 생성
    String fileUrl = profileBuilder.buildUrl("profile", userId, safeFilename);
    log.info("프로필 이미지 저장됨: {}", fileUrl);

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
    log.info("프로필 이미지 삭제됨: {}", filePath.toString());
  }
}
