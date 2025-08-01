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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.reve.domain.Perfume;
import com.example.reve.dto.PerfumeDetailResponseDto;
import com.example.reve.dto.PerfumeListResponseDto;
import com.example.reve.dto.PerfumeSaveRequestDto;
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
  public void savePerfume(PerfumeSaveRequestDto requestDto) {
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
  public String storeImage(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다.");
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String uniqueFilename = UUID.randomUUID().toString() + extension;

      File directory = new File(uploadDir);
      if (!directory.exists()) {
        directory.mkdirs();
      }

      File saveFile = new File(uploadDir, uniqueFilename);
      file.transferTo(saveFile);

      return imageUrlPrefix + uniqueFilename;
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

    // 1. 이미지 파일 삭제
    deleteImage(perfume.getImageUrl());
    deleteImage(perfume.getHoverImageUrl());

    // 2. 향수 DB에서 삭제
    perfumeRepository.delete(perfume);
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

    // 이미지 파일 삭제
    perfumes.forEach(
        perfume -> {
          deleteImage(perfume.getImageUrl());
          deleteImage(perfume.getHoverImageUrl());
        });

    // DB에서 향수 삭제
    perfumeRepository.deleteAllByIdInBatch(perfumeIdList);
  }

  // ============================= 향수 수정하는 로직임(CRUD 중 U) ==========================================
  @Transactional
  public void updatePerfume(
      Long perfumeId,
      PerfumeSaveRequestDto requestDto,
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
      perfume.setImageUrl(newImageUrl);
    }

    if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
      deleteImage(perfume.getHoverImageUrl());
      String newHoverImageUrl = storeImage(hoverImageFile);
      perfume.setHoverImageUrl(newHoverImageUrl);
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
  public List<PerfumeListResponseDto> getAllPerfumes() {
    List<Perfume> perfumes = perfumeRepository.findAllByOrderByCreatedAtDesc();
    return perfumes.stream().map(PerfumeListResponseDto::fromEntity).collect(Collectors.toList());
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
  public PerfumeDetailResponseDto getPerfumeDetail(Long perfumeId) {
    Perfume perfume =
        perfumeRepository
            .findByPerfumeId(perfumeId)
            .orElseThrow(() -> new RuntimeException("해당 향수를 찾을 수 없습니다."));

    return PerfumeDetailResponseDto.fromEntity(perfume);
  }

  // 같은 향 기준 관련 상품 최대 4개 조회
  public List<PerfumeListResponseDto> getRelatedPerfumesByScent(String scent) {
    Pageable limit = PageRequest.of(0, 4); // 0번째 페이지, 4개 한정
    List<Perfume> relatedPerfumes = perfumeRepository.findByScent(scent, limit).getContent();

    return relatedPerfumes.stream()
        .map(PerfumeListResponseDto::fromEntity)
        .collect(Collectors.toList());
  }

  // 페이지 + 정렬 + 검색까지 적용된 필터링
  public Page<Perfume> getPerfumes(
      String search, String scent, String sortParam, int page, int size) {
    Sort sort;

    if (sortParam == null) { // 기본 정렬 기준 = 최신순
      sortParam = "new";
    }

    // 필터링
    switch (sortParam) {
      // 가격 내림차순
      case "price_asc":
        sort = Sort.by("price").ascending();
        break;
      // 가격 오름차순
      case "price_desc":
        sort = Sort.by("price").descending();
        break;
      // 리뷰 많은 순 정렬
      case "review":
        sort = Sort.by("reviewCount").descending();
        break;
      default:
        sort = Sort.by("createdAt").descending();
    }

    if (search != null && search.isBlank()) {
      search = null;
    }

    if (scent != null && scent.isBlank()) {
      scent = null;
    }

    // 페이지 0보다 작을 일 없게 함.
    if (page < 0) {
      page = 0;
    }

    Pageable pageable = PageRequest.of(page, size, sort);

    return perfumeRepository.findBySearchAndScentWithReviewJoin(search, scent, pageable);
  }
}
