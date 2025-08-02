package com.example.reve.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

  @Value("${file.upload-dir}")
  private String uploadDir;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "doc", "docx");

  /**
   * 파일을 지정된 도메인과 ID에 해당하는 디렉토리에 저장하고, 웹 접근 가능 경로를 반환합니다.
   *
   * @param file 업로드할 MultipartFile 객체
   * @param domainType 파일을 그룹화할 도메인 타입 (e.g., "qna", "notice", "perfume")
   * @param domainId 도메인의 고유 ID (e.g., 게시물 ID, 상품 ID)
   * @return 저장된 파일의 상대 경로 (e.g., /uploads/notice/12/timestamp_filename.jpg)
   * @throws IOException 파일 저장 중 오류 발생 시
   */
  public String saveFile(MultipartFile file, String domainType, String domainId)
      throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }

    validateFile(file);

    Path targetDirectoryPath = Paths.get(uploadDir, domainType, domainId);
    Files.createDirectories(targetDirectoryPath);

    String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
    String savedFileName = System.currentTimeMillis() + "_" + originalFilename;

    Path filePath = targetDirectoryPath.resolve(savedFileName);
    file.transferTo(filePath.toFile());

    return String.format("/uploads/%s/%s/%s", domainType, domainId, savedFileName);
  }

  /**
   * 여러 파일을 지정된 도메인과 ID에 해당하는 디렉토리에 저장하고, 저장된 파일 경로 리스트를 반환합니다.
   *
   * @param files 업로드할 MultipartFile 리스트
   * @param domainType 파일을 그룹화할 도메인 타입
   * @param domainId 도메인의 고유 ID
   * @return 저장된 파일들의 상대 경로 리스트
   * @throws IOException 파일 저장 중 오류 발생 시
   */
  public List<String> saveFiles(List<MultipartFile> files, String domainType, String domainId)
      throws IOException {
    if (files == null || files.isEmpty()) {
      return new ArrayList<>();
    }

    List<String> savedFilePaths = new ArrayList<>();
    for (MultipartFile file : files) {
      if (file != null && !file.isEmpty()) {
        String filePath = saveFile(file, domainType, domainId);
        savedFilePaths.add(filePath);
      }
    }
    return savedFilePaths;
  }

  /**
   * 지정된 파일 경로에 해당하는 파일을 삭제합니다.
   *
   * @param fileUrl 삭제할 파일의 웹 접근 경로 (e.g., /uploads/notice/12/file.jpg)
   * @throws IOException 파일 삭제 중 오류 발생 시
   */
  public void deleteFile(String fileUrl) throws IOException {
    if (fileUrl == null || fileUrl.trim().isEmpty()) {
      return;
    }
    // URL에서 실제 파일 시스템 경로를 구성합니다.
    // 예: /uploads/notice/12/file.jpg -> /path/to/uploadDir/notice/12/file.jpg
    String relativePath =
        fileUrl.startsWith("/uploads/") ? fileUrl.substring("/uploads/".length()) : fileUrl;
    Path filePath = Paths.get(uploadDir).resolve(relativePath);

    if (Files.exists(filePath)) {
      Files.delete(filePath);
    }
  }

  /**
   * 지정된 도메인 ID에 해당하는 디렉토리와 그 안의 모든 내용을 삭제합니다.
   *
   * @param domainType 도메인 타입
   * @param domainId 삭제할 디렉토리의 ID
   * @throws IOException 디렉토리 삭제 중 오류 발생 시
   */
  public void deleteDirectory(String domainType, String domainId) throws IOException {
    Path targetDirectoryPath = Paths.get(uploadDir, domainType, domainId);
    if (Files.exists(targetDirectoryPath)) {
      try (java.util.stream.Stream<Path> walk = Files.walk(targetDirectoryPath)) {
        walk.sorted(Comparator.reverseOrder())
            .forEach(
                path -> {
                  try {
                    Files.delete(path);
                  } catch (IOException e) {
                    System.err.println("파일/디렉토리 삭제 실패: " + path + " - " + e.getMessage());
                  }
                });
      }
    }
  }

  /**
   * 파일의 크기와 확장자를 검증합니다.
   *
   * @param file 검증할 MultipartFile
   * @throws IOException 검증 실패 시
   */
  private void validateFile(MultipartFile file) throws IOException {
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IOException("파일 크기가 10MB를 초과합니다: " + file.getOriginalFilename());
    }

    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IOException("허용되지 않는 파일 형식입니다: " + originalFilename);
    }
  }
}
