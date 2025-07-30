package com.example.reve.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

  @Value("${qna.upload.path}") // application.properties에서 설정한 기본 업로드 경로 주입
  private String uploadPath;

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

    // 최종 업로드 디렉토리 경로 생성 (기본 경로 + 서브 디렉토리)
    Path targetDirectoryPath = Paths.get(uploadPath, subDirectory);
    if (!Files.exists(targetDirectoryPath)) {
      Files.createDirectories(targetDirectoryPath); // 디렉토리가 없으면 생성
    }

    // 파일명 생성: 현재 시간(밀리초) + 원본 파일명 (중복 방지를 위해 UUID도 조합 가능)
    String originalFilename = file.getOriginalFilename();
    String extension = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    // 파일명에 현재 시간(밀리초)을 포함하여 고유성 확보
    String savedFileName = System.currentTimeMillis() + "_" + originalFilename;
    // 또는 UUID와 조합: String savedFileName = UUID.randomUUID().toString() + "_" +
    // System.currentTimeMillis() + extension;

    // 파일을 지정된 경로에 저장
    Path filePath = targetDirectoryPath.resolve(savedFileName);
    file.transferTo(filePath.toFile());

    // 저장된 파일의 상대 경로 반환 (웹에서 접근 가능한 경로)
    // 예: /uploads/qna/게시물ID/타임스탬프_파일명.jpg
    return uploadPath + subDirectory + "/" + savedFileName;
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
    Path oldPath = Paths.get(uploadPath, oldDirectoryName);
    Path newPath = Paths.get(uploadPath, newDirectoryName);

    if (Files.exists(oldPath)) {
      Files.move(oldPath, newPath);
    }
  }

  /**
   * 지정된 디렉토리와 그 안의 모든 내용을 삭제합니다.
   *
   * @param directoryName 삭제할 디렉토리 이름 (예: 임시 UUID 또는 Q&A PK)
   * @throws IOException 디렉토리 삭제 중 오류 발생 시
   */
  public void deleteDirectory(String directoryName) throws IOException {
    Path targetDirectoryPath = Paths.get(uploadPath, directoryName);
    if (Files.exists(targetDirectoryPath)) {
      Files.walk(targetDirectoryPath)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }
}
