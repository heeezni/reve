package com.example.reve.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.Perfume;
import com.example.reve.dto.PerfumeDetailResponseDTO;
import com.example.reve.dto.PerfumeListResponseDTO;
import com.example.reve.dto.PerfumeSaveRequestDTO;
import com.example.reve.repository.PerfumeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfumeService {

  private final PerfumeRepository perfumeRepository;

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Value("${file.image-url-prefix}")
  private String imageUrlPrefix;

  // ============================= 향수 등록 및 삭제하는 로직임(CRUD 중 C, D) ==================================
  // 관리자 향수 등록
  @Transactional
  public void savePerfume(PerfumeSaveRequestDTO requestDto) {
    Perfume perfume =
        Perfume.builder()
            .perfumeName(requestDto.getPerfumeName())
            .scent(requestDto.getScent())
            .descriptionTitle(requestDto.getDescriptionTitle())
            .description(requestDto.getDescription())
            .stock(requestDto.getStock())
            .price(requestDto.getPrice())
            .discount(requestDto.getDiscount())
            .imageUrl(requestDto.getImageUrl())
            .volume(requestDto.getVolume())
            .hoverImageUrl(requestDto.getHoverImageUrl())
            .build();
    perfumeRepository.save(perfume);
  }

  // 향수 등록 시 이미지 등록하는 로직
  // 향수 등록 시 이미지 등록하는 로직
  public String storeImage(MultipartFile file) {
    if (file.isEmpty()) {
      return null; // 빈 파일이면 null 반환
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String uniqueFilename = UUID.randomUUID().toString() + extension;

      // perfume 폴더 하위로 저장
      File directory = new File(uploadDir + "/perfume/");
      if (!directory.exists()) {
        directory.mkdirs(); // 디렉토리 없으면 생성
      }

      File saveFile = new File(directory, uniqueFilename);
      file.transferTo(saveFile);

      // 저장된 이미지 URL 반환 (perfume 하위 경로로 반환)
      return imageUrlPrefix + "perfume/" + uniqueFilename;
    } catch (IOException e) {
      throw new RuntimeException("이미지 저장 실패", e);
    }
  }

  // 관리자의 향수 삭제 로직 (예외 발생하면 롤백)
  @Transactional
  public void deletePerfume(Long perfumeId) {
    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(() -> new IllegalArgumentException("해당 향수가 존재하지 않습니다."));

    // 2. 향수 DB에서 삭제
    perfumeRepository.delete(perfume);

    // 트랜잭션 커밋 이후 이미지 삭제
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            deleteImage(perfume.getImageUrl());
            deleteImage(perfume.getHoverImageUrl());
          }
        });
  }

  private void deleteImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return;

    String filename = imageUrl.replace(imageUrlPrefix, "");
    String fullPath = uploadDir + filename;

    File file = new File(fullPath);
    if (file.exists()) {
      boolean deleted = file.delete();
      if (!deleted) {
        throw new RuntimeException("이미지 삭제 실패: " + fullPath);
      }
    }
  }

  // 이미지 삭제하는 로직
  @Transactional
  public void deletePerfumes(List<Long> perfumeIdList) {
    List<Perfume> perfumes = perfumeRepository.findAllById(perfumeIdList);

    List<String> imageUrls =
        perfumes.stream().map(Perfume::getImageUrl).collect(Collectors.toList());

    List<String> hoverImageUrls =
        perfumes.stream().map(Perfume::getHoverImageUrl).collect(Collectors.toList());

    perfumeRepository.deleteAllByIdInBatch(perfumeIdList);

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            imageUrls.forEach(this::safeDelete);
            hoverImageUrls.forEach(this::safeDelete);
          }

          private void safeDelete(String url) {
            if (url != null && !url.isBlank()) {
              deleteImage(url);
            }
          }
        });
  }

  // ============================= 향수 수정하는 로직임(CRUD 중 U) ==========================================
  @Transactional
  public void updatePerfume(
      Long perfumeId,
      PerfumeSaveRequestDTO requestDto,
      MultipartFile imageFile,
      MultipartFile hoverImageFile) {

    Perfume perfume =
        perfumeRepository
            .findById(perfumeId)
            .orElseThrow(() -> new IllegalArgumentException("해당 향수가 존재하지 않습니다."));

    // 이미지 파일 변경이 있다면 저장 및 기존 이미지 삭제 처리
    if (imageFile != null && !imageFile.isEmpty()) {
      deleteImage(perfume.getImageUrl());
      String newImageUrl = storeImage(imageFile);
      if (newImageUrl != null) { // null 체크 추가
        perfume.setImageUrl(newImageUrl);
      }
    }

    if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
      deleteImage(perfume.getHoverImageUrl());
      String newHoverImageUrl = storeImage(hoverImageFile);
      if (newHoverImageUrl != null) { // null 체크 추가
        perfume.setHoverImageUrl(newHoverImageUrl);
      }
    }

    // 향수 정보 수정
    perfume.setPerfumeName(requestDto.getPerfumeName());
    perfume.setScent(requestDto.getScent());
    perfume.setDescriptionTitle(requestDto.getDescriptionTitle());
    perfume.setDescription(requestDto.getDescription());
    perfume.setVolume(requestDto.getVolume());
    perfume.setPrice(requestDto.getPrice());
    perfume.setDiscount(requestDto.getDiscount());
    perfume.setStock(requestDto.getStock());

    // 따로 save() 호출하지 않아도 트랜잭션 커밋 시 변경 반영됨
  }

  // ============================= 향수 조회하는 로직임(CRUD 중 R) ==========================================
  // 모든 향수
  public List<PerfumeListResponseDTO> getAllPerfumes() {
    List<Perfume> perfumes = perfumeRepository.findAllByOrderByCreatedAtDesc();
    return perfumes.stream().map(PerfumeListResponseDTO::fromEntity).collect(Collectors.toList());
  }

  // 검색어로 향수를 검색
  public List<Perfume> searchByName(String search) {
    return perfumeRepository.findByPerfumeNameContainingIgnoreCase(search);
  }

  // 향 종류별
  public List<Perfume> findAllByScent(String scent) {
    return perfumeRepository.findAllByScent(scent);
  }

  // 최신 등록순
  public List<Perfume> findAllByLatest() {
    return perfumeRepository.findAllByOrderByCreatedAtDesc();
  }

  // 낮은 가격순
  public List<Perfume> findAllByPriceAsc() {
    return perfumeRepository.findAllByOrderByPriceAsc();
  }

  // 높은 가격순
  public List<Perfume> findAllByPriceDesc() {
    return perfumeRepository.findAllByOrderByPriceDesc();
  }

  // 리뷰 많은순
  public List<Perfume> findAllByReviewCount() {
    return perfumeRepository.findAllOrderByReviewCountDesc();
  }

  // 향 + 정렬 조합
  public List<Perfume> findByScentAndSort(String scent, String sort) {
    if (sort == null || sort.isEmpty()) {
      return perfumeRepository.findByScentOrderByCreatedAtDesc(scent);
    }

    return switch (sort) {
      case "price_asc" -> perfumeRepository.findByScentOrderByPriceAsc(scent);
      case "price_desc" -> perfumeRepository.findByScentOrderByPriceDesc(scent);
      case "review" -> perfumeRepository.findByScentOrderByReviewCountDesc(scent);
      default -> perfumeRepository.findByScentOrderByCreatedAtDesc(scent);
    };
  }

  // 향수 한 건 조회
  public PerfumeDetailResponseDTO getPerfumeDetail(Long perfumeId) {
    Perfume perfume =
        perfumeRepository
            .findByPerfumeId(perfumeId)
            .orElseThrow(() -> new RuntimeException("해당 향수를 찾을 수 없습니다."));

    return PerfumeDetailResponseDTO.fromEntity(perfume);
  }

  // 같은 향 기준 관련 상품 최대 4개 조회
  public List<PerfumeListResponseDTO> getRelatedPerfumesByScent(String scent) {
    Pageable limit = PageRequest.of(0, 4); // 0번째 페이지, 4개 한정
    List<Perfume> relatedPerfumes = perfumeRepository.findByScent(scent, limit).getContent();

    return relatedPerfumes.stream()
        .map(PerfumeListResponseDTO::fromEntity)
        .collect(Collectors.toList());
  }

  // 전체 향수 개수 조회
  public long getTotalPerfumeCount() {
    return perfumeRepository.count();
  }

  // 페이지 + 정렬 + 검색까지 적용된 필터링
  public Page<Perfume> getPerfumes(
      String search, String scent, String sortParam, int page, int size) {

    if (search != null && search.isBlank()) {
      search = null;
    }

    if (scent != null && scent.isBlank()) {
      scent = null;
    }

    if (page < 0) {
      page = 0;
    }

    Pageable pageable = PageRequest.of(page, size); // 정렬 빼고 페이징만

    if (sortParam == null) {
      sortParam = "new";
    }

    return switch (sortParam) {
      case "price_asc" ->
          perfumeRepository.findBySearchAndScentWithDiscountPriceAsc(search, scent, pageable);
      case "price_desc" ->
          perfumeRepository.findBySearchAndScentWithDiscountPriceDesc(search, scent, pageable);
      case "review" ->
          perfumeRepository.findBySearchAndScentWithReviewJoin(search, scent, pageable);
      case "new" -> perfumeRepository.findBySearchAndScentWithReviewJoin(search, scent, pageable);
      default -> perfumeRepository.findBySearchAndScentWithReviewJoin(search, scent, pageable);
    };
  }
}
