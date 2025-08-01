package com.example.reve.service;

import com.example.reve.domain.Perfume;
import com.example.reve.domain.User;
import com.example.reve.domain.WishList;
import com.example.reve.repository.PerfumeRepository;
import com.example.reve.repository.UserRepository;
import com.example.reve.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {
    private final UserRepository userRepository;
    private final PerfumeRepository perfumeRepository;
    private final WishListRepository wishListRepository;
    //목록 추가하기
    public void addWishlist(Long userId,Long perfumeId) {
        //유저 찾기
        User user = userRepository.findById(userId).orElseThrow();
        //상품 찾기
        Perfume perfume = perfumeRepository.findById(perfumeId).orElseThrow();
        WishList wishList = new WishList();
        wishList.setUser(user);
        wishList.setPerfume(perfume);
        //저장
        wishListRepository.save(wishList);
    }
}
