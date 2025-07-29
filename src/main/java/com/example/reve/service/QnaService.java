package com.example.reve.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.Qna;
import com.example.reve.domain.User;
import com.example.reve.dto.QnaReqDto;
import com.example.reve.dto.QnaResDto;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.QnaRepository;
import com.example.reve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

  private final QnaRepository qnaRepository;
  private final UserRepository userRepository;
  private final PerfumeRepository perfumeRepository;

  @Transactional
  public QnaResDto createQna(QnaReqDto reqDto) {
    // 1. DTO에서 받은 userId로 진짜 유저가 DB에 있는지 찾기
    Optional<User> userOptional = userRepository.findById(reqDto.getUserId());
    User user; // 나중에 사용될 User 객체를 미리 선언

    // 2. Optional 안에 User가 있는지 확인
    if (userOptional.isPresent()) {
      // User 있으면, 꺼내서 user 변수에 담기
      user = userOptional.get();
    } else {
      // 없으면 에러를 발생시키기
      throw new IllegalArgumentException("해당 유저를 찾을 수 없습니다. ID: " + reqDto.getUserId());
    }

    // 3. perfumeId로 진짜 상품이 DB에 있는지 확인
    Optional<Perfume> perfumeOptional = perfumeRepository.findById(reqDto.getPerfumeId());
    Perfume perfume;

    // 4. Optional 안에 Perfume 있는지 확인
    if (perfumeOptional.isPresent()) {
      // Perfume 있으면, 꺼내서 perfume 변수에 담기
      perfume = perfumeOptional.get();
    } else {
      // 없으면 에러를 발생시키기
      throw new IllegalArgumentException("해당 상품을 찾을 수 없습니다. ID: " + reqDto.getPerfumeId());
    }

    // 5. 유저와 상품 정보가 있으면, Qna 게시글 생성
    Qna qna = new Qna();

    qna.setTitle(reqDto.getTitle());
    qna.setContent(reqDto.getContent());
    qna.setPassword(reqDto.getPassword());
    qna.setIsSecret(reqDto.getIsSecret());
    qna.setCategory(reqDto.getCategory());
    qna.setUser(user); // 위에서 찾은 유저 정보
    qna.setPerfume(perfume); // 위에서 찾은 상품 정보

    // 6. 만들어진 Qna 게시글을 DB에 저장
    Qna savedQna = qnaRepository.save(qna);

    // 7. 저장된 최종 결과를 바탕으로 클라이언트에게 보여줄 응답(DTO)을 만들어 반환
    return new QnaResDto(savedQna);
  }

  /** 모든 Q&A 게시글을 조회하여 QnaResDto 리스트로 반환 */
  public List<QnaResDto> selectAll() {
    // 1. QnaRepository를 사용하여 데이터베이스에서 모든 Qna 엔티티를 가져오기
    List<Qna> qnaList = qnaRepository.findAll();

    // 2. QnaResDto 객체들을 담을 빈 리스트 만들기
    List<QnaResDto> qnaResDtoList = new ArrayList<>();

    // 3. 데이터베이스에서 가져온 qnaList의 각 Qna 객체를 순회하며 살펴보기
    for (Qna qna : qnaList) {
      // 4. 현재 살펴보고 있는 qna를 가지고 새로운 QnaResDto 객체 만들기
      QnaResDto qnaResDto = new QnaResDto(qna);

      // 5. 새로 만들어진 QnaResDto 객체를, 아까 만들어둔 빈 리스트에 추가
      qnaResDtoList.add(qnaResDto);
    }

    // 6. 모든 Qna 객체를 QnaResDto로 변환하여 담은 리스트 반환
    return qnaResDtoList;
  }
}
