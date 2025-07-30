package com.example.reve.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

  @Value("${file.upload-dir}") // application.properties에서 설정한 기본 업로드 경로 주입
  private String uploadDir;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList("jpg", "jpeg", "png", "pdf", "doc", "docx"); // 허용되는 확장자

  /**
   * 단일 파일을 지정된 서브 디렉토리 내에 저장하고 저장된 파일의 상대 경로를 반환 파일명은 현재 시간(밀리초)과 원본 파일명을 조합하여 고유하게 생성
   *
   * @param file 업로드할 MultipartFile 객체
   * @param subDirectory 게시물별로 파일을 저장할 서브 디렉토리 이름 (예: Q&A ID)
   * @return 저장된 파일의 상대 경로 (예: /uploads/qna/게시물ID/타임스탬프_파일명.jpg)
   * @throws IOException 파일 저장 중 오류 발생 시
   */
  public String saveFile(MultipartFile file, String subDirectory) throws IOException {
    if (file.isEmpty()) {
      return null; // 파일이 없으면 null 반환
    }

    // 파일 크기 검증
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IOException("파일 크기가 10MB를 초과합니다.");
    }

    // 파일 확장자 검증
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
    }
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IOException("허용되지 않는 파일 형식입니다. (허용: JPG, PNG, PDF, DOC, DOCX)");
    }

    // 최종 업로드 디렉토리 경로 생성 (기본 경로 + 서브 디렉토리)
    Path targetDirectoryPath = Paths.get(uploadDir, "qna", subDirectory);
    if (!Files.exists(targetDirectoryPath)) {
      Files.createDirectories(targetDirectoryPath); // 디렉토리가 없으면 생성
    }

    // 파일명에 현재 시간(밀리초)을 포함하여 고유성 확보
    String savedFileName = System.currentTimeMillis() + "_" + originalFilename;

    // 파일을 지정된 경로에 저장
    Path filePath = targetDirectoryPath.resolve(savedFileName);
    file.transferTo(filePath.toFile());

    // 저장된 파일의 상대 경로 반환 (웹에서 접근 가능한 경로)
    // 예: /uploads/qna/게시물ID/타임스탬프_파일명.jpg
    return "/uploads/qna/" + subDirectory + "/" + savedFileName;
  }

  /**
   * 여러 파일을 지정된 서브 디렉토리 내에 저장하고 저장된 파일들의 상대 경로 리스트를 콤마로 구분된 문자열로 반환
   *
   * @param files 업로드할 MultipartFile 리스트
   * @param subDirectory 게시물별로 파일을 저장할 서브 디렉토리 이름
   * @return 저장된 파일들의 상대 경로를 콤마로 구분한 문자열
   * @throws IOException 파일 저장 중 오류 발생 시
   */
  public String saveFiles(List<MultipartFile> files, String subDirectory) throws IOException {
    if (files == null || files.isEmpty()) {
      return null;
    }

    List<String> savedFilePaths = new ArrayList<>();
    for (MultipartFile file : files) {
      try {
        // 단일 파일 저장 메소드에 subDirectory 전달
        String filePath = saveFile(file, subDirectory);
        if (filePath != null) {
          savedFilePaths.add(filePath);
        }
      } catch (Exception e) {
        // 파일 저장 중 오류가 발생해도 다른 파일은 계속 처리
        System.err.println("파일 저장 중 오류 발생: " + e.getMessage());
        throw new IOException("파일 업로드 중 오류 발생: " + e.getMessage()); // 예외를 다시 던져서 클라이언트에게 전달
      }
    }
    // 콤마로 구분된 문자열로 반환
    return String.join(",", savedFilePaths);
  }

  /**
   * 지정된 디렉토리의 이름을 변경
   *
   * @param oldDirectoryName 기존 디렉토리 이름
   * @param newDirectoryName 새 디렉토리 이름
   * @throws IOException 디렉토리 이름 변경 중 오류 발생 시
   */
  public void renameDirectory(String oldDirectoryName, String newDirectoryName) throws IOException {
    Path oldPath = Paths.get(uploadDir, "qna", oldDirectoryName);
    Path newPath = Paths.get(uploadDir, "qna", newDirectoryName);

    if (Files.exists(oldPath)) {
      Files.move(oldPath, newPath);
    }
  }

  /**
   * 지정된 디렉토리와 그 안의 모든 내용을 삭제
   *
   * @param directoryName 삭제할 디렉토리 이름
   * @throws IOException 디렉토리 삭제 중 오류 발생 시
   */
  public void deleteDirectory(String directoryName) throws IOException {
    Path targetDirectoryPath = Paths.get(uploadDir, "qna", directoryName);
    if (Files.exists(targetDirectoryPath)) {
      Files.walk(targetDirectoryPath)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }
}
