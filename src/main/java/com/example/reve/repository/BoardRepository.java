package com.example.reve.repository;

import com.example.reve.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

  // 키워드가 포함된 게시글을 찾는 쿼리
  List<Board> findByTitleContaining(String keyword);
  // 카테고리별 게시글 조회
  List<Board> findByCategory(String category);
}
