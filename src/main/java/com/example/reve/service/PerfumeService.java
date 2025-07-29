package com.example.reve.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  // 관리자 향수 등록
  @Transactional
  public Long savePerfume(PerfumeSaveRequestDto requestDto) {
    Perfume perfume =
        Perfume.builder()
            .perfumeName(requestDto.getPerfumeName())
            .scent(requestDto.getScent())
            .description(requestDto.getDescription())
            .stock(requestDto.getStock())
            .price(requestDto.getPrice())
            .discount(requestDto.getDiscount())
            .imageUrl(requestDto.getImageUrl())
            .hoverImageUrl(requestDto.getHoverImageUrl())
            .build();
    perfumeRepository.save(perfume);
    return perfume.getPerfumeId();
  }

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
}
