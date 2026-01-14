package com.recomon.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : com.recomon.domain
 * fileName       : Member
 * author         : kobe
 * date           : 2026. 1. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 11.        kobe       최초 생성
 */
@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    // 유저가 선호하는 카테고리 목록 (의외성 추천 시 '제외'할 기준이 됨)
    @ElementCollection
    private List<String> favoriteCategories = new ArrayList<>();
}
